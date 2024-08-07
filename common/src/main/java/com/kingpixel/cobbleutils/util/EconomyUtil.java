package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.impactdev.impactor.api.economy.transactions.EconomyTransaction;
import net.kyori.adventure.key.Key;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 06/08/2024 11:50
 */
public class EconomyUtil {


  // The impactor service
  private static EconomyService service = EconomyService.instance();


  public static Account getAccount(UUID uuid, String c) {
    if (!service.hasAccount(uuid).join()) {
      return service.account(uuid).join();
    }
    Currency currency = Currency.builder().key(Key.key(c)).build();
    return service.account(currency, uuid).join();
  }


  /**
   * Method to add to the balance of an account.
   *
   * @param account The account to add the balance to.
   * @param amount  The amount to add.
   *
   * @return true if the transaction was successful.
   */
  public static boolean add(Account account, double amount) {
    EconomyTransaction transaction = account.deposit(new BigDecimal(amount));
    return transaction.successful();
  }

  /**
   * Method to remove a balance from an account.
   *
   * @param account The account to remove the balance from.
   * @param amount  The amount to remove from the account.
   *
   * @return true if the transaction was successful.
   */
  public static boolean remove(Account account, double amount) {
    EconomyTransaction transaction = account.withdraw(new BigDecimal(amount));
    return transaction.successful();
  }

  /**
   * Method to check if an account has enough balance and optionally remove the amount.
   *
   * @param account The account to check.
   * @param amount  The amount to check for.
   *
   * @return true if the account has enough balance.
   */
  public static boolean hasEnough(Account account, double amount) {
    if (CobbleUtils.config.isDebug()) {
      CobbleUtils.LOGGER.info("Checking if account has enough money: ");
      CobbleUtils.LOGGER.info("Account: " + account.owner());
      CobbleUtils.LOGGER.info("Amount: " + amount);
    }
    if (account.balance().compareTo(new BigDecimal(amount)) >= 0) {
      remove(account, amount);
      CobbleUtils.server.getPlayerList().getPlayer(account.owner()).sendSystemMessage(AdventureTranslator.toNative(
        CobbleUtils.language.getMessageBought()
          .replace("%price%", String.valueOf(amount))
          .replace("%bal%", account.balance().toString())
          .replace("%prefix%", CobbleUtils.language.getPrefixShop())
      ));
      return true;
    } else {
      CobbleUtils.server.getPlayerList().getPlayer(account.owner()).sendSystemMessage(AdventureTranslator.toNative(
        CobbleUtils.language.getMessageNotHaveMoney()
          .replace("%price%", String.valueOf(amount))
          .replace("%bal%", account.balance().toString())
          .replace("%prefix%", CobbleUtils.language.getPrefixShop())
      ));
      return false;
    }
  }

  public static boolean hasEnough(ServerPlayer player, String currency, double amount) {
    return hasEnough(getAccount(player.getUUID(), currency), amount);
  }

}
