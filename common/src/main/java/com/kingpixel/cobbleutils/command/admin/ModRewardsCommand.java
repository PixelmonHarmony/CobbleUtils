package com.kingpixel.cobbleutils.command.admin;

import com.kingpixel.cobbleutils.Model.ItemChance;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * @author Carlos Varas Alonso - 07/11/2024 4:18
 */
public class ModRewardsCommand {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .then(
          CommandManager.literal("modrewards")
            .executes(context -> {
              if (!context.getSource().isExecutedByPlayer()) {
                return 0;
              }
              ItemChance.ItemMod.openMenu(context.getSource().getPlayer());
              return 1;
            })
        )
    );
  }
}
