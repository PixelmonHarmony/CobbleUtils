package com.kingpixel.cobbleutils.command.admin;

import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * @author Carlos Varas Alonso - 07/11/2024 4:18
 */
public class WikiCommand {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base, String url) {
    dispatcher.register(
      base
        .then(
          CommandManager.literal("wiki")
            .executes(context -> {
              if (!context.getSource().isExecutedByPlayer()) {
                return 0;
              }
              PlayerUtils.sendMessage(context.getSource().getPlayer(), url);
              return 1;
            })
        )
    );
  }
}
