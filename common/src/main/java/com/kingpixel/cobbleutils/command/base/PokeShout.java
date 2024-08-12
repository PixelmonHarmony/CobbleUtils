package com.kingpixel.cobbleutils.command.base;

import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 26/07/2024 14:14
 */
public class PokeShout implements Command<CommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .requires(source -> LuckPermsUtil.checkPermission(source, 0, List.of("cobbleutils.pokeshout", "cobbleutils" +
          ".user")))
        .then(
          CommandManager.argument("slot", PartySlotArgumentType.Companion.partySlot())
            .executes(context -> {
              if (!context.getSource().isExecutedByPlayer()) {
                CobbleUtils.LOGGER.error("This command can only be executed by a player");
                return 0;
              }
              Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "slot");
              ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
              if (pokemon != null) {
                Utils.broadcastMessage(getMessage(player, pokemon));
                return 1;
              } else {
                PlayerUtils.sendMessage(player, CobbleUtils.language.getMessageNoPokemon());
                return 0;
              }
            })));
  }

  @Override
  public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
    return 0;
  }

  public static Text getMessage(ServerPlayerEntity player, Pokemon pokemon) {
    String baseMessage = CobbleUtils.language.getMessagePokeShout();
    String playerName = player.getGameProfile().getName();
    String messageContent = PokemonUtils.replace(baseMessage, pokemon).replace("%player%", playerName);

    return AdventureTranslator.toNativeComponent(messageContent)
      .setStyle(Style.EMPTY
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
          AdventureTranslator.toNative(PokemonUtils.replace(
            "%lorepokemon%", pokemon)))));
  }

}
