package com.kingpixel.cobbleutils.command.base;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 26/07/2024 14:14
 */
public class PokeShoutAll implements Command<ServerCommandSource> {
  private static Map<UUID, Date> cooldowns = new ConcurrentHashMap<>();

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.pokeshoutplusall",
          "cobbleutils" +
            ".user")))
        .executes(context -> {
          if (!context.getSource().isExecutedByPlayer()) {
            CobbleUtils.LOGGER.error("This command can only be executed by a player");
            return 0;
          }
          ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
          Date cooldown = cooldowns.get(player.getUuid());
          if (PlayerUtils.isCooldown(cooldown)) {
            PlayerUtils.sendMessage(player, CobbleUtils.language.getMessageCooldown()
              .replace("%cooldown%", String.valueOf(PlayerUtils.getCooldown(cooldown)))
              .replace("%prefix%", CobbleUtils.config.getPrefix())
            );
            return 0;
          }
          cooldowns.put(player.getUuid(), new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(CobbleUtils.config.getCooldownpokeshout())));
          PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
          if (playerPartyStore.size() == 0) {
            player.sendMessage(
              AdventureTranslator.toNative(
                CobbleUtils.language.getMessageNoPokemon()
              )
            );
            return 0;
          }
          playerPartyStore.forEach(pokemon -> Utils.broadcastMessage(PokeShout.getMessage(player, pokemon)));
          return 1;
        }));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }

}
