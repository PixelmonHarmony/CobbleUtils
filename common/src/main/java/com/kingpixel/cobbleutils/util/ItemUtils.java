package com.kingpixel.cobbleutils.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * @author Carlos Varas Alonso - 04/07/2024 4:05
 */
public class ItemUtils {
  public static String getNameItem(String item) {
    return Utils.parseItemId(item).getHoverName().getString();
  }

  public static String getNameItem(ItemStack itemStack) {
    if (itemStack.getTag() == null) return getTranslatedName(itemStack);
    if (itemStack.getTag().contains("display")) {
      return itemStack.getHoverName().getString();
    }
    return getTranslatedName(itemStack);
  }

  public static String getTranslatedName(ItemStack itemStack) {
    return Component.translatable("<lang:" + itemStack.getDescriptionId() + ">").getString();
  }
}
