package com.kingpixel.cobbleutils.command.base.shops;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
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

import java.util.List;

/**
 * @author Carlos Varas Alonso - 13/08/2024 18:53
 */
public class ShopCommand implements Command<ServerCommandSource> {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .requires(source -> LuckPermsUtil.checkPermission(
          source, 2, List.of("cobbleutils.admin", "cobbleutils.shop",
            "cobbleutils.user")
        ))
        .executes(context -> {
          if (!context.getSource().isExecutedByPlayer()) {
            CobbleUtils.LOGGER.error("This command can only be executed by a player");
            return 0;
          }
          ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
          CobbleUtils.shopConfig.getShop().open(player);
          return 0;
        })
        .then(
          CommandManager.literal("shops")
            .requires(
              source -> LuckPermsUtil.checkPermission(
                source, 2, List.of("cobbleutils.admin", "cobbleutils.shop.shops")
              )
            )
            .then(
              CommandManager.argument("shop", StringArgumentType.string())
                .suggests((context, builder) -> {
                  CobbleUtils.shopConfig.getShop().getShops().forEach(shop -> {
                    if (context.getSource().isExecutedByPlayer()) {
                      if (LuckPermsUtil.checkPermission(
                        context.getSource(), 2, List.of("cobbleutils.admin", "cobbleutils.shop." + shop.getId())
                      )) {
                        builder.suggest(shop.getId());
                      }
                    } else {
                      builder.suggest(shop.getId());
                    }
                  });
                  return builder.buildFuture();
                })
                .executes(context -> {
                  if (!context.getSource().isExecutedByPlayer()) {
                    CobbleUtils.LOGGER.error("This command can only be executed by a player");
                    return 0;
                  }
                  ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                  String shop = StringArgumentType.getString(context, "shop");
                  CobbleUtils.shopConfig.getShop().open(player, shop);
                  return 0;
                })
                .then(
                  CommandManager.argument("player", EntityArgumentType.player())
                    .requires(source -> LuckPermsUtil.checkPermission(
                      source, 2, List.of("cobbleutils.admin", "cobbleutils.shop.other")
                    ))
                    .executes(context -> {
                      ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                      String shop = StringArgumentType.getString(context, "shop");
                      CobbleUtils.shopConfig.getShop().open(player, shop);
                      return 0;
                    })
                )
            )
        ).then(
          CommandManager.literal("reload")
            .requires(source -> LuckPermsUtil.checkPermission(
              source, 2, List.of("cobbleutils.admin", "cobbleutils.shop.reload")
            ))
            .executes(context -> {
              CobbleUtils.load();
              if (!context.getSource().isExecutedByPlayer()) {
                CobbleUtils.LOGGER.info("Reloaded");
              } else {
                context.getSource().getPlayer().sendMessage(
                  AdventureTranslator.toNative(
                    CobbleUtils.language.getMessageReload()
                      .replace("%prefix%", CobbleUtils.language.getPrefixShop())
                  )
                );
              }
              return 1;
            })
        )
    );
  }

  @Override public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }
}
