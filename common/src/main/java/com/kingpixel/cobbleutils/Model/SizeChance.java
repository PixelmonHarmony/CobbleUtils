package com.kingpixel.cobbleutils.Model;

import lombok.Getter;
import lombok.ToString;

/**
 * @author Carlos Varas Alonso - 13/06/2024 10:39
 */
@Getter
@ToString
public class SizeChance {
  private String id;
  private float size;
  private int chance;

  public SizeChance() {
    this.id = "normal";
    this.size = 1f;
    this.chance = 100;
  }

  public SizeChance(float size, int chance) {
    this.size = size;
    this.chance = chance;
  }

  public SizeChance(String id, float size, int chance) {
    this.id = id;
    this.size = size;
    this.chance = chance;
  }

}
