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
      return LuckPermsProvider.get() != null;
    } catch (IllegalStateException | NullPointerException e) {
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
    for (String permission : permissions) {
      addPermission(permission);
    }
    ServerPlayerEntity player = source.getPlayer();
    if (player != null) {
      boolean hasPermission = source.hasPermissionLevel(level);
      if (hasPermission) return true;
      if (isLuckPermsPresent()) {
        LuckPerms luckPermsApi = getLuckPermsApi();
        for (String permission : permissions) {
          User user = luckPermsApi.getUserManager().getUser(player.getUuid());
          if (user != null) {
            boolean restult = user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
            if (CobbleUtils.config.isDebug()) {
              CobbleUtils.LOGGER.info("Checking permission: " + permission);
              CobbleUtils.LOGGER.info("Result: " + restult);
            }
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
    addPermission(permission);
    ServerPlayerEntity player = source.getPlayer();
    if (player != null) {
      boolean hasPermission = source.hasPermissionLevel(level);
      if (hasPermission) return true;
      if (isLuckPermsPresent()) {
        if (permission.isEmpty()) return true;
        LuckPerms luckPermsApi = getLuckPermsApi();
        if (luckPermsApi != null) {
          UserManager userManager = luckPermsApi.getUserManager();
          User user = userManager.getUser(player.getUuid());
          if (user != null) {
            boolean restult = user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
            if (CobbleUtils.config.isDebug()) {
              CobbleUtils.LOGGER.info("Checking permission: " + permission);
              CobbleUtils.LOGGER.info("Result: " + restult);
            }
            hasPermission = restult;
            if (hasPermission) return true;
          }
        }
      }
      return hasPermission;
    }
    return source.hasPermissionLevel(level) || source.getEntity() == null;
  }

  public static boolean checkPermission(ServerPlayerEntity player, String permission) {
    if (permission.isEmpty()) return true;
    if (player.hasPermissionLevel(4)) return true;
    if (isLuckPermsPresent()) {
      addPermission(permission);
      LuckPerms luckPermsApi = getLuckPermsApi();
      if (luckPermsApi != null) {
        UserManager userManager = luckPermsApi.getUserManager();
        User user = userManager.getUser(player.getUuid());
        if (user != null) {
          return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        }
      }
    }
    return false;
  }

  public static void addPermission(String permission) {
    if (isLuckPermsPresent()) {
      getLuckPermsApi().getNodeBuilderRegistry().forPermission().permission(permission).build();
    }
  }

}
