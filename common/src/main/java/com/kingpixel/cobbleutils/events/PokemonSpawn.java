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
        try {
          if (CobbleUtils.config.getPokemonsizes().isEmpty()) return Unit.INSTANCE;
          if (!CobbleUtils.config.isRandomsize()) return Unit.INSTANCE;
          float scale = CobbleUtils.config.getRandomPokemonSize();
          evt.getEntity().getPokemon().setScaleModifier(scale);
        } catch (Exception e) {
          CobbleUtils.LOGGER.error("Error scaling pokemon: " + e.getMessage());
        }
        return Unit.INSTANCE;
      });
    }
  }
}
