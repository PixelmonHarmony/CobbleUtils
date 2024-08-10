package com.kingpixel.cobbleutils.command.admin.rewards;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 12/06/2024 3:47
 */
public class RewardsReload implements Command<ServerCommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .then(
          CommandManager.literal("reload")
            .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.reload", "cobbleutils" +
              ".admin")))
            .executes(context -> {
              CobbleUtils.load();
              return 1;
            })));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 1;
  }

}
