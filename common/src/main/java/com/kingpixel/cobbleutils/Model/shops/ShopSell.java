package com.kingpixel.cobbleutils.Model.shops;

import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.EconomyUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.math.BigDecimal;
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

  public static void addProduct(Shop shop) {
    String currency = shop.getCurrency();

    Set<Shop.Product> productSet = products.computeIfAbsent(currency, k -> new HashSet<>());

    for (Shop.Product product : shop.getProducts()) {
      if (product.getSell().compareTo(BigDecimal.ZERO) > 0) {
        productSet.add(product);
      }
    }
  }

  public static void sellProducts(ServerPlayerEntity player) {
    PlayerInventory inventory = player.getInventory();

    // Mapa para almacenar el total vendido por cada currency
    Map<String, BigDecimal> currencyTotals = new HashMap<>();

    // Iterar sobre cada tipo de moneda y sus productos
    ShopSell.products.forEach((currency, products) -> {
      BigDecimal currencyTotal = BigDecimal.ZERO;

      for (Shop.Product product : products) {
        if (product.getSell().compareTo(BigDecimal.ZERO) == 0) continue;
        ItemStack productStack = product.getItemchance().getItemStack();

        for (ItemStack itemStack : inventory.main) {
          if (itemStack.isEmpty()) continue;
          if (ItemStack.canCombine(itemStack, productStack)) {
            int amount = itemStack.getCount();
            BigDecimal price = product.getSell().multiply(BigDecimal.valueOf(amount));
            currencyTotal = currencyTotal.add(price);
            itemStack.decrement(amount);
          }
        }
      }

      // Si se vendieron productos en esta currency, se actualiza el total
      if (currencyTotal.compareTo(BigDecimal.ZERO) > 0) {
        currencyTotals.put(currency, currencyTotal);
      }
    });

    // Si se obtuvieron ganancias en alguna currency, enviar un mensaje detallado al jugador
    if (!currencyTotals.isEmpty()) {
      StringBuilder message = new StringBuilder("&aYou have sold all your items for:");

      // Crear un desglose de las ganancias por cada currency
      currencyTotals.forEach((currency, total) -> {
        message.append(String.format(" &e%s %s &aCoins,", total, currency));
        EconomyUtil.addMoney(player, currency, total);
      });

      // Eliminar la última coma y enviar el mensaje
      String finalMessage = message.substring(0, message.length() - 1);
      player.sendMessage(AdventureTranslator.toNative(finalMessage));
    }


  }


  public static void sellProductHand(ItemStack mainHandStack) {
  }
}
