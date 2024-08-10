package com.kingpixel.cobbleutils.command.base;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 26/07/2024 14:14
 */
public class PokeShoutAll implements Command<ServerCommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .requires(source -> LuckPermsUtil.checkPermission(source, 0, List.of("cobbleutils.pokeshout", "cobbleutils" +
          ".user")))
        .executes(context -> {
          if (!context.getSource().isExecutedByPlayer()) {
            CobbleUtils.LOGGER.error("This command can only be executed by a player");
            return 0;
          }
          ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
          PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
          playerPartyStore.forEach(pokemon -> Utils.broadcastMessage(PokeShout.getMessage(player, pokemon)));
          return 0;
        }));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }

}
