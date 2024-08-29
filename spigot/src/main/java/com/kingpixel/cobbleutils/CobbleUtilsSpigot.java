package com.kingpixel.cobbleutils;

import com.kingpixel.cobbleutils.websocket.WebSocketMain;
import org.bukkit.plugin.java.JavaPlugin;

public final class CobbleUtilsSpigot extends JavaPlugin {

  @Override public void onEnable() {
    WebSocketMain.init();
  }

  @Override public void onDisable() {

  }
}
