package com.kingpixel.cobbleutils.command.base.shops;

import com.kingpixel.cobbleutils.Model.shops.ShopSell;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 13/08/2024 18:53
 */
public class ShopSellCommand implements Command<ServerCommandSource> {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .then(
          CommandManager.literal("all")
            .executes(context -> {
              if (!context.getSource().isExecutedByPlayer()) {
                return 0;
              }
              ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
              ShopSell.sellProducts(player);
              return 1;
            })
        ).then(
          CommandManager.literal("hand")
            .executes(context -> {
              if (!context.getSource().isExecutedByPlayer()) {
                return 0;
              }
              ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
              ShopSell.sellProductHand(player.getInventory().getMainHandStack());
              return 1;
            })
        ).then(
          CommandManager.literal("menu")
            .executes(context -> {
              if (!context.getSource().isExecutedByPlayer()) {
                return 0;
              }
              ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

              return 0;
            })
        )
    );
  }

  @Override public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }
}
