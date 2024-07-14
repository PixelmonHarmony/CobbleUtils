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
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

/**
 * @author Carlos Varas Alonso - 28/06/2024 21:04
 */
public class RewardsRemove implements Command<CommandSourceStack> {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base
        .then(
          Commands.literal("remove")
            .executes(context -> {
              if (!context.getSource().isPlayer()) {
                CobbleUtils.LOGGER.info("Only players can claim rewards!");
                return 0;
              }
              Player player = context.getSource().getPlayerOrException();
              RewardsUtils.removeRewards(player);
              return 1;
            })
            .then(Commands.argument("player", EntityArgument.player())
              .executes(context -> {
                Player player = EntityArgument.getPlayer(context, "player");
                RewardsUtils.removeRewards(player);
                return 1;
              })
            )
        )
    );
  }

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return 1;
  }
}
