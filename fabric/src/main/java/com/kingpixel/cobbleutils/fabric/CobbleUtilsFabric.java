package com.kingpixel.cobbleutils.fabric;

import com.kingpixel.cobbleutils.CobbleUtils;
import net.fabricmc.api.ModInitializer;

public class CobbleUtilsFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    CobbleUtils.init();
  }
}
