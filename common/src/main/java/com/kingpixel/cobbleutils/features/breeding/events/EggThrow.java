package com.kingpixel.cobbleutils.features.breeding.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import kotlin.Unit;

/**
 * @author Carlos Varas Alonso - 23/07/2024 22:07
 */
public class EggThrow {
  public static void register() {
    CobblemonEvents.POKEMON_SENT_PRE.subscribe(Priority.HIGHEST, (evt) -> {
      if (evt.getPokemon().getSpecies().showdownId().equalsIgnoreCase("egg")) {
        evt.getPokemon().setCurrentHealth(0);
        evt.cancel();
      }
      return Unit.INSTANCE;
    });
  }
}
