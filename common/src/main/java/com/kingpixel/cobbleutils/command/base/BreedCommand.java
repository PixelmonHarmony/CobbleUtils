package com.kingpixel.cobbleutils.command.base;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.models.EggData;
import com.kingpixel.cobbleutils.features.breeding.ui.PlotBreedingUI;
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
                      return 0;
                    }
                    if (male != female && EggData.isCompatible(male, female)) {
                      try {
                        if (!CobbleUtils.breedconfig.isDitto()) {
                          return dittos(male, player, female);
                        }
                        if (!CobbleUtils.breedconfig.isDoubleditto()) {
                          return dittos(male, player, female);
                        }
                        if (male.getGender() == Gender.FEMALE) {
                          player.sendMessage(AdventureBreeding.adventure(PokemonUtils.replace(
                            CobbleUtils.breedconfig.getNotCompatible(),
                            List.of(male, female))));
                          return 0;
                        }
                        if (female.getGender() == Gender.MALE) {
                          player.sendMessage(AdventureBreeding.adventure(PokemonUtils.replace(
                            CobbleUtils.breedconfig.getNotCompatible(),
                            List.of(male, female))));
                          return 0;
                        }

                        if (!EggData.isCompatible(male, female)) {
                          player.sendMessage(AdventureBreeding.adventure(PokemonUtils.replace(
                            CobbleUtils.breedconfig.getNotCompatible(), List.of(male, female))));
                          return 0;
                        }
                        if (CobbleUtils.breedconfig.getBlacklist().contains(male.getSpecies().showdownId())) {
                          player.sendMessage(AdventureBreeding.adventure(PokemonUtils.replace(
                            CobbleUtils.breedconfig.getNotCompatible(), List.of(male, female))));
                          return 0;
                        }
                        if (CobbleUtils.breedconfig.getBlacklist().contains(female.getSpecies().showdownId())) {
                          player.sendMessage(AdventureBreeding.adventure(PokemonUtils.replace(
                            CobbleUtils.breedconfig.getNotCompatible(), List.of(male, female))));
                          return 0;
                        }
                        Pokemon egg = EggData.createEgg(male, female, player);
                        if (egg != null) {
                          Cobblemon.INSTANCE.getStorage().getParty(player).add(egg);
                          cooldowns.put(player.getUuid(),
                            new Date().getTime() + TimeUnit.SECONDS
                              .toMillis(CobbleUtils.breedconfig.getCooldowninstaBreedInSeconds()));
                        } else {
                          player.sendMessage(AdventureBreeding.adventure(PokemonUtils.replace(
                            CobbleUtils.breedconfig.getNotCompatible(),
                            List.of(male, female))));
                          return 0;
                        }
                      } catch (NoPokemonStoreException e) {
                        throw new RuntimeException(e);
                      }
                    } else {
                      player.sendMessage(AdventureBreeding.adventure(PokemonUtils.replace(
                        CobbleUtils.breedconfig.getNotCompatible(),
                        List.of(male, female))));
                      return 0;
                    }
                    return 1;
                  }))));

  }

  private static Integer dittos(Pokemon male, ServerPlayerEntity player, Pokemon female) {
    if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      player.sendMessage(
        AdventureBreeding.adventure(
          CobbleUtils.breedconfig.getNotdoubleditto()));
      return 0;
    }
    if (female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      player.sendMessage(
        AdventureBreeding.adventure(
          CobbleUtils.breedconfig.getNotdoubleditto()));
      return 0;
    }
    return 1;
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }

}
