package com.kingpixel.cobbleutils.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.platform.events.PlatformEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.CobbleUtilsTags;
import com.kingpixel.cobbleutils.Model.ScalePokemonData;
import com.kingpixel.cobbleutils.Model.SizeChanceWithoutItem;
import com.kingpixel.cobbleutils.properties.ScalePropertyType;
import com.kingpixel.cobbleutils.properties.SizePropertyType;
import kotlin.Unit;

import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.SIZE_CUSTOM_TAG;
import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.SIZE_TAG;

/**
 * @author Carlos Varas Alonso - 03/07/2024 22:37
 */
public class ScaleEvent {
  private static final SizePropertyType SizePropertyType = new SizePropertyType();
  private static final ScalePropertyType ScalePropertyType = new ScalePropertyType();

  public static void register() {
    CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.HIGH, (evt) -> {
      if (!CobbleUtils.config.isRandomsize()) return Unit.INSTANCE;
      scalePokemon(evt.getPokemon());
      return Unit.INSTANCE;
    });
    CobblemonEvents.STARTER_CHOSEN.subscribe(Priority.HIGH, (evt) -> {
      if (!CobbleUtils.config.isRandomsize()) return Unit.INSTANCE;
      scalePokemon(evt.getPokemon());
      return Unit.INSTANCE;
    });
    CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.HIGH, (evt) -> {
      if (!CobbleUtils.config.isRandomsize()) return Unit.INSTANCE;
      PokemonEntity pokemonEntity = evt.getEntity();
      if (pokemonEntity.isPersistenceRequired()) return Unit.INSTANCE;
      if (pokemonEntity.isNoAi()) return Unit.INSTANCE;
      Pokemon pokemon = pokemonEntity.getPokemon();
      if (pokemon.getPersistentData().getString(SIZE_TAG).equalsIgnoreCase(SIZE_CUSTOM_TAG)) return Unit.INSTANCE;
      if (pokemon.isPlayerOwned()) {
        solveScale(pokemonEntity.getPokemon());
        return Unit.INSTANCE;
      }
      scalePokemon(pokemonEntity.getPokemon());
      return Unit.INSTANCE;
    });

    CobblemonEvents.POKEMON_SENT_POST.subscribe(Priority.NORMAL, (evt) -> {
      if (!CobbleUtils.config.isRandomsize()) return Unit.INSTANCE;
      solveScale(evt.getPokemon());
      return Unit.INSTANCE;
    });

    PlatformEvents.SERVER_STARTED.subscribe(Priority.NORMAL, (evt) -> {
      CustomPokemonProperty.Companion.register(ScalePropertyType);
      CustomPokemonProperty.Companion.register(SizePropertyType);
      return Unit.INSTANCE;
    });


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
   */
  private static void scalePokemon(Pokemon pokemon) {
    if (!CobbleUtils.config.isRandomsize()) return;
    if (CobbleUtils.config.getPokemonsizes().isEmpty()) return;
    if (pokemon.getPersistentData().getBoolean(CobbleUtilsTags.BOSS_TAG)) return;
    ScalePokemonData scalePokemonData = ScalePokemonData.getScalePokemonData(pokemon);
    SizeChanceWithoutItem size = scalePokemonData.getRandomPokemonSize();
    applyScale(pokemon, size.getId(), size.getSize());
  }

  private static void applyScale(Pokemon pokemon, String id, float size) {
    pokemon.getPersistentData().putString(SIZE_TAG, id);
    pokemon.setScaleModifier(size);
  }
}
