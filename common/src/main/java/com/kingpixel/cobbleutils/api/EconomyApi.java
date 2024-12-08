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

  /**
   * Remove money from the player
   *
   * @param player  The player to remove the money
   * @param money   The amount of money
   * @param curreny The currency to remove
   */
  public static void removeMoney(ServerPlayerEntity player, BigDecimal money, @Nonnull String curreny) {
    EconomyUtil.removeMoney(player, curreny, money);
  }

  /**
   * Get the money of the player
   *
   * @param player  The player to get the money
   * @param curreny The currency to get
   *
   * @return The amount of money
   */
  public static BigDecimal getMoney(ServerPlayerEntity player, @Nonnull String curreny) {
    return EconomyUtil.getBalance(player, curreny);
  }

  /**
   * Set the money of the player
   *
   * @param player  The player to set the money
   * @param money   The amount of money
   * @param curreny The currency to set
   */
  public static void setMoney(ServerPlayerEntity player, BigDecimal money, @Nonnull String curreny) {
  }

  /**
   * Format the money of the player
   *
   * @param player   The player to format the money
   * @param money    The amount of money
   * @param currency The currency to format
   *
   * @return The formatted money
   */
  public static String formatMoney(ServerPlayerEntity player, BigDecimal money, @Nonnull String currency) {
    return EconomyUtil.formatCurrency(money, currency, player.getUuid());
  }

  /**
   * Check if the player has enough money
   *
   * @param player   The player to check the money
   * @param money    The amount of money
   * @param currency The currency to check
   *
   * @return If the player has enough money
   */
  public static boolean hasEnoughMoney(ServerPlayerEntity player, BigDecimal money, @Nonnull String currency) {
    return EconomyUtil.hasEnough(player, currency, money);
  }
}
