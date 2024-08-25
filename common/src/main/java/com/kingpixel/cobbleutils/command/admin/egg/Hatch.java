package com.kingpixel.cobbleutils.command.admin.egg;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.util.AdventureBreeding;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
              if (!context.getSource().isExecutedByPlayer())
                return 0;
              ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
              Long cooldown = cooldowns.get(player.getUuid());

              if (cooldown != null && PlayerUtils.isCooldown(cooldown)) {
                player.sendMessage(AdventureBreeding.adventure(
                  CobbleUtils.language.getMessageCooldown()
                    .replace("%cooldown%", PlayerUtils.getCooldown(new Date(cooldown)))));
                return 0;
              }

              if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null)
                return 0;
              Pokemon egg = PartySlotArgumentType.Companion.getPokemon(context, "egg");

              if (egg.getSpecies().showdownId().equalsIgnoreCase("egg")) {
                if (LuckPermsUtil.hasOp(player)) {
                  cooldowns.put(player.getUuid(), new Date(1).getTime());
                } else {
                  cooldowns.put(player.getUuid(),
                    new Date().getTime()
                      + TimeUnit.SECONDS.toMillis(CobbleUtils.breedconfig.getCooldowninstaHatchInSeconds()));
                }

                egg.getPersistentData().putInt("steps", 0);
                egg.getPersistentData().putInt("cycles", 0);
              }
              return 1;
            })
        ).then(
          CommandManager.literal("all")
            .requires(source -> LuckPermsUtil.checkPermission(source, 2,
              List.of("cobbleutils.hatch.all", "cobbleutils.admin")))
            .executes(context -> {
              if (!context.getSource().isExecutedByPlayer())
                return 0;
              ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
              Long cooldown = cooldowns.get(player.getUuid());

              if (cooldown != null && PlayerUtils.isCooldown(cooldown)) {
                player.sendMessage(AdventureBreeding.adventure(
                  CobbleUtils.language.getMessageCooldown()
                    .replace("%cooldown%", PlayerUtils.getCooldown(new Date(cooldown)))));
                return 0;
              }

              if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null)
                return 0;

              if (LuckPermsUtil.hasOp(player)) {
                cooldowns.put(player.getUuid(), new Date(1).getTime());
              } else {
                cooldowns.put(player.getUuid(),
                  new Date().getTime()
                    + TimeUnit.SECONDS.toMillis(CobbleUtils.breedconfig.getCooldowninstaHatchInSeconds()));
              }

              try {
                Cobblemon.INSTANCE.getStorage().getParty(player.getUuid()).forEach(egg -> {
                  if (egg.getSpecies().showdownId().equalsIgnoreCase("egg")) {
                    egg.getPersistentData().putInt("steps", 0);
                    egg.getPersistentData().putInt("cycles", 0);
                  }
                });
                Cobblemon.INSTANCE.getStorage().getPC(player.getUuid()).forEach(egg -> {
                  if (egg.getSpecies().showdownId().equalsIgnoreCase("egg")) {
                    egg.getPersistentData().putInt("steps", 0);
                    egg.getPersistentData().putInt("cycles", 0);
                  }
                });
              } catch (NoPokemonStoreException e) {
                throw new RuntimeException(e);
              }
              return 1;
            })
        )
    );

  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }
}