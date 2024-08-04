package com.kingpixel.cobbleutils.properties;

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import org.jetbrains.annotations.NotNull;

/**
 * @author Carlos Varas Alonso - 04/08/2024 19:40
 */
public class ScaleProperty implements CustomPokemonProperty {

  @Override public void apply(@NotNull PokemonEntity pokemonEntity) {
  }

  @NotNull @Override public String asString() {
    return "scale";
  }

  @Override public void apply(@NotNull Pokemon pokemon) {

  }

  @Override public boolean matches(@NotNull Pokemon pokemon) {
    return false;
  }

  @Override public boolean matches(@NotNull PokemonEntity pokemonEntity) {
    return false;
  }
}
