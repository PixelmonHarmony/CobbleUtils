package com.kingpixel.cobbleutils.command.admin;

import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.options.Pokerus;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

/**
 * @author Carlos Varas Alonso - 20/07/2024 8:36
 */
public class PokerusCommand implements Command<CommandSourceStack> {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.requires(source -> source.hasPermission(2))
        .then(
          Commands.literal("pokerus")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("slot", PartySlotArgumentType.Companion.partySlot())
                .executes(context -> {
                  if (!context.getSource().isPlayer()) {
                    CobbleUtils.LOGGER.info("You must be a player to use this command");
                    return 0;
                  }
                  Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "slot");
                  if (pokemon != null) {
                    Pokerus.apply(pokemon);
                    AdventureTranslator.toNative(
                      PokemonUtils.replace(
                        "Applied Pokerus to %pokemon%", pokemon
                      )
                    );
                  } else {
                    CobbleUtils.LOGGER.info("Pokemon not found");
                  }
                  return 1;
                })
                .then(
                  Commands.argument("player", EntityArgument.player())
                    .executes(context -> {
                        Pokemon pokemon = PartySlotArgumentType.Companion.getPokemonOf(context, "slot", EntityArgument.getPlayer(context, "player"));

                        if (pokemon != null) {
                          Pokerus.apply(pokemon);
                          AdventureTranslator.toNative(
                            PokemonUtils.replace(
                              "Applied Pokerus to %pokemon%", pokemon
                            )
                          );
                        } else {
                          CobbleUtils.LOGGER.info("Pokemon not found");
                        }
                        return 1;
                      }
                    )
                )
            )
        )
    );


  }

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

    return 1;
  }
}
