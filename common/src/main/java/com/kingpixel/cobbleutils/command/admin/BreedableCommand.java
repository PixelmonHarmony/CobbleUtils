package com.kingpixel.cobbleutils.command.admin;

import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.CobbleUtilsTags;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 20/07/2024 15:03
 */
public class BreedableCommand implements Command<ServerCommandSource> {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .then(
          CommandManager.literal("breedable")
            .requires(source ->
              LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.breedable", "cobbleutils.admin")))
            .then(
              CommandManager.argument("slot", PartySlotArgumentType.Companion.partySlot())
                .then(
                  CommandManager.argument("breedable", StringArgumentType.greedyString())
                    .suggests((context, builder) -> {
                      builder.suggest("true");
                      builder.suggest("false");
                      return builder.buildFuture();
                    })
                    .executes(context -> {
                      if (!context.getSource().isExecutedByPlayer()) {
                        CobbleUtils.LOGGER.info("You must be a player to use this command");
                        return 0;
                      }

                      Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "slot");
                      if (pokemon != null) {
                        boolean breedable = StringArgumentType
                          .getString(context, CobbleUtilsTags.BREEDABLE_TAG).equalsIgnoreCase("true");
                        pokemon.getPersistentData().putBoolean(CobbleUtilsTags.BREEDABLE_TAG,
                          breedable);
                        AdventureTranslator.toNative(
                          PokemonUtils.replace(
                            "Set breedable to %breedable% to %pokemon%"
                              .replace("%breedable%", String.valueOf(breedable)),
                            pokemon));
                      } else {
                        CobbleUtils.LOGGER.info("Pokemon not found");
                      }
                      return 1;
                    })
                    .then(
                      CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                          ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                          Pokemon pokemon = PartySlotArgumentType.Companion.getPokemonOf(context,
                            "slot", player);
                          if (pokemon != null) {
                            boolean breedable = StringArgumentType.getString(context, "breedable")
                              .equalsIgnoreCase("true");
                            pokemon.getPersistentData().putBoolean(CobbleUtilsTags.BREEDABLE_TAG,
                              breedable);
                            player.sendMessage(
                              AdventureTranslator.toNative(
                                PokemonUtils.replace(
                                  "Set breedable to %breedable% to %pokemon%"
                                    .replace("%breedable%", String.valueOf(breedable)),
                                  pokemon)));
                          } else {
                            CobbleUtils.LOGGER.info("Pokemon not found");
                          }
                          return 1;
                        }))))));

  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

    return 1;
  }
}