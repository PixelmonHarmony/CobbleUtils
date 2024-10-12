package com.kingpixel.cobbleutils.command.admin.egg;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.models.EggData;
import com.kingpixel.cobbleutils.features.breeding.util.AdventureBreeding;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.PlayerUtils;
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
 * @author Carlos Varas Alonso - 09/08/2024 3:27
 */
public class Hatch implements Command<ServerCommandSource> {
  private static Map<UUID, Long> cooldowns = new HashMap<>();

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {

    dispatcher.register(
      base
        .requires(source -> LuckPermsUtil.checkPermission(source, 2,
          List.of("cobbleutils.hatch", "cobbleutils.admin")))
        .then(
          CommandManager.argument("egg", PartySlotArgumentType.Companion.partySlot())
            .executes(context -> {
              if (!context.getSource().isExecutedByPlayer()) return 0;
              ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
              return hatch(context, player);
            }).then(
              CommandManager.argument("player", EntityArgumentType.player())
                .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.hatchother",
                  "cobbleutils.admin")))
                .executes(context -> {
                  ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                  return hatch(context, player);
                })
            )
        ).then(
          CommandManager.literal("all")
            .requires(source -> LuckPermsUtil.checkPermission(source, 2,
              List.of("cobbleutils.hatchall", "cobbleutils.admin")))
            .executes(context -> {
              if (!context.getSource().isExecutedByPlayer())
                return 0;
              ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
              if (cooldown(player)) return 0;

              if (LuckPermsUtil.hasOp(player)) {
                cooldowns.put(player.getUuid(), new Date(1).getTime());
              } else {
                cooldowns.put(player.getUuid(),
                  new Date().getTime()
                    + TimeUnit.SECONDS.toMillis(CobbleUtils.breedconfig.getCooldowninstaHatchInSeconds()));
              }

              try {
                Cobblemon.INSTANCE.getStorage().getParty(player.getUuid()).forEach(pokemon -> {
                  openEgg(player, pokemon);
                });
                Cobblemon.INSTANCE.getStorage().getPC(player.getUuid()).forEach(pokemon -> {
                  openEgg(player, pokemon);
                });
              } catch (NoPokemonStoreException e) {
                throw new RuntimeException(e);
              }
              return 1;
            }).then(
              CommandManager.argument("player", EntityArgumentType.player())
                .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.hatchotherall",
                  "cobbleutils.admin")))
                .executes(context -> {
                  ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                  if (cooldown(player)) return 0;

                  if (LuckPermsUtil.hasOp(player)) {
                    cooldowns.put(player.getUuid(), new Date(1).getTime());
                  } else {
                    cooldowns.put(player.getUuid(),
                      new Date().getTime()
                        + TimeUnit.SECONDS.toMillis(CobbleUtils.breedconfig.getCooldowninstaHatchInSeconds()));
                  }

                  try {
                    Cobblemon.INSTANCE.getStorage().getParty(player.getUuid()).forEach(pokemon -> {
                      openEgg(player, pokemon);
                    });
                    Cobblemon.INSTANCE.getStorage().getPC(player.getUuid()).forEach(pokemon -> {
                      openEgg(player, pokemon);
                    });
                  } catch (NoPokemonStoreException e) {
                    throw new RuntimeException(e);
                  }
                  return 1;
                })
            )
        )
    );

  }

  private static boolean cooldown(ServerPlayerEntity player) {
    Long cooldown = cooldowns.get(player.getUuid());

    if (cooldown != null && PlayerUtils.isCooldown(cooldown)) {
      player.sendMessage(AdventureBreeding.adventure(
        CobbleUtils.language.getMessageCooldown()
          .replace("%cooldown%", PlayerUtils.getCooldown(new Date(cooldown)))));
      return true;
    }

    if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null)
      return true;
    return false;
  }

  private static int hatch(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
    if (cooldown(player)) return 0;
    Pokemon egg = PartySlotArgumentType.Companion.getPokemonOf(context, "egg", player);

    if (egg.getSpecies().showdownId().equalsIgnoreCase("egg")) {
      if (LuckPermsUtil.hasOp(player)) {
        cooldowns.put(player.getUuid(), new Date(1).getTime());
      } else {
        cooldowns.put(player.getUuid(),
          new Date().getTime()
            + TimeUnit.SECONDS.toMillis(CobbleUtils.breedconfig.getCooldowninstaHatchInSeconds()));
      }
      openEgg(player, egg);
    }
    return 1;
  }


  public static void openEgg(ServerPlayerEntity player, Pokemon pokemon) {
    if (pokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) {
      pokemon.getPersistentData().putInt("steps", 0);
      pokemon.getPersistentData().putInt("cycles", -1);
      pokemon.setCurrentHealth(0);
      EggData eggData = EggData.from(pokemon);
      eggData.steps(player, pokemon, Integer.MAX_VALUE);
      eggData.EggToPokemon(player, pokemon);
    }
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }
}