package com.kingpixel.cobbleutils.command.base;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.models.EggData;
import com.kingpixel.cobbleutils.features.breeding.ui.PlotBreedingUI;
import com.kingpixel.cobbleutils.features.breeding.ui.PlotSelectPokemonUI;
import com.kingpixel.cobbleutils.features.breeding.util.AdventureBreeding;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 02/08/2024 12:23
 */
public class BreedCommand implements Command<ServerCommandSource> {
  private static Map<UUID, Long> cooldowns = new HashMap<>();

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base.executes(
          context -> {
            if (!context.getSource().isExecutedByPlayer()) {
              return 0;
            }
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
              return 0;
            }
            PlotBreedingUI.open(player);
            return 1;
          }).then(
          CommandManager.literal("other")
            .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.breed.other", "cobbleutils.admin")))
            .then(
              CommandManager.argument("player", EntityArgumentType.players())
                .requires(source -> source.hasPermissionLevel(2))
                .executes(
                  context -> {
                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                    if (Cobblemon.INSTANCE.getBattleRegistry()
                      .getBattleByParticipatingPlayer(player) != null) {
                      return 0;
                    }
                    PlotBreedingUI.open(player);
                    return 1;
                  })))
        .then(
          CommandManager.argument("male", PartySlotArgumentType.Companion.partySlot())
            .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.breed.pokemons", "cobbleutils.admin")))
            .then(
              CommandManager.argument("female", PartySlotArgumentType.Companion.partySlot())
                .executes(
                  context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                      return 0;
                    }

                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

                    if (Cobblemon.INSTANCE.getBattleRegistry()
                      .getBattleByParticipatingPlayer(player) != null) {
                      return 0;
                    }
                    Long cooldown = cooldowns.get(player.getUuid());

                    if (cooldown != null && PlayerUtils.isCooldown(cooldown)) {
                      player.sendMessage(AdventureTranslator.toNative(
                        CobbleUtils.language.getMessageCooldown()
                          .replace("%cooldown%", PlayerUtils.getCooldown(new Date(cooldown)))));
                      return 0;
                    }

                    Pokemon male = PartySlotArgumentType.Companion.getPokemon(context, "male");
                    Pokemon female = PartySlotArgumentType.Companion.getPokemon(context, "female");

                    if (male == null || female == null) {
                      player.sendMessage(AdventureBreeding.adventure(
                        CobbleUtils.breedconfig.getNotCompatible()));
                      return 0;
                    }

                    try {
                      if (!PlotSelectPokemonUI.arePokemonsCompatible(
                        male, female, player, true
                      )) return 0;
                      Pokemon egg;

                      if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
                        egg = EggData.createEgg(male, female, player);
                      } else if (female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
                        egg = EggData.createEgg(female, male, player);
                      } else {
                        egg = EggData.createEgg(male, female, player);
                      }

                      // Creación del huevo y manejo del cooldown

                      if (egg != null) {
                        Cobblemon.INSTANCE.getStorage().getParty(player).add(egg);
                        if (LuckPermsUtil.hasOp(player)) {
                          cooldowns.put(player.getUuid(), new Date(1).getTime());
                        } else {
                          cooldowns.put(player.getUuid(),
                            new Date().getTime()
                              + TimeUnit.SECONDS.toMillis(CobbleUtils.breedconfig.getCooldowninstaBreedInSeconds()));
                        }
                      } else {
                        player.sendMessage(AdventureBreeding.adventure(PokemonUtils.replace(
                          CobbleUtils.breedconfig.getNotCompatible(), List.of(male, female))));
                        return 0;
                      }
                    } catch (NoPokemonStoreException e) {
                      throw new RuntimeException(e);
                    }

                    return 1;
                  }).then(
                  CommandManager.argument("player", EntityArgumentType.players())
                    .executes(
                      context -> {
                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");

                        if (Cobblemon.INSTANCE.getBattleRegistry()
                          .getBattleByParticipatingPlayer(player) != null) {
                          return 0;
                        }
                        Long cooldown = cooldowns.get(player.getUuid());

                        if (cooldown != null && PlayerUtils.isCooldown(cooldown)) {
                          player.sendMessage(AdventureTranslator.toNative(
                            CobbleUtils.language.getMessageCooldown()
                              .replace("%cooldown%", PlayerUtils.getCooldown(new Date(cooldown)))));
                          return 0;
                        }

                        Pokemon male = PartySlotArgumentType.Companion.getPokemonOf(context, "male", player);
                        Pokemon female = PartySlotArgumentType.Companion.getPokemonOf(context, "female", player);

                        if (male == null || female == null) {
                          player.sendMessage(AdventureBreeding.adventure(
                            CobbleUtils.breedconfig.getNotCompatible()));
                          return 0;
                        }

                        try {
                          if (!PlotSelectPokemonUI.arePokemonsCompatible(
                            male, female, player, true
                          )) return 0;
                          Pokemon egg;

                          if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
                            egg = EggData.createEgg(male, female, player);
                          } else if (female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
                            egg = EggData.createEgg(female, male, player);
                          } else {
                            egg = EggData.createEgg(male, female, player);
                          }

                          // Creación del huevo y manejo del cooldown

                          if (egg != null) {
                            Cobblemon.INSTANCE.getStorage().getParty(player).add(egg);
                            if (LuckPermsUtil.hasOp(player)) {
                              cooldowns.put(player.getUuid(), new Date(1).getTime());
                            } else {
                              cooldowns.put(player.getUuid(),
                                new Date().getTime()
                                  + TimeUnit.SECONDS.toMillis(CobbleUtils.breedconfig.getCooldowninstaBreedInSeconds()));
                            }
                          } else {
                            player.sendMessage(AdventureBreeding.adventure(PokemonUtils.replace(
                              CobbleUtils.breedconfig.getNotCompatible(), List.of(male, female))));
                            return 0;
                          }
                        } catch (NoPokemonStoreException e) {
                          throw new RuntimeException(e);
                        }

                        return 1;
                      }
                    )
                )
            )
        )
    );

  }


  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }

}
