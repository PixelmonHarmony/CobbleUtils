package com.kingpixel.cobbleutils.command.admin.rewards;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.party.ui.RewardsUI;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author Carlos Varas Alonso - 12/06/2024 3:47
 */
public class Rewards implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base
        .executes(new Rewards())
        .then(
          Commands.literal("other")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("player", EntityArgument.player())
                .executes(new Rewards())
            )
        )
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    ServerPlayer player = context.getSource().getPlayerOrException();
    ServerPlayer target = null;
    try {
      target = EntityArgument.getPlayer(context, "player");
    } catch (Exception e) {
      if (!context.getSource().isPlayer()) {
        CobbleUtils.LOGGER.info("You must was a player to execute this command");
        return 0;
      }
    }
    if (target != null) {
      if (RewardsUtils.hasRewards(target)) {
        UIManager.openUIForcefully(target, RewardsUI.getRewards(target));
      } else {
        target.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.language.getMessageNotHaveRewards()));
      }
      return 1;
    }
    if (RewardsUtils.hasRewards(player)) {
      UIManager.openUIForcefully(player, RewardsUI.getRewards(player));
    } else {
      player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.language.getMessageNotHaveRewards()));
    }
    return 1;
  }

}
