package com.kingpixel.cobbleutils.features.breeding.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import net.minecraft.text.Text;

/**
 * @author Carlos Varas Alonso - 02/08/2024 13:28
 */
public class AdventureBreeding {
  public static Text adventure(String s) {
    return AdventureTranslator.toNativeWithOutPrefix(s
      .replace("%prefix%", CobbleUtils.breedconfig.getPrefix()));
  }
}
