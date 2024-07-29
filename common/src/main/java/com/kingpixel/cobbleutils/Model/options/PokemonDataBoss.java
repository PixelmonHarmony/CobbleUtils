package com.kingpixel.cobbleutils.Model.options;

import com.cobblemon.mod.common.pokemon.Pokemon;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 29/07/2024 4:40
 */
@Getter
@Setter
@ToString
public class PokemonDataBoss {
  private List<String> pokemon;
  private String formsoraspects;

  public PokemonDataBoss() {
    this.pokemon = List.of("zorua", "typhlosion");
    this.formsoraspects = "hisuian=true";
  }

  public PokemonDataBoss(List<String> pokemon, String formsoraspects) {
    this.pokemon = pokemon;
    this.formsoraspects = formsoraspects;
  }

  public static String getRandom(Pokemon pokemon, List<PokemonDataBoss> pokemonDataBosses) {
    if (pokemonDataBosses == null || pokemonDataBosses.isEmpty()) return "";

    return pokemonDataBosses.stream()
      .filter(pokemonDataBoss -> pokemonDataBoss.getPokemon().contains(pokemon.showdownId()))
      .map(PokemonDataBoss::getFormsoraspects)
      .findFirst()
      .orElse("");
  }

}
