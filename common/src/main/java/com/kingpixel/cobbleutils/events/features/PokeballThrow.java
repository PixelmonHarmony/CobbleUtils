package com.kingpixel.cobbleutils.events.features;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.kingpixel.cobbleutils.CobbleUtils;
import kotlin.Unit;

/**
 * @author Carlos Varas Alonso - 20/07/2024 13:13
 */
public class PokeballThrow {
  public static void register() {
    CobblemonEvents.THROWN_POKEBALL_HIT.subscribe(Priority.HIGHEST, (evt) -> {
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.info("Pokeball thrown");
      }
      return Unit.INSTANCE;
    });

  }
}
