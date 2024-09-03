package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import dev.architectury.platform.Platform;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.PermissionNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public abstract class LuckPermsUtil {

  private static Permission PERMISSION_TYPE;

  private enum Permission {
    LUCKPERMS,
    SPIGOT,
    FORGE,
    FABRIC,
    NONE,
  }

  private static void setup() {
    if (PERMISSION_TYPE != null) return;
    if (getLuckPermsApi() != null) {
      PERMISSION_TYPE = Permission.LUCKPERMS;
      CobbleUtils.LOGGER.info("LuckPerms detected");
    } else if (WebSocketClient.getInstance() != null) {
      PERMISSION_TYPE = Permission.SPIGOT;
      CobbleUtils.LOGGER.info("WebSocket permissions detected");
    } else if (Platform.isForge()) {
      Permission
        PERMISSION_TYPE = Permission.FORGE;
      CobbleUtils.LOGGER.error("Forge permission system not implemented");
    } else if (Platform.isFabric()) {
      PERMISSION_TYPE = Permission.FABRIC;
      CobbleUtils.LOGGER.error("Fabric permission system not implemented");
    } else {
      PERMISSION_TYPE = Permission.NONE;
      CobbleUtils.LOGGER.error("No permission system detected");
    }
  }

  private static LuckPerms getLuckPermsApi() {
    try {
      return LuckPermsProvider.get();
    } catch (IllegalStateException | NullPointerException | NoClassDefFoundError e) {
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.error("LuckPerms is not present");
      }
      return null;
    }
  }

  public static boolean checkPermission(ServerCommandSource source, int level, List<String> permissions) {
    setup();
    ServerPlayerEntity player = source.getPlayer();
    if (player == null) {
      return source.hasPermissionLevel(level);
    }

    boolean hasPermission = source.hasPermissionLevel(level);
    if (hasPermission) return true;

    return switch (PERMISSION_TYPE) {
      case LUCKPERMS -> checkLuckPermsPermission(player, permissions);
      case SPIGOT -> checkSpigotPermissions(player, permissions);
      default -> hasPermission;
    };
  }

  private static boolean checkLuckPermsPermission(ServerPlayerEntity player, List<String> permissions) {
    LuckPerms luckPermsApi = getLuckPermsApi();
    if (luckPermsApi == null) {
      return false;
    }

    UserManager userManager = luckPermsApi.getUserManager();
    User user = userManager.getUser(player.getUuid());

    if (user == null) {
      return false;
    }

    for (String permission : permissions) {
      if (permission == null || permission.isEmpty()) return true;
      addPermission(permission);
      if (user.getCachedData().getPermissionData().checkPermission(permission).asBoolean()) {
        return true;
      }
    }
    return false;
  }

  private static boolean checkSpigotPermissions(ServerPlayerEntity player, List<String> permissions) {
    boolean hasPermission = player.hasPermissionLevel(4);
    for (String permission : permissions) {
      if (permission == null || permission.isEmpty()) return true;
      addPermission(permission);

      hasPermission = WebSocketClient.getInstance().checkPermission(player, permission).join();
      if (hasPermission) return true;

    }
    return hasPermission;
  }

  public static boolean checkPermission(ServerCommandSource source, int level, String permission) {
    return checkPermission(source, level, List.of(permission));
  }

  public static boolean checkPermission(ServerPlayerEntity player, String permission) {
    setup();
    if (permission == null || permission.isEmpty()) return true;

    if (player != null && player.hasPermissionLevel(4)) return true;

    return switch (PERMISSION_TYPE) {
      case LUCKPERMS -> checkLuckPermsPermission(player, List.of(permission));
      case SPIGOT -> WebSocketClient.getInstance().checkPermission(player, permission).join();
      default -> false;
    };
  }

  public static void addPermission(String permission) {
    setup();
    switch (PERMISSION_TYPE) {
      case LUCKPERMS:
        LuckPerms luckPermsApi = getLuckPermsApi();
        PermissionNode.builder(permission).build();
        if (luckPermsApi != null) {
          luckPermsApi.getNodeBuilderRegistry().forPermission().permission(permission).build();
        }
        break;
      case SPIGOT:
        WebSocketClient.getInstance().addPermission(permission);
        break;
      case NONE:
      default:

        break;
    }
  }

  public static boolean hasOp(ServerPlayerEntity player) {
    setup();
    return player.hasPermissionLevel(4);
  }
}
