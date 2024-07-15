package com.kingpixel.cobbleutils.Model;

import lombok.Getter;
import lombok.ToString;

/**
 * @author Carlos Varas Alonso - 13/06/2024 10:19
 */
@Getter
@ToString
public class PokemonChance {
  private String pokemon;
  private int chance;

  public PokemonChance() {
    this.pokemon = "bulbasaur";
    this.chance = 100;
  }

  public PokemonChance(String pokemon, int chance) {
    this.pokemon = pokemon;
    this.chance = chance;

  }
}
