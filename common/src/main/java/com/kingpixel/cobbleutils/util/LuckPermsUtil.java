package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public abstract class LuckPermsUtil {

  private static Permission PERMISSION_TYPE;

  private enum Permission {
    LUCKPERMS,
    FABRIC_PERMISSIONS_API,
    FORGE_PERMISSIONS_API,
    NONE
  }

  private static void setup() {
    if (PERMISSION_TYPE != null) return;
    if (haveFabricPermissionsApi()) {
      PERMISSION_TYPE = Permission.FABRIC_PERMISSIONS_API;
      CobbleUtils.LOGGER.info("Fabric permissions detected");
    } else if (haveForgePermissionApi()) {
      PERMISSION_TYPE = Permission.FORGE_PERMISSIONS_API;
      CobbleUtils.LOGGER.info("Forge permissions detected");
    } else if (getLuckPermsApi() != null) {
      PERMISSION_TYPE = Permission.LUCKPERMS;
      CobbleUtils.LOGGER.info("LuckPerms detected");
    } else {
      CobbleUtils.LOGGER.error("No permission system detected");
      PERMISSION_TYPE = Permission.NONE;
    }
  }

  private static LuckPerms getLuckPermsApi() {
    try {
      return LuckPermsProvider.get();
    } catch (IllegalStateException | NullPointerException | NoClassDefFoundError e) {
      return null;
    }
  }

  private static boolean haveForgePermissionApi() {
    return false;

  }

  private static boolean haveFabricPermissionsApi() {
    try {
      return Permissions.class != null;
    } catch (NoClassDefFoundError e) {
      return false;
    }
  }

  public static boolean checkPermission(ServerCommandSource source, int level, List<String> permissions) {
    setup();
    ServerPlayerEntity player = source.getPlayer();
    if (player == null) return source.hasPermissionLevel(level);

    boolean hasPermission = source.hasPermissionLevel(level);
    if (hasPermission) return true;

    return switch (PERMISSION_TYPE) {
      case LUCKPERMS -> checkLuckPermsPermission(player, permissions);
      case FABRIC_PERMISSIONS_API -> checkFabricPermissions(source, level, permissions);
      default -> hasPermission;
    };
  }

  private static boolean checkFabricPermissions(ServerCommandSource source, int level, List<String> permissions) {
    for (String permission : permissions) {
      if (permission.isEmpty()) return true;
      if (Permissions.check(source, permission, level)) return true;
    }
    return false;
  }


  public static boolean checkLuckPermsPermission(ServerPlayerEntity player, List<String> permissions) {
    LuckPerms luckPermsApi = getLuckPermsApi();
    if (luckPermsApi == null) return false;
    UserManager userManager = luckPermsApi.getUserManager();
    User user = userManager.getUser(player.getUuid());

    if (user == null) return false;

    for (String permission : permissions) {
      if (permission == null || permission.isEmpty()) return true;
      //addPermission(permission);
      return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
    return false;
  }


  public static boolean checkPermission(ServerCommandSource source, int level, String permission) {
    return checkPermission(source, level, List.of(permission));
  }

  public static boolean checkPermission(ServerPlayerEntity player, String permission) {
    setup();
    if (permission == null || permission.isEmpty()) return true;
    if (player == null) return false;
    if (player.hasPermissionLevel(4)) return true;

    return switch (PERMISSION_TYPE) {
      case LUCKPERMS -> checkLuckPermsPermission(player, List.of(permission));
      case FABRIC_PERMISSIONS_API -> Permissions.check(player, permission, 4);
      default -> false;
    };
  }

  public static boolean hasOp(ServerPlayerEntity player) {
    setup();
    return player.hasPermissionLevel(4);
  }
}
