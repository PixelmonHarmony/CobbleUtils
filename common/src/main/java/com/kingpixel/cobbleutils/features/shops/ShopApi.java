package com.kingpixel.cobbleutils.features.shops;

import com.kingpixel.cobbleutils.command.base.shops.ShopCommand;
import com.kingpixel.cobbleutils.config.ShopConfig;
import com.mojang.brigadier.CommandDispatcher;
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
        //LiteralArgumentBuilder<ServerCommandSource> shopliteral = CommandManager.literal(command + "shop");
        ShopCommand.register(dispatcher, command, shopConfig, modid, true);
      }
    }
  }

  public static void register(String modid, List<String> commands,
                              ShopConfig shopConfig,
                              CommandDispatcher<ServerCommandSource> dispatcher,
                              boolean active, String pathShop, String pathShops) {
    if (active) {
      shopConfig.init(pathShop, modid, pathShops);
      for (String command : commands) {
        //LiteralArgumentBuilder<ServerCommandSource> shopliteral = CommandManager.literal(command + "shop");
        ShopCommand.register(dispatcher, command, shopConfig, modid, true);
      }
    }
  }
}
