package com.kingpixel.cobbleutils.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import kotlin.Unit;

/**
 * @author Carlos Varas Alonso - 03/07/2024 22:37
 */
public class ScaleEvent {
  public static void register() {
    if (CobbleUtils.config.isRandomsize()) {
      CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.HIGH, (evt) -> scalePokemon(evt.getEntity().getPokemon()));
      CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.HIGH, (evt) -> scalePokemon(evt.getPokemon()));
      CobblemonEvents.STARTER_CHOSEN.subscribe(Priority.HIGH, (evt) -> scalePokemon(evt.getPokemon()));
    }
  }

  /**
   * @param pokemon
   *
   * @return
   */
  private static Unit scalePokemon(Pokemon pokemon) {
    if (CobbleUtils.config.getPokemonsizes().isEmpty()) return Unit.INSTANCE;
    if (!CobbleUtils.config.isRandomsize()) return Unit.INSTANCE;
    float scale = CobbleUtils.config.getRandomPokemonSize();
    pokemon.getPersistentData().putString("size", CobbleUtils.config.getSizeName(scale));
    pokemon.setScaleModifier(scale);
    return Unit.INSTANCE;
  }
}
