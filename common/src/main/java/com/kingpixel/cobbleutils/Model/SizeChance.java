package com.kingpixel.cobbleutils.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 13/06/2024 10:39
 */
@Getter
@Setter
@ToString
public class SizeChance {
  private String id;
  private float size;
  private int chance;
  private ItemModel item;

  public SizeChance() {
    this.id = "normal";
    this.size = 1f;
    this.chance = 100;
    this.item = new ItemModel("cobblemon:big_root", "", List.of(""));
  }

  public SizeChance(float size, int chance) {
    this.size = size;
    this.chance = chance;
  }

  public SizeChance(String id, float size, int chance) {
    this.id = id;
    this.size = size;
    this.chance = chance;
    this.item = new ItemModel("cobblemon:big_root", "", List.of(""));
  }

}
