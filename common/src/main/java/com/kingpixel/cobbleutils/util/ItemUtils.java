package com.kingpixel.cobbleutils.util;

import net.minecraft.item.ItemStack;

/**
 * @author Carlos Varas Alonso - 04/07/2024 4:05
 */
public class ItemUtils {
  public static String getNameItem(String item) {
    ItemStack itemStack = Utils.parseItemId(item);
    return getTranslatedName(itemStack);
  }

  public static String getNameItem(ItemStack itemStack) {
    return getTranslatedName(itemStack);
  }

  public static String getTranslatedName(ItemStack itemStack) {
    if (itemStack.getNbt() == null) return "<lang:" + itemStack.getItem().getTranslationKey() + ">";
    if (itemStack.getNbt().contains("display")) return itemStack.getName().getString();
    return "<lang:" + itemStack.getItem().getTranslationKey() + ">";
  }
}
