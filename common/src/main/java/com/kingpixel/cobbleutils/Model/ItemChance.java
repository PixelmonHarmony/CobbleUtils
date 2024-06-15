package com.kingpixel.cobbleutils.Model;

import lombok.Getter;
import lombok.ToString;

/**
 * @author Carlos Varas Alonso - 13/06/2024 10:19
 */
@Getter
@ToString
public class ItemChance {
  private String item;
  private int chance;

  public ItemChance() {
    this.item = "minecraft:dirt";
    this.chance = 100;
  }

  public ItemChance(String item, int chance) {
    this.item = item;
    this.chance = chance;
  }

}
