package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.shops.ShopMenu;
import com.kingpixel.cobbleutils.Model.shops.ShopSell;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static com.kingpixel.cobbleutils.Model.shops.ShopTransactions.loadTransactions;

/**
 * @author Carlos Varas Alonso - 13/08/2024 17:14
 */
@Getter
@Setter
@ToString
public class ShopConfig {
  public static String PATH_SHOP = CobbleUtils.PATH + "/shop/";
  public static String PATH_SHOPS = PATH_SHOP + "/shops/";
  private short rowsBuySellMenu;
  private short slotViewProduct;
  private ShopMenu shop;

  public ShopConfig() {
    this.rowsBuySellMenu = 6;
    this.slotViewProduct = 22;
    shop = new ShopMenu();
    shop.setShops(null);
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(PATH_SHOP, "shopconfig.json",
      el -> {
        Gson gson = Utils.newGson();
        ShopConfig config = gson.fromJson(el, ShopConfig.class);
        this.shop = config.shop;
        getShop().setShops(new ArrayList<>());
        this.rowsBuySellMenu = config.getRowsBuySellMenu();
        this.slotViewProduct = config.getSlotViewProduct();
        String data = gson.toJson(this);
        config.getShop().setShops(ShopMenu.getShops(PATH_SHOPS));
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(PATH_SHOP, "shopconfig.json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write shopconfig.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No shopconfig.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      ShopMenu.createDefaultShop(PATH_SHOPS);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(PATH_SHOP, "shopconfig.json",
        data);
      this.getShop().setShops(ShopMenu.getShops(PATH_SHOPS));

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }

    shop.getShops().forEach(ShopSell::addProduct);
    CobbleUtils.LOGGER.info(ShopSell.products.toString());
    loadTransactions(shop);
  }


}
