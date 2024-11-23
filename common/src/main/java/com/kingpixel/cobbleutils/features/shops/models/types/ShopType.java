package com.kingpixel.cobbleutils.features.shops.models.types;

import com.kingpixel.cobbleutils.features.shops.Shop;
import com.kingpixel.cobbleutils.features.shops.models.Product;
import com.kingpixel.cobbleutils.features.shops.models.ShopDynamicData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Carlos Varas Alonso - 27/08/2024 21:49
 */
@Getter
@Setter
@ToString
public abstract class ShopType {
  protected TypeShop typeShop;

  public ShopType() {
    this.typeShop = TypeShop.PERMANENT;
  }

  public ShopType(TypeShop typeShop) {
    this.typeShop = typeShop;
  }

  public enum TypeShop {
    PERMANENT,  // Stores that always display all their items and are always available
    DYNAMIC,    // Stores where items change and a specific number of items are shown randomly
    WEEKLY,      // Stores that are only available on certain days of the week
    DYNAMIC_WEEKLY, // Stores that are only available on certain days of the week and change their items
  }

  void initializeShopMenu() {
    if (ShopDynamicData.shopProducts == null) {
      ShopDynamicData.shopProducts = new ConcurrentHashMap<>();
    }
    if (ShopDynamicData.cooldowns == null) {
      ShopDynamicData.cooldowns = new ConcurrentHashMap<>();
    }
  }

  public List<Product> getProducts(Shop shop) {
    return shop.getProducts();
  }

  private boolean replenish(Shop shop) {
    return false;
  }

  public boolean isAvailable(ServerPlayerEntity player) {
    return true;
  }

}


