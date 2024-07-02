package com.kingpixel.cobbleutils.command.admin.rewards;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

/**
 * @author Carlos Varas Alonso - 28/06/2024 21:04
 */
public class RewardsClaim implements Command<CommandSourceStack> {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base
        .then(
          Commands.literal("claim")
            .executes(new RewardsClaim())
        )
    );
  }

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    if (CobbleUtils.config.isDebug())
      CobbleUtils.LOGGER.info("RewardsClaim command");
    Player player = context.getSource().getPlayerOrException();
    RewardsUtils.claimRewards(player);

    return 1;
  }
}
