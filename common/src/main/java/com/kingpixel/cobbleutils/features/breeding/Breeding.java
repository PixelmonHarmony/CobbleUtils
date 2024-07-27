package com.kingpixel.cobbleutils.features.breeding;

import com.kingpixel.cobbleutils.features.breeding.events.EggThrow;
import com.kingpixel.cobbleutils.features.breeding.events.WalkBreeding;

/**
 * @author Carlos Varas Alonso - 23/07/2024 9:24
 */
public class Breeding {
  public static void register() {
    events();
  }

  private static void events() {
    WalkBreeding.register();
    EggThrow.register();
  }
}
