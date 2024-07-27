package com.kingpixel.cobbleutils.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.CobbleUtilsTags;
import com.kingpixel.cobbleutils.Model.ScalePokemonData;
import com.kingpixel.cobbleutils.Model.SizeChanceWithoutItem;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import kotlin.Unit;
import net.minecraft.world.entity.Mob;

import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.SIZE_CUSTOM_TAG;
import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.SIZE_TAG;

/**
 * @author Carlos Varas Alonso - 03/07/2024 22:37
 */
public class ScaleEvent {

  public static void register() {
    if (CobbleUtils.config.isRandomsize()) {
      CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.HIGH, (evt) -> scalePokemon(evt.getPokemon()));
      CobblemonEvents.STARTER_CHOSEN.subscribe(Priority.HIGH, (evt) -> scalePokemon(evt.getPokemon()));
      EntityEvent.ADD.register((entity, level) -> {
        if (!CobbleUtils.config.isRandomsize()) return EventResult.pass();
        if (((Mob) entity).isNoAi()) return EventResult.pass();
        if (entity instanceof PokemonEntity pokemonEntity) {
          Pokemon pokemon = pokemonEntity.getPokemon();
          if (pokemon.getPersistentData().getString(SIZE_TAG).equalsIgnoreCase(SIZE_CUSTOM_TAG))
            return EventResult.pass();
          if (pokemon.getPersistentData().getBoolean(CobbleUtilsTags.BOSS_TAG)) return EventResult.pass();
          if (pokemon.isPlayerOwned()) {
            solveScale(pokemonEntity.getPokemon());
            return EventResult.pass();
          }
          scalePokemon(pokemonEntity.getPokemon());
        }
        return EventResult.pass();
      });
    }
  }

  public static void solveScale(Pokemon pokemon) {
    ScalePokemonData scalePokemonData = ScalePokemonData.getScalePokemonData(pokemon);
    if (scalePokemonData.existSize(pokemon)) {
      SizeChanceWithoutItem size = scalePokemonData.getSize(pokemon);
      if (pokemon.getScaleModifier() == size.getSize()) return;
      applyScale(pokemon, size.getId(), size.getSize());
    } else {
      if (CobbleUtils.config.isSolveSizeRandom()) {
        SizeChanceWithoutItem size = scalePokemonData.getRandomPokemonSize();
        applyScale(pokemon, size.getId(), size.getSize());
      } else {
        applyScale(pokemon, CobbleUtils.config.getDefaultsize(), 1f);
      }
    }
  }

  /**
   * @param pokemon Pokemon
   *
   * @return Unit
   */
  private static Unit scalePokemon(Pokemon pokemon) {
    if (!CobbleUtils.config.isRandomsize()) return Unit.INSTANCE;
    if (CobbleUtils.config.getPokemonsizes().isEmpty()) return Unit.INSTANCE;
    if (pokemon.getPersistentData().getBoolean(CobbleUtilsTags.BOSS_TAG)) return Unit.INSTANCE;

    ScalePokemonData scalePokemonData = ScalePokemonData.getScalePokemonData(pokemon);
    SizeChanceWithoutItem size = scalePokemonData.getRandomPokemonSize();
    applyScale(pokemon, size.getId(), size.getSize());
    return Unit.INSTANCE;
  }

  private static void applyScale(Pokemon pokemon, String id, float size) {
    pokemon.getPersistentData().putString(SIZE_TAG, id);
    pokemon.setScaleModifier(size);
  }
}
