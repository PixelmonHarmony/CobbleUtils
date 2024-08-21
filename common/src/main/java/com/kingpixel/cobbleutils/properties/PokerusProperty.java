package com.kingpixel.cobbleutils.properties;

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.options.Pokerus;
import org.jetbrains.annotations.NotNull;

/**
 * @author Carlos Varas Alonso - 04/08/2024 19:40
 */
public class PokerusProperty implements CustomPokemonProperty {
  private Boolean value;

  public PokerusProperty(Boolean s) {
    this.value = s;
  }

  @Override public void apply(@NotNull PokemonEntity pokemonEntity) {
    if (this.value) {
      Pokerus.apply(pokemonEntity.getPokemon());
    }
  }

  @NotNull @Override public String asString() {
    return Boolean.TRUE.toString();
  }

  @Override public void apply(@NotNull Pokemon pokemon) {
    if (this.value) {
      Pokerus.apply(pokemon);
    }
  }

  @Override public boolean matches(@NotNull Pokemon pokemon) {
    return true;
  }

  @Override public boolean matches(@NotNull PokemonEntity pokemonEntity) {
    return true;
  }
}
