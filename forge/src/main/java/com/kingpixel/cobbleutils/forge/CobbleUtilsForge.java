package com.kingpixel.cobbleutils.forge;

import com.kingpixel.cobbleutils.CobbleUtils;
import net.minecraftforge.fml.common.Mod;

@Mod(CobbleUtils.MOD_ID)
public class CobbleUtilsForge {
  public CobbleUtilsForge() {
    CobbleUtils.init();
  }
}