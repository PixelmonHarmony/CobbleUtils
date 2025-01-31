package com.kingpixel.cobbleutils.features.shops.models.types;

import com.kingpixel.cobbleutils.features.shops.Shop;
import com.kingpixel.cobbleutils.features.shops.models.Product;
import com.kingpixel.cobbleutils.features.shops.models.ShopDynamicData;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Improved version of ShopTypeDynamic class
 *
 * @author
 */
@Getter
@Setter
@ToString
public class ShopTypeDynamic extends ShopType {
  private int minutes;
  private int amountProducts;

  public ShopTypeDynamic() {
    super(TypeShop.DYNAMIC);
    this.minutes = 60;
    this.amountProducts = 10;
  }

  public ShopTypeDynamic(int amountProducts) {
    super(TypeShop.DYNAMIC);
    this.minutes = 60;
    this.amountProducts = amountProducts;
  }

  public ShopTypeDynamic updateShop(Shop shop) {
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
}