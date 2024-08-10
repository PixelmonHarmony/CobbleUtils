package com.kingpixel.cobbleutils.util;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * @author Carlos Varas Alonso - 04/07/2024 4:05
 */
public class ItemUtils {
  public static String getNameItem(String item) {
    return Utils.parseItemId(item).getName().getString();
  }

  public static String getNameItem(ItemStack itemStack) {
    if (itemStack.getNbt() == null) return getTranslatedName(itemStack);
    if (itemStack.getNbt().contains("display")) {
      return itemStack.getName().getString();
    }
    return getTranslatedName(itemStack);
  }

  public static String getTranslatedName(ItemStack itemStack) {
    return Text.translatable("<lang:" + itemStack.getItem().getTranslationKey() + ">").getString();
  }
}
