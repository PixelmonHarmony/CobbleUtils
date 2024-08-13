package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.impactdev.impactor.api.economy.transactions.EconomyTransaction;
import net.kyori.adventure.key.Key;
import net.minecraft.server.network.ServerPlayerEntity;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 06/08/2024 11:50
 */
public abstract class EconomyUtil {

  // The impactor service
  private static EconomyService service = EconomyService.instance();

  public static boolean isImpactorPresent() {
    try {
      Class.forName("net.impactdev.impactor.api.Impactor");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static boolean isVaultApi() {
    try {
      Class.forName("net.milkbowl.vault.economy.Economy");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static Account getAccount(UUID uuid, String c) {
    if (!service.hasAccount(uuid).join()) {
      return service.account(uuid).join();
    }
    Currency currency = Currency.builder().key(Key.key(c)).build();
    return service.account(currency, uuid).join();
  }

  public static Account getAccount(UUID uuid) {
    if (!service.hasAccount(uuid).join()) {
      return service.account(uuid).join();
    }
    return service.account(uuid).join();
  }

  /**
   * Method to add to the balance of an account.
   *
   * @param player   The account to add the balance to.
   * @param currency The currency to add the balance to.
   * @param amount   The amount to add.
   *
   * @return true if the transaction was successful.
   */
  public static boolean addMoney(ServerPlayerEntity player, String currency, BigDecimal amount) {
    if (isImpactorPresent()) {
      Account account;
      if (currency.isEmpty()) {
        account = getAccount(player.getUuid());
      } else {
        account = getAccount(player.getUuid(), currency);
      }
      EconomyTransaction transaction = account.deposit(amount);
      return transaction.successful();
    } else if (isVaultApi()) {
      return false;
    } else {
      return false;
    }
  }

  /**
   * Method to remove a balance from an account.
   *
   * @param player   The player to remove the balance from.
   * @param amount   The amount to remove from the account.
   * @param currency The currency to remove the balance from.
   *
   * @return true if the transaction was successful.
   */
  public static boolean removeMoney(ServerPlayerEntity player, String currency, BigDecimal amount) {
    if (isImpactorPresent()) {
      Account account;
      if (currency.isEmpty()) {
        account = getAccount(player.getUuid());
      } else {
        account = getAccount(player.getUuid(), currency);
      }
      EconomyTransaction transaction = account.withdraw(amount);
      return transaction.successful();
    } else if (isVaultApi()) {
      return false;
    } else {
      return false;
    }
  }

  public static boolean removeMoney(Account account, BigDecimal amount) {
    EconomyTransaction transaction = account.withdraw(amount);
    return transaction.successful();
  }

  /**
   * Method to check if an account has enough balance and optionally remove the
   * amount.
   *
   * @param account The account to check.
   * @param amount  The amount to check for.
   *
   * @return true if the account has enough balance.
   */
  public static boolean hasEnoughImpactor(Account account, BigDecimal amount) {
    if (account.balance().compareTo(amount) >= 0) {
      removeMoney(account, amount);
      CobbleUtils.server.getPlayerManager().getPlayer(account.owner()).sendMessage(AdventureTranslator.toNative(
        CobbleUtils.language.getMessageBought()
          .replace("%price%", String.valueOf(amount))
          .replace("%bal%", account.balance().toString())
          .replace("%prefix%", CobbleUtils.language.getPrefixShop())));
      return true;
    } else {
      CobbleUtils.server.getPlayerManager().getPlayer(account.owner()).sendMessage(
        AdventureTranslator.toNative(
          CobbleUtils.language.getMessageNotHaveMoney()
            .replace("%price%", String.valueOf(amount))
            .replace("%bal%", account.balance().toString())
            .replace("%prefix%", CobbleUtils.language.getPrefixShop())));
      return false;
    }
  }

  public static boolean hasEnough(ServerPlayerEntity player, BigDecimal amount) {
    if (isImpactorPresent()) {
      return hasEnoughImpactor(getAccount(player.getUuid()), amount);
    } else if (isVaultApi()) {
      return false;
    } else {
      return false;
    }
  }

  public static boolean hasEnough(ServerPlayerEntity player, String currency, BigDecimal amount) {
    if (isImpactorPresent()) {
      return hasEnoughImpactor(getAccount(player.getUuid(), currency), amount);
    } else if (isVaultApi()) {
      return false;
    } else {
      return false;
    }
  }

}
