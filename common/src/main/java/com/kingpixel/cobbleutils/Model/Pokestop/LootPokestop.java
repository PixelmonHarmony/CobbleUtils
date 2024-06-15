package com.kingpixel.cobbleutils.Model.Pokestop;

import lombok.Getter;
import lombok.ToString;

/**
 * @author Carlos Varas Alonso - 14/06/2024 22:43
 */
@Getter
@ToString
public class LootPokestop {
  private String item;
  private int chance;

  public LootPokestop() {
    this.item = "cobblemon:poke_ball";
    this.chance = 100;
  }

  public LootPokestop(String item) {
    this.item = item;
    this.chance = 100;
  }

  public LootPokestop(String item, int chance) {
    this.item = item;
    this.chance = chance;
  }
}
