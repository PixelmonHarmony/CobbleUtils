package com.kingpixel.cobbleutils.api;

import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 06/11/2024 23:06
 */
public class PermissionApi {


  public static boolean hasPermission(ServerPlayerEntity player, String permission, int level) {
    return LuckPermsUtil.checkPermission(player, permission);
  }


  public static boolean hasPermission(ServerPlayerEntity player, List<String> permissions, int level) {
    return LuckPermsUtil.checkPermission(player.getCommandSource(), level, permissions);
  }

  public static boolean hasPermission(ServerCommandSource source, String permission, int level) {
    return LuckPermsUtil.checkPermission(source, level, permission);
  }

  public static boolean hasPermission(ServerCommandSource source, List<String> permissions, int level) {
    return LuckPermsUtil.checkPermission(source, level, permissions);
  }
}
