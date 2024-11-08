package com.kingpixel.cobbleutils.api;

import com.kingpixel.cobbleutils.util.EconomyUtil;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * @author Carlos Varas Alonso - 05/11/2024 23:58
 */
public class EconomyApi {
  /**
   * Add money to the player
   *
   * @param player  The player to add the money
   * @param money   The amount of money
   * @param curreny The currency to add
   */
  public static void addMoney(ServerPlayerEntity player, BigDecimal money, @Nonnull String curreny) {
    EconomyUtil.addMoney(player, curreny, money);
  }

  public static void removeMoney(ServerPlayerEntity player, BigDecimal money, @Nonnull String curreny) {
    EconomyUtil.removeMoney(player, curreny, money);
  }

  public static BigDecimal getMoney(ServerPlayerEntity player, @Nonnull String curreny) {
    return EconomyUtil.getBalance(player, curreny);
  }

  public static void setMoney(ServerPlayerEntity player, BigDecimal money, @Nonnull String curreny) {
  }

  public static String formatMoney(ServerPlayerEntity player, BigDecimal money, @Nonnull String currency) {
    return EconomyUtil.formatCurrency(money, currency, player.getUuid());
  }

  public static boolean hasEnoughMoney(ServerPlayerEntity player, BigDecimal money, @Nonnull String currency) {
    return EconomyUtil.hasEnough(player, currency, money);
  }
}
