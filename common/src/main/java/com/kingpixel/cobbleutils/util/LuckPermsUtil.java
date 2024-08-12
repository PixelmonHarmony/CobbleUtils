package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 10/08/2024 17:50
 */
public class LuckPermsUtil {
  public static boolean isLuckPermsPresent() {
    try {
      Class.forName("net.luckperms.api.LuckPerms");
      return true;
    } catch (ClassNotFoundException e) {
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.error("LuckPerms is not present");
      }
      return false;
    }
  }

  public static LuckPerms getLuckPermsApi() {
    try {
      return LuckPermsProvider.get();
    } catch (IllegalStateException | NullPointerException e) {
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.error("LuckPerms is not present");
      }
      return null;
    }
  }

  public static boolean checkPermission(ServerCommandSource source, int level, List<String> permissions) {
    ServerPlayerEntity player = source.getPlayer();
    if (player != null) {
      boolean hasPermission = source.hasPermissionLevel(level);
      if (!hasPermission && isLuckPermsPresent()) {
        LuckPerms luckPermsApi = getLuckPermsApi();
        for (String permission : permissions) {
          //addPermission(permission);
          User user = luckPermsApi.getUserManager().getUser(player.getUuid());
          if (user != null) {
            hasPermission = user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
          }
        }
      }
      return hasPermission;
    }
    return source.getEntity() == null;
  }

  public static boolean checkPermission(ServerCommandSource source, int level, String permission) {
    ServerPlayerEntity player = source.getPlayer();
    if (player != null) {
      boolean hasPermission = source.hasPermissionLevel(level);
      if (!hasPermission && isLuckPermsPresent()) {
        LuckPerms luckPermsApi = getLuckPermsApi();
        if (luckPermsApi != null) {
          UserManager userManager = luckPermsApi.getUserManager();
          if (userManager != null) {
            User user = userManager.getUser(player.getUuid());
            if (user != null) {
              hasPermission = user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
            }
          }
        }
      }
      return hasPermission;
    }
    return source.getEntity() == null;
  }


  public static void addPermission(String permission) {
    if (isLuckPermsPresent()) {
      getLuckPermsApi().getNodeBuilderRegistry().forPermission().permission(permission).negated(false).build();
    }
  }

}
