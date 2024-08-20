package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Date;

/**
 * @author Carlos Varas Alonso - 28/06/2024 20:44
 */
public class PlayerUtils {
  public static void sendMessage(ServerPlayerEntity player, String message) {
    player.sendMessage(AdventureTranslator.toNativeWithOutPrefix(message));
  }

  public static void broadcast(String message) {
    CobbleUtils.server.getPlayerManager().getPlayerList().forEach(player -> sendMessage(player, message));
  }

  public static String getCooldown(Date date) {
    long time = date.getTime() - new Date().getTime();
    long seconds = time / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;

    long remainingHours = hours % 24;
    long remainingMinutes = minutes % 60;
    long remainingSeconds = seconds % 60;

    StringBuilder result = new StringBuilder();

    if (days > 0) {
      String dayString = days != 1 ? CobbleUtils.language.getDays().replace("%s", String.valueOf(days))
        : CobbleUtils.language.getDay().replace("%s", String.valueOf(days));
      result.append(dayString);
    }
    if (remainingHours > 0) {
      String hourString = remainingHours != 1 ? CobbleUtils.language.getHours().replace("%s",
        String.valueOf(remainingHours))
        : CobbleUtils.language.getHour().replace("%s", String.valueOf(remainingHours));
      result.append(hourString);
    }
    if (remainingMinutes > 0) {
      String minuteString = remainingMinutes != 1 ? CobbleUtils.language.getMinutes().replace("%s",
        String.valueOf(remainingMinutes))
        : CobbleUtils.language.getMinute().replace("%s",
        String.valueOf(remainingMinutes));
      result.append(minuteString);
    }
    if (remainingSeconds > 0) {
      String secondString = remainingSeconds != 1 ? CobbleUtils.language.getSeconds().replace("%s",
        String.valueOf(remainingSeconds))
        : CobbleUtils.language.getSecond().replace("%s",
        String.valueOf(remainingSeconds));
      result.append(secondString);
    }
    if (result.isEmpty())
      return CobbleUtils.language.getNocooldown();

    return result.toString().trim();
  }

  public static ItemStack getHeadItem(ServerPlayerEntity player) {
    ItemStack itemStack = Items.PLAYER_HEAD.getDefaultStack();
    itemStack.getOrCreateNbt().putString("SkullOwner", player.getGameProfile().getName());
    return itemStack;
  }

  public static boolean isCooldown(Long cooldown) {
    return new Date().getTime() < cooldown;
  }

  public static ServerPlayerEntity castPlayer(PlayerEntity player) {
    return new ServerPlayerEntity(CobbleUtils.server, CobbleUtils.server.getOverworld(), player.getGameProfile());
  }
}
