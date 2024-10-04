package com.kingpixel.cobbleutils.features.shops;

import com.kingpixel.cobbleutils.command.base.shops.ShopCommand;
import com.kingpixel.cobbleutils.config.ShopConfig;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 28/09/2024 20:15
 */
public class ShopApi {
  public static void register(String modid, List<String> commands,
                              ShopConfig shopConfig,
                              CommandDispatcher<ServerCommandSource> dispatcher,
                              boolean active) {
    if (active) {
      for (String command : commands) {
        LiteralArgumentBuilder<ServerCommandSource> shopliteral =
          CommandManager.literal(command)
            .then(
              CommandManager.literal("shop")
                .requires(source -> LuckPermsUtil.checkPermission(
                  source, 2, List.of(modid + ".admin", modid + ".shop",
                    modid + ".user")
                ))
            );
        ShopCommand.register(dispatcher, shopliteral, shopConfig, modid, true);
        //ShopTransactionCommand.register(dispatcher, shopliteral);
        //ShopSellCommand.register(dispatcher, CommandManager.literal("sell"));
      }
    }
  }
}
