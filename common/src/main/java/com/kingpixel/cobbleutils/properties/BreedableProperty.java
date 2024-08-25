package com.kingpixel.cobbleutils.properties;

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Carlos Varas Alonso - 04/08/2024 19:40
 */
public class BreedableProperty implements CustomPokemonProperty {
  private Boolean value = true;

  public BreedableProperty(Boolean s) {
    this.value = s;
  }

  @Override public void apply(@NotNull PokemonEntity pokemonEntity) {
    PokemonUtils.setBreedable(pokemonEntity.getPokemon(), this.value);
  }

  @NotNull @Override public String asString() {
    return Boolean.TRUE.toString();
  }

  @Override public void apply(@NotNull Pokemon pokemon) {
    PokemonUtils.setBreedable(pokemon, this.value);
  }

  @Override public boolean matches(@NotNull Pokemon pokemon) {
    return true;
  }

  @Override public boolean matches(@NotNull PokemonEntity pokemonEntity) {
    return true;
  }
}
