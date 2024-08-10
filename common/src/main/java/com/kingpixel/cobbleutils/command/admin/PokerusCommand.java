package com.kingpixel.cobbleutils.command.admin;

import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.options.Pokerus;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 20/07/2024 8:36
 */
public class PokerusCommand implements Command<ServerCommandSource> {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .then(
          CommandManager.literal("pokerus")
            .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.pokerus", "cobbleutils.admin")))
            .then(
              CommandManager.argument("slot", PartySlotArgumentType.Companion.partySlot())
                .executes(context -> {
                  if (!context.getSource().isExecutedByPlayer()) {
                    CobbleUtils.LOGGER.info("You must be a player to use this command");
                    return 0;
                  }
                  Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "slot");
                  if (pokemon != null) {
                    Pokerus.apply(pokemon);
                    AdventureTranslator.toNative(
                      PokemonUtils.replace(
                        "Applied Pokerus to %pokemon%", pokemon));
                  } else {
                    CobbleUtils.LOGGER.info("Pokemon not found");
                  }
                  return 1;
                })
                .then(
                  CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> {
                      Pokemon pokemon = PartySlotArgumentType.Companion.getPokemonOf(context, "slot",
                        EntityArgumentType.getPlayer(context, "player"));

                      if (pokemon != null) {
                        Pokerus.apply(pokemon);
                        AdventureTranslator.toNative(
                          PokemonUtils.replace(
                            "Applied Pokerus to %pokemon%", pokemon));
                      } else {
                        CobbleUtils.LOGGER.info("Pokemon not found");
                      }
                      return 1;
                    })))));

  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

    return 1;
  }
}
