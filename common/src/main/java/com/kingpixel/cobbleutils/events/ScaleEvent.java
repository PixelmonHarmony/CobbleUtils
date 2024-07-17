package com.kingpixel.cobbleutils.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ScalePokemonData;
import com.kingpixel.cobbleutils.Model.SizeChanceWithoutItem;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import kotlin.Unit;
import net.minecraft.world.entity.Mob;

/**
 * @author Carlos Varas Alonso - 03/07/2024 22:37
 */
public class ScaleEvent {
  public static void register() {
    if (CobbleUtils.config.isRandomsize()) {
      CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.HIGH, (evt) -> scalePokemon(evt.getPokemon()));
      CobblemonEvents.STARTER_CHOSEN.subscribe(Priority.HIGH, (evt) -> scalePokemon(evt.getPokemon()));
      EntityEvent.ADD.register((entity, level) -> {
        if (entity instanceof PokemonEntity) {
          if (((Mob) entity).isNoAi()) return EventResult.pass();
          PokemonEntity pokemonEntity = (PokemonEntity) entity;
          if (pokemonEntity.getPokemon().isPlayerOwned()) {
            solveScale(pokemonEntity.getPokemon());
            return EventResult.pass();
          }
          scalePokemon(pokemonEntity.getPokemon());
        }
        return EventResult.pass();
      });
    }
  }

  private static void solveScale(Pokemon pokemon) {
    if (pokemon.getPersistentData().getString("size").equalsIgnoreCase("custom")) return;
    ScalePokemonData scalePokemonData = ScalePokemonData.getScalePokemonData(pokemon);
    if (scalePokemonData.existSize(pokemon)) {
      SizeChanceWithoutItem size = scalePokemonData.getSize(pokemon);
      if (pokemon.getScaleModifier() == size.getSize()) return;
      pokemon.getPersistentData().putString("size", size.getId());
      pokemon.setScaleModifier(size.getSize());
    } else {
      if (CobbleUtils.config.isSolveSizeRandom()) {
        SizeChanceWithoutItem size = scalePokemonData.getRandomPokemonSize();
        pokemon.getPersistentData().putString("size", size.getId());
        pokemon.setScaleModifier(size.getSize());
      } else {
        pokemon.getPersistentData().putString("size", "normal");
        pokemon.setScaleModifier(1f);
      }
    }
  }

  /**
   * @param pokemon Pokemon
   *
   * @return Unit
   */
  private static Unit scalePokemon(Pokemon pokemon) {
    if (CobbleUtils.config.getPokemonsizes().isEmpty()) return Unit.INSTANCE;
    if (!CobbleUtils.config.isRandomsize()) return Unit.INSTANCE;
    ScalePokemonData scalePokemonData = ScalePokemonData.getScalePokemonData(pokemon);
    SizeChanceWithoutItem size = scalePokemonData.getRandomPokemonSize();
    pokemon.getPersistentData().putString("size", size.getId());
    pokemon.setScaleModifier(size.getSize());
    return Unit.INSTANCE;
  }
}
