package com.kingpixel.cobbleutils.Model.shops.types;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.shops.Shop;
import com.kingpixel.cobbleutils.Model.shops.ShopDynamicData;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 27/08/2024 21:50
 */
@Getter
@Setter
@ToString
public class ShopTypeDynamicWeekly extends ShopType {
  private List<DayOfWeek> dayOfWeek;
  private int amountProducts;
  private int minutes;

  public ShopTypeDynamicWeekly() {
    super(TypeShop.WEEKLY);
    dayOfWeek = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    amountProducts = 10;
    minutes = 60;
  }

  public ShopTypeDynamicWeekly(List<DayOfWeek> dayOfWeek, int amountProducts, int minutes) {
    super(TypeShop.DYNAMIC_WEEKLY);
    this.dayOfWeek = dayOfWeek;
    this.amountProducts = amountProducts;
    this.minutes = minutes;
  }

  public ShopTypeDynamicWeekly updateShop(Shop shop) {
    // Asegúrate de que las estructuras en ShopMenu estén inicializadas
    initializeShopMenu();

    // Si el cooldown ha expirado o no existe, realiza la reposición de productos
    if (!PlayerUtils.isCooldown(ShopDynamicData.cooldowns.get(shop.getId()))) {
      // Actualiza el cooldown
      ShopDynamicData.cooldowns.put(shop.getId(), new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes)));

      // Realiza la reposición de productos
      replenish(shop);
    }
    if (amountProducts != ShopDynamicData.shopProducts.get(shop.getId()).size()) replenish(shop);
    return this;
  }

  private void initializeShopMenu() {
    if (ShopDynamicData.shopProducts == null) {
      ShopDynamicData.shopProducts = new ConcurrentHashMap<>();
    }
    if (ShopDynamicData.cooldowns == null) {
      ShopDynamicData.cooldowns = new ConcurrentHashMap<>();
    }
  }

  public List<Shop.Product> replenish(Shop shop) {
    List<Shop.Product> shopProducts = shop.getProducts();
    List<Shop.Product> currentProducts = ShopDynamicData.shopProducts.computeIfAbsent(shop.getId(), k -> new ArrayList<>());

    currentProducts.clear();
    int size = shopProducts.size();
    Set<Integer> chosenIndices = new HashSet<>();
    while (chosenIndices.size() < amountProducts && chosenIndices.size() < size) {
      int randomIndex = Utils.RANDOM.nextInt(size);
      if (chosenIndices.add(randomIndex)) {
        currentProducts.add(shopProducts.get(randomIndex));
      }
    }
    if (CobbleUtils.config.isDebug()) {
      CobbleUtils.LOGGER.info("Replenished shop " + shop.getId() + " with " + currentProducts.size() + " products");
      CobbleUtils.LOGGER.info("Chosen indices: " + chosenIndices);
      CobbleUtils.LOGGER.info("Current cooldown: " + getCooldown(shop));
      CobbleUtils.LOGGER.info("Current products: " + getProducts(shop));
    }
    if (!PlayerUtils.isCooldown(getCooldown(shop))) {
      ShopDynamicData.cooldowns.put(shop.getId(), new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes)));
    }
    ShopDynamicData.shopProducts.put(shop.getId(), currentProducts);
    return currentProducts;
  }

  public Date getCooldown(Shop shop) {
    if (ShopDynamicData.cooldowns.get(shop.getId()) == null) {
      ShopDynamicData.cooldowns.put(shop.getId(), new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes)));
      replenish(shop);
    }
    return ShopDynamicData.cooldowns.get(shop.getId());
  }

  public List<Shop.Product> getProducts(Shop shop) {
    // Obtén los productos de la tienda o realiza la reposición si no existen
    return ShopDynamicData.shopProducts.computeIfAbsent(shop.getId(), k -> replenish(shop));
  }
}
