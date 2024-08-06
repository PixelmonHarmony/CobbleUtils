package com.kingpixel.cobbleutils.properties;

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.ScalePokemonData;
import com.kingpixel.cobbleutils.Model.SizeChanceWithoutItem;
import org.jetbrains.annotations.NotNull;

/**
 * @author Carlos Varas Alonso - 04/08/2024 19:40
 */
public class SizeProperty implements CustomPokemonProperty {
  private String value = "aleatory";

  public SizeProperty(String s) {
    if (s != null) {
      this.value = s;
    }
  }

  @Override public void apply(@NotNull PokemonEntity pokemonEntity) {
    SizeChanceWithoutItem sizeChanceWithoutItem = ScalePokemonData.getSize(pokemonEntity.getPokemon(), this.value);
    sizeChanceWithoutItem.apply(pokemonEntity.getPokemon());
  }

  @NotNull @Override public String asString() {
    return "size";
  }

  @Override public void apply(@NotNull Pokemon pokemon) {
    SizeChanceWithoutItem sizeChanceWithoutItem = ScalePokemonData.getSize(pokemon, this.value);
    sizeChanceWithoutItem.apply(pokemon);
  }

  @Override public boolean matches(@NotNull Pokemon pokemon) {
    return true;
  }

  @Override public boolean matches(@NotNull PokemonEntity pokemonEntity) {
    return true;
  }
}
