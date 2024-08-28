package com.kingpixel.cobbleutils.Model.shops;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Carlos Varas Alonso - 28/08/2024 3:53
 */
public class ShopDynamicData {
  public static Map<String, Date> cooldowns = new ConcurrentHashMap<>();
  public static Map<String, List<Shop.Product>> shopProducts = new ConcurrentHashMap<>();
}
