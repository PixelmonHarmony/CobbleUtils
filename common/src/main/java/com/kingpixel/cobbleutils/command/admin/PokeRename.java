package com.kingpixel.cobbleutils.command.admin;

import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author Carlos Varas Alonso - 18/07/2024 11:28
 */
public class PokeRename implements Command<CommandSourceStack> {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.requires(source -> source.hasPermission(2))
        .then(
          Commands.literal("pokerename")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("slot", PartySlotArgumentType.Companion.partySlot())
                .then(
                  Commands.argument("name", StringArgumentType.greedyString())
                    .executes(new PokeRename())
                )
            )
        )
    );


  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    try {
      if (!context.getSource().isPlayer()) return 0;

      ServerPlayer player = context.getSource().getPlayerOrException();
      Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "slot");

      String name = StringArgumentType.getString(context, "name");

      player.sendSystemMessage(Component.literal("This command is not implemented yet, please wait."));

      MutableComponent displayName = AdventureTranslator.toNativeComponent(name);

      pokemon.setNickname(displayName);

      PokemonEntity pokemonEntity = pokemon.getEntity();
      if (pokemonEntity != null) {
        pokemonEntity.setCustomName(displayName);
        pokemonEntity.setCustomNameVisible(true);
      }
      JsonObject json = pokemon.saveToJSON(new JsonObject());

      if (json.has("Nickname")) {
        JsonObject nicknameObj = json.getAsJsonObject("Nickname");
        if (nicknameObj.has("text")) {
          nicknameObj.remove("text");
          nicknameObj.addProperty("text", displayName.getString());
        }
      }
      pokemon.loadFromJSON(json);
      context.getSource().getPlayerOrException().sendSystemMessage(displayName);

    } catch (Exception e) {
      CobbleUtils.LOGGER.error(e.getMessage());
      return 0;
    }
    return 1;
  }

}