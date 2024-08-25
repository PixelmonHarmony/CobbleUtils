package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.PermissionNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 10/08/2024 17:50
 */
public abstract class LuckPermsUtil {
  public static LuckPerms getLuckPermsApi() {
    try {
      return LuckPermsProvider.get();
    } catch (IllegalStateException | NullPointerException | NoClassDefFoundError e) {
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.error("LuckPerms is not present");
      }
      e.printStackTrace();
      return null;
    }
  }

  public static boolean checkPermission(ServerCommandSource source, int level, List<String> permissions) {
    ServerPlayerEntity player = source.getPlayer();
    if (player != null) {
      boolean hasPermission = source.hasPermissionLevel(level);
      if (hasPermission) return true;
      LuckPerms luckPermsApi = getLuckPermsApi();
      if (luckPermsApi != null) {
        for (String permission : permissions) {
          User user = luckPermsApi.getUserManager().getUser(player.getUuid());
          if (user != null) {
            boolean restult = user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
            hasPermission = restult;
            if (hasPermission) return true;
          }
        }
      }
      return hasPermission;
    }
    return source.hasPermissionLevel(level) || source.getEntity() == null;
  }


  public static boolean checkPermission(ServerCommandSource source, int level, String permission) {
    ServerPlayerEntity player = source.getPlayer();
    if (player != null) {
      boolean hasPermission = source.hasPermissionLevel(level);
      if (hasPermission) return true;
      LuckPerms luckPermsApi = getLuckPermsApi();
      if (luckPermsApi != null) {
        if (permission.isEmpty()) return true;
        UserManager userManager = luckPermsApi.getUserManager();
        User user = userManager.getUser(player.getUuid());
        if (user != null) {
          boolean restult = user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
          hasPermission = restult;
          if (hasPermission) return true;
        }
      }
      return hasPermission;
    }
    return source.hasPermissionLevel(level) || source.getEntity() == null;
  }

  public static boolean checkPermission(ServerPlayerEntity player, String permission) {
    if (permission.isEmpty()) return true;
    if (player.hasPermissionLevel(4)) return true;
    LuckPerms luckPermsApi = getLuckPermsApi();
    if (luckPermsApi != null) {
      UserManager userManager = luckPermsApi.getUserManager();
      User user = userManager.getUser(player.getUuid());
      if (user != null) {
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
      }
    }
    return false;
  }

  public static void addPermission(String permission) {
    LuckPerms luckPermsApi = getLuckPermsApi();
    if (luckPermsApi != null) {
      PermissionNode permissionNode = PermissionNode.builder(permission).build();
      getLuckPermsApi().getNodeBuilderRegistry().forPermission().permission(permission).build();
    }
    try {

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static boolean hasOp(ServerPlayerEntity player) {
    return player.hasPermissionLevel(4);
  }

}
