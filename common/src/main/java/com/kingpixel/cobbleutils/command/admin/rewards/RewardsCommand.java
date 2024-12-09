package com.kingpixel.cobbleutils.command.admin.rewards;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 28/06/2024 10:51
 */
public class RewardsCommand implements Command<ServerCommandSource> {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .then(
          CommandManager.literal("save")
            .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.storage_rewards.save", "cobbleutils" +
              ".admin")))
            .then(
              CommandManager.argument("player", EntityArgumentType.player())
                .then(
                  CommandManager.literal("command")
                    .then(
                      CommandManager.argument("command", StringArgumentType.greedyString())
                        .executes(new RewardsCommand()))))));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
    String command = StringArgumentType.getString(context, "command");
    if (RewardsUtils.saveRewardCommand(player, command)) {
      if (context.getSource().isExecutedByPlayer()) {
        context.getSource().getPlayer().sendMessage(Text.literal("Command saved!"));
      } else {
        CobbleUtils.LOGGER.info("Command saved!");
      }
    }
    return 1;
  }
}
