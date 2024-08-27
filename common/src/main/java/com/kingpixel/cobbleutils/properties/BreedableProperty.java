package com.kingpixel.cobbleutils.properties;

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 04/08/2024 19:40
 */
public class BreedableProperty implements CustomPokemonProperty {
  private final Boolean value;

  public BreedableProperty(Boolean s) {
    this.value = Objects.requireNonNullElse(s, true);
  }

  @Override public void apply(@NotNull PokemonEntity pokemonEntity) {
    PokemonUtils.setBreedable(pokemonEntity.getPokemon(), Objects.requireNonNullElse(this.value, true));
  }

  @NotNull @Override public String asString() {
    if (this.value == null) {
      return "true";
    } else {
      return this.value.toString();
    }
  }

  @Override public void apply(@NotNull Pokemon pokemon) {
    PokemonUtils.setBreedable(pokemon, Objects.requireNonNullElse(this.value, true));
  }

  @Override public boolean matches(@NotNull Pokemon pokemon) {
    return true;
  }

  @Override public boolean matches(@NotNull PokemonEntity pokemonEntity) {
    return true;
  }
}
