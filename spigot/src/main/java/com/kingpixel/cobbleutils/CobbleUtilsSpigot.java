package com.kingpixel.cobbleutils;

import com.kingpixel.cobbleutils.websocket.WebSocketServer;
import org.bukkit.plugin.java.JavaPlugin;

public final class CobbleUtilsSpigot extends JavaPlugin {

  @Override public void onEnable() {
    WebSocketServer.init();
  }

  @Override public void onDisable() {

  }
}
