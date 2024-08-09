package com.kingpixel.cobbleutils.managers;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.permission.CobblemonPermission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.kingpixel.cobbleutils.CobbleUtils;
import net.minecraft.commands.CommandSourceStack;

/**
 * @author Carlos Varas Alonso - 10/05/2024 20:37
 */
public class CobbleUtilsPermission {

  public final CobblemonPermission USER_PERMISSION;
  public final CobblemonPermission POKESHOUT_PERMISSION;
  public final CobblemonPermission POKESHOUTALL_PERMISSION;
  public final CobblemonPermission INSTA_BREED_PERMISSION;
  public final CobblemonPermission HATCH_PERMISSION;

  public CobbleUtilsPermission() {
    this.USER_PERMISSION = new CobblemonPermission("cobbleutils.user",
      toPermLevel(CobbleUtils.permissionConfig.permissionLevels.COMMAND_USER_PERMISSION_LEVEL));
    this.POKESHOUT_PERMISSION = new CobblemonPermission("cobbleutils.pokeshout",
      toPermLevel(CobbleUtils.permissionConfig.permissionLevels.COMMAND_POKESHOUT_PERMISSION_LEVEL));
    this.POKESHOUTALL_PERMISSION = new CobblemonPermission("cobbleutils.pokeshoutall",
      toPermLevel(CobbleUtils.permissionConfig.permissionLevels.COMMAND_POKESHOUTALL_PERMISSION_LEVEL));
    this.INSTA_BREED_PERMISSION = new CobblemonPermission("cobbleutils.instabreed",
      toPermLevel(CobbleUtils.permissionConfig.permissionLevels.COMMAND_INSTA_BREED_PERMISSION_LEVEL));
    this.HATCH_PERMISSION = new CobblemonPermission("cobbleutils.hatch",
      toPermLevel(CobbleUtils.permissionConfig.permissionLevels.COMMAND_HATCH_PERMISSION_LEVEL));


  }

  public PermissionLevel toPermLevel(int permLevel) {
    for (PermissionLevel value : PermissionLevel.values()) {
      if (value.ordinal() == permLevel) {
        return value;
      }
    }
    return PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS;
  }

  public static boolean checkPermission(CommandSourceStack source, CobblemonPermission permission) {
    return Cobblemon.INSTANCE.getPermissionValidator().hasPermission(source, permission);
  }
}

