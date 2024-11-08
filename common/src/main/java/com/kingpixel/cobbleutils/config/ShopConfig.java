package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.shops.Shop;
import com.kingpixel.cobbleutils.features.shops.ShopConfigMenu;
import com.kingpixel.cobbleutils.features.shops.models.types.ShopTypeDynamic;
import com.kingpixel.cobbleutils.features.shops.models.types.ShopTypeDynamicWeekly;
import com.kingpixel.cobbleutils.features.shops.models.types.ShopTypePermanent;
import com.kingpixel.cobbleutils.features.shops.models.types.ShopTypeWeekly;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.kingpixel.cobbleutils.features.shops.ShopTransactions.loadTransactions;

/**
 * @author Carlos Varas Alonso - 13/08/2024 17:14
 */
@Getter
@Setter
@ToString
public class ShopConfig {
  private ShopConfigMenu shop;
  public static final Map<ShopConfigMenu.ShopMod, List<Shop>> shops = new ConcurrentHashMap<>();

  public ShopConfig() {
    shop = new ShopConfigMenu();
  }

  public void createConfigIfNotExists(String pathShop) {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(pathShop, "shopconfig.json",
      el -> {
        Gson gson = Utils.newGson();
        ShopConfig config = gson.fromJson(el, ShopConfig.class);
        this.shop = config.getShop();
        CobbleUtils.LOGGER.info("shopconfig.json loaded successfully from " + pathShop);
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No shopconfig.json file found at " + pathShop + ". Creating a new one.");
      ShopConfig newConfig = new ShopConfig();
      Gson gson = Utils.newGson();
      String data = gson.toJson(newConfig);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(pathShop, "shopconfig.json", data);
      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write shopconfig.json file to " + pathShop);
      } else {
        CobbleUtils.LOGGER.info("shopconfig.json created successfully at " + pathShop);
      }
    }
  }

  public static List<Shop> addShopsFromPath(String mod_id, String path) {
    if (true) {
      // Default shops
      String default_path = path + "defaults/";
      CobbleUtils.LOGGER.info("No shops found. Creating default shops.");
      List<Shop> defaultShops = createDefaultShops();
      saveShopsToPath(mod_id, default_path, defaultShops);
    }
    // Load shops
    List<Shop> shopList = ShopConfigMenu.getShops(path);
    shopList.forEach(ShopConfig::checkShop);
    saveShopsToPath(mod_id, path, shopList);
    ShopConfigMenu.ShopMod shopMod = new ShopConfigMenu.ShopMod(mod_id, path);
    shops.put(shopMod, shopList);

    return shopList;
  }

  private static List<Shop> createDefaultShops() {
    List<Shop> shopArrayList = List.of(
      new Shop("permanent", "Permanent", new ShopTypePermanent(), (short) 6, List.of()),
      new Shop("dynamic", "Dynamic", new ShopTypeDynamic(), (short) 6, List.of(
        "%cooldown%",
        "%amountProducts%"
      )),
      new Shop("weekly", "Weekly", new ShopTypeWeekly(), (short) 6, List.of(
        "%days%"
      )),
      new Shop("dynamicweekly", "DynamicWeekly", new ShopTypeDynamicWeekly(), (short) 6, List.of(
        "%cooldown%",
        "%amountProducts%",
        "%days%"
      ))
    );
    for (int i = 0; i < shopArrayList.size(); i++) {
      shopArrayList.get(i).getDisplay().setSlot(i);
    }
    return shopArrayList;
  }

  private static void saveShopsToPath(String mod_id, String path, List<Shop> shopList) {
    Gson gson = Utils.newGson();
    File directory = Utils.getAbsolutePath(path);
    if (!directory.exists()) {
      directory.mkdirs();
    }

    for (Shop shop : shopList) {
      String json = gson.toJson(shop);
      String fileName = shop.getId() + ".json";
      checkShop(shop);

      try {
        Utils.writeFileAsync(path, fileName, json).join();
      } catch (Exception e) {
        CobbleUtils.LOGGER.error("Failed to save default shop " + shop.getId());
      }
    }
  }

  public static void checkShop(Shop shop) {
    if (shop.getRows() < 1 || shop.getRows() > 6) shop.setRows((short) 6);
    int max_array = (shop.getRows() * 9) - 1;

    if (shop.getPrevious() == null) shop.setPrevious(CobbleUtils.language.getItemPrevious());
    if (shop.getNext() == null) shop.setNext(CobbleUtils.language.getItemNext());
    if (shop.getClose() == null) shop.setClose(CobbleUtils.language.getItemClose());
    
    if (shop.getPrevious().getSlot() == null || shop.getPrevious().getSlot() > max_array)
      shop.getPrevious().setSlot(max_array - 8);
    if (shop.getClose().getSlot() == null || shop.getClose().getSlot() > max_array)
      shop.getClose().setSlot(max_array - 4);
    if (shop.getNext().getSlot() == null || shop.getNext().getSlot() > max_array)
      shop.getNext().setSlot(max_array);
  }

  public void init(String pathShop, String mod_id, String pathShops) {
    createConfigIfNotExists(pathShop);

    List<Shop> shopList = addShopsFromPath(mod_id, pathShops);
    shopList.forEach(ShopConfig::checkShop);
    loadTransactions(shop);
    ShopConfigMenu.addShops(mod_id, pathShops, shopList);
  }
}
