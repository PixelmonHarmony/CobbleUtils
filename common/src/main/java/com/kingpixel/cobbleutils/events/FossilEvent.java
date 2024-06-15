package com.kingpixel.cobbleutils.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.kingpixel.cobbleutils.CobbleUtils;
import kotlin.Unit;

/**
 * @author Carlos Varas Alonso - 14/06/2024 2:54
 */
public class FossilEvent {
  public static void register() {
    if (CobbleUtils.config.isRandomsize()) {
      CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.HIGH, (evt) -> {
        evt.getPokemon().setScaleModifier(CobbleUtils.config.getRandomPokemonSize());
        if (CobbleUtils.config.isDebug())
          CobbleUtils.LOGGER.info("Pokemon " + evt.getPokemon().getSpecies().getName() + " scaled to " + evt.getPokemon().getScaleModifier());
        return Unit.INSTANCE;
      });
      CobbleUtils.LOGGER.info("FossilEvent registered");
    }
  }
}
