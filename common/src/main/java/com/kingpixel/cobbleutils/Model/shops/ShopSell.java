package com.kingpixel.cobbleutils.Model.shops;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.EconomyUtil;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Carlos Varas Alonso - 25/08/2024 23:32
 */
@Getter
@Setter
@ToString
public class ShopSell {
  public static Map<String, Set<Shop.Product>> products = new HashMap<>();

  public ShopSell() {
    products = new HashMap<>();
  }

  public ShopSell(Map<String, Set<Shop.Product>> products) {
    this.products = products;
  }

  /**
   * Adds products from the shop to the global product list, updating existing products.
   *
   * @param shop The shop whose products are to be added.
   */
  public static void addProduct(Shop shop) {
    String currency = shop.getCurrency();
    Set<Shop.Product> productSet = products.computeIfAbsent(currency, k -> new HashSet<>());

    for (Shop.Product product : shop.getProducts()) {
      if (product.getItemchance().getItem().startsWith("pokemon:")) continue;
      if (product.getSell().compareTo(BigDecimal.ZERO) > 0) {
        // Remove old product if it exists
        productSet.removeIf(p -> ItemStack.canCombine(p.getItemchance().getItemStack(),
          product.getItemchance().getItemStack()));

        // Add or update the new product
        productSet.add(product);
      }
    }
  }

  /**
   * Sells all products from the player's inventory and updates the player's balance.
   *
   * @param player The player who is selling the products.
   */
  public static void sellProducts(ServerPlayerEntity player) {
    PlayerInventory inventory = player.getInventory();

    // Map to store the total money earned by each currency
    Map<String, BigDecimal> currencyTotals = new HashMap<>();

    // Iterate over each currency and its products
    products.forEach((currency, productSet) -> {
      BigDecimal currencyTotal = BigDecimal.ZERO;

      for (Shop.Product product : productSet) {
        if (!LuckPermsUtil.checkPermission(player, product.getPermission())) continue;
        if (product.getSell().setScale(EconomyUtil.getDecimals(currency), RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) == 0)
          continue;
        ItemStack productStack = product.getItemchance().getItemStack();

        for (ItemStack itemStack : inventory.main) {
          if (itemStack.isEmpty()) continue;
          if (ItemStack.canCombine(itemStack, productStack)) {
            int amount = itemStack.getCount();
            BigDecimal price =
              product.getSell().setScale(EconomyUtil.getDecimals(currency), RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(amount));
            currencyTotal = currencyTotal.setScale(EconomyUtil.getDecimals(currency), RoundingMode.HALF_UP).add(price);
            itemStack.decrement(amount);
          }
        }
      }

      // Update total for the currency if any products were sold
      if (currencyTotal.setScale(EconomyUtil.getDecimals(currency), RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) > 0) {
        currencyTotals.put(currency, currencyTotal);
      }
    });

    // Send detailed message to the player if any money was earned
    if (!currencyTotals.isEmpty()) {
      StringBuilder message = new StringBuilder(CobbleUtils.shopLang.getMessageSell());
      StringBuilder currencyMessage = new StringBuilder();
      currencyTotals.forEach((currency, total) -> {
        currencyMessage.append(String.format("\n &6%s &a%s,", total, currency));
        EconomyUtil.addMoney(player, currency, total);
      });
      message = new StringBuilder(message.toString().replace("%currencys%", currencyMessage.toString()));

      // Remove the last comma and send the message
      String finalMessage = message.substring(0, message.length() - 1);
      player.sendMessage(AdventureTranslator.toNative(finalMessage));
    }
  }

  /**
   * Sells the product currently held in the player's main hand.
   *
   * @param player The player who is selling the product.
   */
  public static void sellProductHand(ServerPlayerEntity player) {
    PlayerInventory inventory = player.getInventory();
    ItemStack mainHandStack = inventory.getMainHandStack();

    if (mainHandStack.isEmpty()) {
      player.sendMessage(AdventureTranslator.toNative("&cYou are not holding any item to sell."));
      return;
    }

    boolean sold = false;
    String currencyUsed = null;
    BigDecimal totalEarned = BigDecimal.ZERO;

    // Find the currency for the item in the hand
    for (Map.Entry<String, Set<Shop.Product>> entry : products.entrySet()) {
      String currency = entry.getKey();
      Set<Shop.Product> productSet = entry.getValue();

      for (Shop.Product product : productSet) {
        if (!LuckPermsUtil.checkPermission(player, product.getPermission())) continue;
        if (product.getSell().setScale(EconomyUtil.getDecimals(currency), RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) == 0)
          continue;
        ItemStack productStack = product.getItemchance().getItemStack();

        if (ItemStack.canCombine(mainHandStack, productStack)) {
          int amount = mainHandStack.getCount();
          BigDecimal price = product.getSell().multiply(BigDecimal.valueOf(amount));
          EconomyUtil.addMoney(player, currency, price);
          mainHandStack.decrement(amount);
          totalEarned = totalEarned.add(price);
          currencyUsed = currency;
          sold = true;
          break;
        }
      }
      if (sold) break;
    }

    if (sold) {
      player.sendMessage(AdventureTranslator.toNative(
        String.format("&aYou have sold your item for %s %s.", totalEarned, currencyUsed)));
    } else {
      player.sendMessage(AdventureTranslator.toNative("&cThe item you are holding cannot be sold."));
    }
  }
}
