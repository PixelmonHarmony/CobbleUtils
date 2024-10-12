package com.kingpixel.cobbleutils.properties;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 04/08/2024 19:40
 */
public class MinIvsProperty implements CustomPokemonProperty {
  private String value = "0";

  public MinIvsProperty(String s) {
    if (s != null) {
      this.value = s;
    }
  }

  @Override public void apply(@NotNull PokemonEntity pokemonEntity) {
    applyMinIvs(pokemonEntity.getPokemon());
  }

  @NotNull @Override public String asString() {
    return "min_ivs";
  }

  @Override public void apply(@NotNull Pokemon pokemon) {
    applyMinIvs(pokemon);
  }

  @Override public boolean matches(@NotNull Pokemon pokemon) {
    return true;
  }

  @Override public boolean matches(@NotNull PokemonEntity pokemonEntity) {
    return true;
  }

  private void applyMinIvs(Pokemon pokemon) {
    int min = 0;
    int amount_of_stats = 6;
    List<Stats> stats = new ArrayList<>(Arrays.stream(Stats.values()).toList());
    stats.remove(Stats.EVASION);
    stats.remove(Stats.ACCURACY);

    try {
      min = Integer.parseInt(this.value.split("_")[0]);
      amount_of_stats = Integer.parseInt(this.value.split("_")[1]);
    } catch (NumberFormatException ignored) {
    }

    min = Math.max(0, Math.min(min, 31));

    int finalMin = min;
    for (int i = 0; i < amount_of_stats; i++) {
      if (stats.isEmpty()) return;
      Stats stats1 = stats.remove(Utils.RANDOM.nextInt(stats.size()));
      pokemon.getIvs().set(stats1, Utils.RANDOM.nextInt(finalMin, 32));
    }
  }

}
