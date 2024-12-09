package com.kingpixel.cobbleutils.command.admin.rewards;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 28/06/2024 21:04
 */
public class RewardsRemove implements Command<ServerCommandSource> {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .then(
          CommandManager.literal("remove")
            .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.storage_rewards.remove",
              "cobbleutils" +
                ".admin")))
            .executes(context -> {
              if (!context.getSource().isExecutedByPlayer()) {
                CobbleUtils.LOGGER.info("Only players can remove rewards!");
                return 0;
              }
              ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
              RewardsUtils.removeRewards(player);
              return 1;
            })
            .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.storage_rewards.remove",
              "cobbleutils" +
                ".admin")))
            .then(CommandManager.argument("player", EntityArgumentType.player())
              .executes(context -> {
                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                RewardsUtils.removeRewards(player);
                return 1;
              }))));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 1;
  }
}
