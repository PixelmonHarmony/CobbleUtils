package com.kingpixel.cobbleutils.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.kingpixel.cobbleutils.CobbleUtils;
import kotlin.Unit;

/**
 * @author Carlos Varas Alonso - 12/06/2024 3:16
 */
public class PokemonSpawn {
  public static void register() {
    if (CobbleUtils.config.isRandomsize()) {
      CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.HIGH, (evt) -> {
        evt.getEntity().getPokemon().setScaleModifier(CobbleUtils.config.getRandomPokemonSize());
        if (CobbleUtils.config.isDebug())
          CobbleUtils.LOGGER.info("Pokemon " + evt.getEntity().getPokemon().getSpecies().getName() + " scaled to " + evt.getEntity().getPokemon().getScaleModifier());
        return Unit.INSTANCE;
      });
      CobbleUtils.LOGGER.info("PokemonSpawn registered");
    }
  }
}
