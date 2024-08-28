package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.Breeding;
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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
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

  public static String getBalance(ServerPlayerEntity player, String currency, int digits) {
    // Supongamos que obtienes el balance como un BigDecimal desde algún método
    BigDecimal balance = getBalance(player, currency);

    if (balance != null) {
      // Redondear el balance al número de dígitos decimales especificados
      balance = balance.setScale(digits, RoundingMode.HALF_UP);
      // Devolver como cadena de texto
      return formatCurrency(balance, currency, player.getUuid());
    }

    // En caso de que el balance sea null, retornas una cadena vacía o algún valor por defecto.
    return "0.00";
  }

  public static int getDecimals(String currency) {
    setEconomyType();
    return switch (economyType) {
      case IMPACTOR -> getCurrency(currency).decimals();
      case VAULT -> 2;
      default -> 2;
    };
  }

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
        if (transaction.successful()) {
          CobbleUtils.server.getPlayerManager().getPlayer(account.owner()).sendMessage(
            AdventureTranslator.toNative(
              CobbleUtils.shopLang.getMessageAddMoney()
                .replace("%prefix%", CobbleUtils.shopLang.getPrefix())
                .replace("%price%", String.valueOf(amount))
                .replace("%balance%", formatCurrency(account.balance(), account.currency(), account.owner()))
                .replace("%symbol%", getSymbol(account.currency()))
                .replace("%currency%", getCurrencyName(account.currency()))
            )
          );
          return true;
        }
        return false;
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
      sendMessage(account, amount, CobbleUtils.shopLang.getMessageBought());
      return true;
    } else {
      sendMessage(account, amount, CobbleUtils.shopLang.getMessageNotHaveMoney());
      return false;
    }
  }

  private static void sendMessage(Account account, BigDecimal amount, String messageNotHaveMoney) {
    try {
      CobbleUtils.server.getPlayerManager().getPlayer(account.owner()).sendMessage(
        AdventureTranslator.toNative(
          messageNotHaveMoney
            .replace("%prefix%", CobbleUtils.shopLang.getPrefix())
            .replace("%price%", formatCurrency(amount, account.currency(), account.owner()))
            .replace("%balance%", formatCurrency(account.balance(), account.currency(), account.owner()))
            .replace("%symbol%", getSymbol(account.currency()))
            .replace("%currency%", getCurrencyName(account.currency()))
        )
      );
    } catch (NoSuchMethodError | Exception e) {
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.error("Error sending message");
      }
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
              .replace("%bal%", formatCurrency(getBalance(player, currency), currency, player.getUuid()))
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
   * Method to format a BigDecimal to a currency string.
   *
   * @param amount   The balance to format.
   * @param currency The currency to format the balance to.
   *
   * @return The formatted balance with the format of Country host.
   */
  public static String formatCurrency(BigDecimal amount, Currency currency) {
    try {
      return formatCurrency(amount, currency, UUID.randomUUID());
    } catch (NoSuchMethodError | Exception e) {
      return formatCurrency(amount, "");
    }
  }

  /**
   * Method to format a BigDecimal to a currency string.
   *
   * @param amount   The balance to format.
   * @param currency The currency to format the balance to.
   *
   * @return The formatted balance with the format of Country host.
   */
  public static String formatCurrency(BigDecimal amount, String currency) {
    return formatCurrency(amount, currency, UUID.randomUUID());
  }

  /**
   * Method to format a BigDecimal to a currency string.
   *
   * @param amount   The balance to format.
   * @param currency The currency to format the balance to.
   * @param player   The player to get the country from.
   *
   * @return The formatted balance with the format of Country player.
   */
  public static String formatCurrency(BigDecimal amount, Currency currency, UUID player) {
    return formatCurrency(amount, getCurrencyName(currency), player);
  }

  /**
   * Method to format a BigDecimal to a currency string.
   *
   * @param amount   The balance to format.
   * @param currency The currency to format the balance to.
   * @param player   The player to get the country from.
   *
   * @return The formatted balance with the format of Country player.
   */
  public static String formatCurrency(BigDecimal amount, String currency, UUID player) {

    // Crear un NumberFormat para moneda basado en la configuración regional
    Breeding.UserInfo userinfo = null;
    if (player != null) {
      userinfo = Breeding.playerCountry.get(player);
    }

    Locale locale;
    if (userinfo == null) {
      locale = Locale.getDefault();
    } else {
      locale = new Locale(userinfo.language(), userinfo.countryCode());

    }

    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);

    // Comprobar si el NumberFormat es una instancia de DecimalFormat
    if (currencyFormatter instanceof DecimalFormat) {
      // Obtener los símbolos de formato actuales
      DecimalFormatSymbols symbols = ((DecimalFormat) currencyFormatter).getDecimalFormatSymbols();

      // Cambiar el símbolo de la moneda al que desees
      symbols.setCurrencySymbol(getSymbol(currency));
      ((DecimalFormat) currencyFormatter).setDecimalFormatSymbols(symbols);

      // Configurar el patrón de formato para determinar la posición del símbolo
      String pattern = CobbleUtils.shopLang.isSymbolBeforeAmount() ? "¤#,##0.00" : "#,##0.00 ¤";
      ((DecimalFormat) currencyFormatter).applyPattern(pattern);
    }

    // Formatear y devolver el BigDecimal como una cadena
    return currencyFormatter.format(amount);
  }

  /**
   * Method to get the currency from the impactor api.
   *
   * @param currency The currency to get.
   *
   * @return The currency.
   */
  public static Currency getCurrency(String currency) {
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
      case IMPACTOR -> getAccount(player.getUuid(), currency).balance()
        .setScale(getCurrency(currency).decimals(), RoundingMode.HALF_UP);
      case VAULT -> {
        double money = vaultEconomy.getBalance(player.getGameProfile().getName());
        yield BigDecimal.valueOf(money);
      }
      default -> BigDecimal.ZERO;
    };
  }
}
