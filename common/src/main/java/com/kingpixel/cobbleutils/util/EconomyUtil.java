package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.impactdev.impactor.api.economy.transactions.EconomyTransaction;
import net.kyori.adventure.key.Key;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.intellij.lang.annotations.Subst;

import java.math.BigDecimal;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

/**
 * @author Carlos Varas Alonso - 06/08/2024 11:50
 */
public abstract class EconomyUtil {

  // The impactor service
  private static EconomyService service;
  // The vault economy
  private static Economy vaultEconomy;


  private static EconomyType economyType;

  private enum EconomyType {
    IMPACTOR,
    VAULT
  }

  private static void setEconomyType() {
    if (economyType != null) return;
    if (isImpactorPresent()) {
      economyType = EconomyType.IMPACTOR;
      service = EconomyService.instance();
      CobbleUtils.LOGGER.info("Impactor economy found");
    } else if (isVaultApi()) {
      economyType = EconomyType.VAULT;
      CobbleUtils.LOGGER.info("Vault economy found");
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
      EconomyService service = EconomyService.instance();
      return service != null;
    } catch (IllegalStateException | NullPointerException | NoClassDefFoundError e) {
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
      // Verifica si el servidor está utilizando Arclight y si el plugin Vault está cargado
      if (getServer().getPluginManager().getPlugin("Vault") == null) {
        CobbleUtils.LOGGER.error("Vault plugin not found.");
        return false;
      }

      // Intenta obtener el servicio de economía registrado
      RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
      if (rsp == null) {
        CobbleUtils.LOGGER.error("No Economy provider registered.");
        return false;
      }

      // Asigna el proveedor de economía
      vaultEconomy = rsp.getProvider();
      return vaultEconomy != null;
    } catch (IllegalStateException | NullPointerException | NoClassDefFoundError e) {
      CobbleUtils.LOGGER.error("Error accessing Vault API." + e);
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
      case VAULT: {
        return vaultEconomy.depositPlayer(player.getGameProfile().getName(), amount.doubleValue()).transactionSuccess();
      }
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
    setEconomyType();
    switch (economyType) {
      case IMPACTOR:
        Account account;
        if (currency.isEmpty()) {
          account = getAccount(player.getUuid());
        } else {
          account = getAccount(player.getUuid(), currency.trim());
        }
        EconomyTransaction transaction = account.withdraw(amount);
        return transaction.successful();
      case VAULT:
        return vaultEconomy.withdrawPlayer(player.getGameProfile().getName(), amount.doubleValue()).transactionSuccess();
      default:
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
              .replace("%symbol%", getSymbol(account.currency()))
              .replace("%currency%", account.currency().plural().insertion() == null
                ? "$"
                : account.currency().plural().insertion())
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
              .replace("%symbol%", getSymbol(account.currency()))
              .replace("%currency%", getCurrencyName(account.currency()))
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
        if (getBalance(player, currency).compareTo(amount) >= 0) {
          player.sendMessage(AdventureTranslator.toNative(
            CobbleUtils.language.getMessageBought()
              .replace("%price%", String.valueOf(amount))
              .replace("%bal%", getBalance(player, currency).toString())
              .replace("%symbol%", getSymbol(currency))
              .replace("%currency%", currency)
              .replace("%prefix%", CobbleUtils.language.getPrefixShop())
          ));
          vaultEconomy.withdrawPlayer(player.getGameProfile().getName(), amount.doubleValue());
          return true;
        }
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
  private static Currency getCurrency(String currency) {
    setEconomyType();
    try {
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

        case IMPACTOR -> {
          Currency c = getCurrency(currency);
          yield c.symbol().insertion() == null ? "$" : c.symbol().insertion();
        }
        case VAULT -> "$";
        default -> "$";
      };
    } catch (NoSuchMethodError | Exception ignored) {
      return "$";
    }
  }

  /**
   * Method to get the currency symbol.
   *
   * @param currency The currency to get the symbol for.
   *
   * @return The currency symbol.
   */
  public static String getSymbol(Currency currency) {
    try {
      return currency.symbol().insertion() == null ? "$" : currency.symbol().insertion();
    } catch (NoSuchMethodError | Exception | NoClassDefFoundError ignored) {
      return "$";
    }
  }

  public static String getCurrencyName(Currency currency) {
    setEconomyType();
    try {
      return switch (economyType) {
        case IMPACTOR -> currency.plural().insertion() == null ? "$" : currency.plural().insertion();
        case VAULT -> "$";
        default -> "$";
      };
    } catch (NoSuchMethodError | Exception | NoClassDefFoundError ignored) {
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
      case VAULT -> {
        double money = vaultEconomy.getBalance(player.getGameProfile().getName());
        yield BigDecimal.valueOf(money);
      }
      default -> BigDecimal.ZERO;
    };
  }
}
