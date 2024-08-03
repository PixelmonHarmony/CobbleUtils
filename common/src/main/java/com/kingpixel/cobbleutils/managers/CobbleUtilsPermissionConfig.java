package com.kingpixel.cobbleutils.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * @author Carlos Varas Alonso - 29/07/2024 8:14
 */
public class CobbleUtilsPermissionConfig {

  public static Gson GSON = new GsonBuilder()
    .disableHtmlEscaping()
    .setPrettyPrinting()
    .create();
  @SerializedName("permissionlevels") public PermissionLevels permissionLevels = new PermissionLevels();

  public class PermissionLevels {
    // User
    @SerializedName("cobbleutils.pokeshout") public int COMMAND_POKESHOUT_PERMISSION_LEVEL = 1;
    @SerializedName("cobbleutils.pokeshoutall") public int COMMAND_POKESHOUTALL_PERMISSION_LEVEL = 1;
    @SerializedName("cobbleutils.user") public int COMMAND_USER_PERMISSION_LEVEL = 1;
  }
}
