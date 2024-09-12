package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.config.ShopConfig;

/**
 * @author Carlos Varas Alonso - 13/09/2024 1:52
 */
public abstract class ShopExtend {
  public static final String PATH = "/config/cobbleutils";
  public static final String PATH_SHOP = PATH + "/shop/";
  public static final String PATH_SHOPS = PATH_SHOP + "shops/";
  public static ShopConfig shopConfig = new ShopConfig();
}
