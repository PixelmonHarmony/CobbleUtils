package com.kingpixel.cobbleutils.command.admin;

import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.CobbleUtilsTags;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author Carlos Varas Alonso - 20/07/2024 15:03
 */
public class BreedableCommand implements Command<CommandSourceStack> {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.requires(source -> source.hasPermission(2))
        .then(
          Commands.literal("breedable")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("slot", PartySlotArgumentType.Companion.partySlot())
                .then(
                  Commands.argument("breedable", StringArgumentType.greedyString())
                    .suggests((context, builder) -> {
                      builder.suggest("true");
                      builder.suggest("false");
                      return builder.buildFuture();
                    })
                    .executes(context -> {
                      if (!context.getSource().isPlayer()) {
                        CobbleUtils.LOGGER.info("You must be a player to use this command");
                        return 0;
                      }

                      Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "slot");
                      if (pokemon != null) {
                        boolean breedable = StringArgumentType.getString(context, CobbleUtilsTags.BREEDABLE_TAG).equalsIgnoreCase("true");
                        pokemon.getPersistentData().putBoolean(CobbleUtilsTags.BREEDABLE_TAG, breedable);
                        AdventureTranslator.toNative(
                          PokemonUtils.replace(
                            "Set breedable to %breedable% to %pokemon%"
                              .replace("%breedable%", String.valueOf(breedable)), pokemon
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
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            Pokemon pokemon = PartySlotArgumentType.Companion.getPokemonOf(context, "slot", player);
                            if (pokemon != null) {
                              boolean breedable = StringArgumentType.getString(context, "breedable").equalsIgnoreCase("true");
                              pokemon.getPersistentData().putBoolean(CobbleUtilsTags.BREEDABLE_TAG, breedable);
                              player.sendSystemMessage(
                                AdventureTranslator.toNative(
                                  PokemonUtils.replace(
                                    "Set breedable to %breedable% to %pokemon%"
                                      .replace("%breedable%", String.valueOf(breedable)), pokemon
                                  )
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
        )
    );


  }

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

    return 1;
  }
}