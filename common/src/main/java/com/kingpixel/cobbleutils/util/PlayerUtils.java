package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import net.minecraft.world.entity.player.Player;

import java.util.Date;

/**
 * @author Carlos Varas Alonso - 28/06/2024 20:44
 */
public class PlayerUtils {
  public static void sendMessage(Player player, String message) {
    player.sendSystemMessage(AdventureTranslator.toNativeWithOutPrefix(message));
  }

  public static void broadcast(String message) {
    CobbleUtils.server.getPlayerList().getPlayers().forEach(player -> sendMessage(player, message));
  }

  public static String getCooldown(Date date) {
    long time = date.getTime() - new Date().getTime();
    long seconds = time / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;
    return days + "d " + hours % 24 + "h " + minutes % 60 + "m " + seconds % 60 + "s";
  }
}
