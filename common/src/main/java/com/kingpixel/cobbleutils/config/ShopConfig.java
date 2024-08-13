package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ShopMenu;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 13/08/2024 17:14
 */
@Getter
@Setter
@ToString
public class ShopConfig {
  public static String PATH_SHOP = CobbleUtils.PATH + "/shop/";
  private ShopMenu shop;

  public ShopConfig() {
    shop = new ShopMenu();
    shop.setShops(null);
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH, "shopconfig.json",
      el -> {
        Gson gson = Utils.newGson();
        ShopConfig config = gson.fromJson(el, ShopConfig.class);
        this.shop = config.shop;
        config.getShop().setShops(ShopMenu.getShops(PATH_SHOP));
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH, "shopconfig.json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write shopconfig.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No shopconfig.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      ShopMenu.createDefaultShop(PATH_SHOP);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH, "shopconfig.json",
        data);
      this.getShop().setShops(ShopMenu.getShops(PATH_SHOP));

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }

  }

}
