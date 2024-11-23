package com.kingpixel.cobbleutils.features.shops.models.types;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.shops.Shop;
import com.kingpixel.cobbleutils.features.shops.models.Product;
import com.kingpixel.cobbleutils.features.shops.models.ShopDynamicData;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Improved version of ShopTypeDynamicWeekly class
 *
 * @author
 */
@Getter
@Setter
@ToString
public class ShopTypeDynamicWeekly extends ShopType {
  private int amountProducts;
  private int minutes;
  private List<DayOfWeek> dayOfWeek;

  public ShopTypeDynamicWeekly() {
    super(TypeShop.DYNAMIC_WEEKLY);
    amountProducts = 10;
    minutes = 60;
    dayOfWeek = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  }

  public ShopTypeDynamicWeekly(List<DayOfWeek> dayOfWeek, int amountProducts, int minutes) {
    super(TypeShop.DYNAMIC_WEEKLY);
    this.dayOfWeek = dayOfWeek;
    this.amountProducts = amountProducts;
    this.minutes = minutes;
  }

  public ShopTypeDynamicWeekly updateShop(Shop shop) {
    initializeShopMenu();
    if (!PlayerUtils.isCooldown(getCooldown(shop))) {
      setCooldown(shop);
      replenish(shop);
    }
    if (amountProducts != ShopDynamicData.shopProducts.get(shop.getId()).size()) {
      replenish(shop);
    }
    return this;
  }

  public List<Product> replenish(Shop shop) {
    List<Product> shopProducts = shop.getProducts();
    List<Product> currentProducts = ShopDynamicData.shopProducts.computeIfAbsent(shop.getId(), k -> new ArrayList<>());

    currentProducts.clear();
    int size = shopProducts.size();
    Set<Integer> chosenIndices = new HashSet<>();
    while (chosenIndices.size() < amountProducts && chosenIndices.size() < size) {
      int randomIndex = Utils.RANDOM.nextInt(size);
      if (chosenIndices.add(randomIndex)) {
        currentProducts.add(shopProducts.get(randomIndex));
      }
    }
    setCooldown(shop);
    ShopDynamicData.shopProducts.put(shop.getId(), currentProducts);
    return currentProducts;
  }

  public Date getCooldown(Shop shop) {
    return ShopDynamicData.cooldowns.get(shop.getId());
  }

  private void setCooldown(Shop shop) {
    ShopDynamicData.cooldowns.put(shop.getId(), new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes)));
  }

  @Override
  public List<Product> getProducts(Shop shop) {
    if (!PlayerUtils.isCooldown(getCooldown(shop))) {
      replenish(shop);
    }
    return ShopDynamicData.shopProducts.computeIfAbsent(shop.getId(), k -> replenish(shop));
  }

  @Override
  public boolean isAvailable(ServerPlayerEntity player) {
    boolean isAvailable = dayOfWeek.contains(DayOfWeek.from(new Date().toInstant()));
    if (!isAvailable) {
      PlayerUtils.sendMessage(
        player,
        "This shop is only available on the following days: " + dayOfWeek,
        CobbleUtils.language.getPrefixShop()
      );
    }
    return isAvailable;
  }
}