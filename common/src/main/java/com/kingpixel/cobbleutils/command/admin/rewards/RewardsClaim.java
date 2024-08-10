package com.kingpixel.cobbleutils.command.admin.rewards;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 28/06/2024 21:04
 */
public class RewardsClaim implements Command<ServerCommandSource> {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
      LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
        base
            .then(
                CommandManager.literal("claim")
                    .executes(new RewardsClaim())));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    if (!context.getSource().isExecutedByPlayer()) {
      CobbleUtils.LOGGER.info("Only players can claim rewards!");
      return 0;
    }
    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
    RewardsUtils.claimRewards(player);

    return 1;
  }
}
