package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.impactdev.impactor.api.economy.transactions.EconomyTransaction;
import net.kyori.adventure.key.Key;
import net.minecraft.server.network.ServerPlayerEntity;
import org.intellij.lang.annotations.Subst;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 06/08/2024 11:50
 */
public abstract class EconomyUtil {

  // The impactor service
  private static EconomyService service = EconomyService.instance();

  private static EconomyType economyType;

  private enum EconomyType {
    IMPACTOR,
    VAULT
  }

  private static void setEconomyType() {
    if (economyType != null) return;
    if (isImpactorPresent()) {
      economyType = EconomyType.IMPACTOR;
    } else if (isVaultApi()) {
      economyType = EconomyType.VAULT;
    } else {
      CobbleUtils.LOGGER.error("No economy api found");
      economyType = null;
    }
  }

  /**
   * Method to check if the impactor api is present.
   *
   * @return true if the api is present.
   */
  public static boolean isImpactorPresent() {
    try {
      Class.forName("net.impactdev.impactor.api.Impactor");
      return true;
    } catch (ClassNotFoundException e) {
      CobbleUtils.LOGGER.error("Impactor not found");
      return false;
    }
  }

  /**
   * Method to check if the vault api is present.
   *
   * @return true if the api is present.
   */
  public static boolean isVaultApi() {
    try {
      Class.forName("net.milkbowl.vault.economy.Economy");
      return true;
    } catch (ClassNotFoundException e) {
      CobbleUtils.LOGGER.error("Vault not found");
      return false;
    }
  }

  /**
   * Method to get an account from the impactor api.
   *
   * @param uuid     The uuid of the account.
   * @param currency The currency of the account.
   *
   * @return The account.
   */
  public static Account getAccount(UUID uuid, @Subst("") String currency) {
    if (!service.hasAccount(uuid).join()) {
      return service.account(uuid).join();
    }
    return service.account(getCurrency(currency.trim()), uuid).join();
  }

  /**
   * Method to get an account from the impactor api.
   *
   * @param uuid The uuid of the account.
   *
   * @return The account.
   */
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
  public static boolean addMoney(ServerPlayerEntity player, @Subst("") String currency, BigDecimal amount) {
    setEconomyType();
    switch (economyType) {
      case IMPACTOR: {
        Account account;
        if (currency.isEmpty()) {
          account = getAccount(player.getUuid());
        } else {
          account = getAccount(player.getUuid(), currency.trim());
        }
        EconomyTransaction transaction = account.deposit(amount);
        return transaction.successful();
      }
      case VAULT:
        return false;
      default:
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
  public static boolean removeMoney(ServerPlayerEntity player, @Subst("") String currency, BigDecimal amount) {
    if (isImpactorPresent()) {
      Account account;
      if (currency.isEmpty()) {
        account = getAccount(player.getUuid());
      } else {
        account = getAccount(player.getUuid(), currency.trim());
      }
      EconomyTransaction transaction = account.withdraw(amount);
      return transaction.successful();
    } else if (isVaultApi()) {
      return false;
    } else {
      return false;
    }
  }

  /**
   * Method to add to the balance of an account.
   *
   * @param account The account to add the balance to.
   * @param amount  The amount to add.
   *
   * @return true if the transaction was successful.
   */
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
      try {
        CobbleUtils.server.getPlayerManager().getPlayer(account.owner()).sendMessage(AdventureTranslator.toNative(
            CobbleUtils.language.getMessageBought()
              .replace("%price%", String.valueOf(amount))
              .replace("%bal%", account.balance().toString())
              //.replace("%symbol%", account.currency().symbol().toString())
              //.replace("%currency%", account.currency().plural().toString())
              .replace("%prefix%", CobbleUtils.language.getPrefixShop())
          )
        );
      } catch (NoSuchMethodError | Exception e) {
        if (CobbleUtils.config.isDebug()) {
          CobbleUtils.LOGGER.error("Error sending message");
        }
      }
      return true;
    } else {
      try {
        CobbleUtils.server.getPlayerManager().getPlayer(account.owner()).sendMessage(
          AdventureTranslator.toNative(
            CobbleUtils.language.getMessageNotHaveMoney()
              .replace("%price%", String.valueOf(amount))
              .replace("%bal%", account.balance().toString())
              //.replace("%symbol%", account.currency().symbol().toString())
              //.replace("%currency%", account.currency().plural().toString())
              .replace("%prefix%", CobbleUtils.language.getPrefixShop())
          )
        );
      } catch (NoSuchMethodError | Exception e) {
        if (CobbleUtils.config.isDebug()) {
          CobbleUtils.LOGGER.error("Error sending message");
        }
      }
      return false;
    }
  }

  /**
   * Method to check if an account has enough balance and optionally remove the
   * amount.
   *
   * @param player   The player to check.
   * @param currency The currency to check for.
   * @param amount   The amount to check for.
   *
   * @return true if the account has enough balance.
   */
  public static boolean hasEnough(ServerPlayerEntity player, @Subst("") String currency, BigDecimal amount) {
    setEconomyType();
    switch (economyType) {
      case IMPACTOR:
        return hasEnoughImpactor(getAccount(player.getUuid(), currency.trim()), amount);
      case VAULT:
        return false;
      default:
        return false;
    }
  }


  /**
   * Method to get the currency from the impactor api.
   *
   * @param currency The currency to get.
   *
   * @return The currency.
   */
  private static Currency getCurrency(@Subst("") String currency) {
    try {
      service = EconomyService.instance();
      if (currency.isEmpty()) {
        if (CobbleUtils.config.isDebug()) {
          CobbleUtils.LOGGER.error("Currency is empty");
        }
        return service.currencies().primary();
      }
      String c = currency.trim();
      return service.currencies().currency(Key.key(c)).orElseGet(() -> service.currencies().primary());
    } catch (NoSuchMethodError | Exception e) {
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.error("Error getting currency");
      }
      return service.currencies().primary();
    }
  }

  /**
   * Method to get the currency symbol.
   *
   * @param currency The currency to get the symbol for.
   *
   * @return The currency symbol.
   */
  public static String getSymbol(@Subst("") String currency) {
    setEconomyType();
    try {
      return switch (economyType) {
        case IMPACTOR -> getCurrency(currency).symbol().toString();
        case VAULT -> "$";
        default -> "$";
      };
    } catch (NoSuchMethodError | Exception ignored) {
      return "$";
    }
  }

  /**
   * Method to get the balance of an account.
   *
   * @param player   The player to get the balance for.
   * @param currency The currency to get the balance for.
   *
   * @return The balance of the account.
   */
  public static BigDecimal getBalance(ServerPlayerEntity player, @Subst("") String currency) {
    setEconomyType();
    return switch (economyType) {
      case IMPACTOR -> getAccount(player.getUuid(), currency).balance();
      case VAULT -> BigDecimal.ZERO;
      default -> BigDecimal.ZERO;
    };
  }
}
