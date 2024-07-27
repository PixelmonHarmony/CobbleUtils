package com.kingpixel.cobbleutils.Model.options;

import com.kingpixel.cobbleutils.Model.ItemChance;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 20/07/2024 9:06
 */
@Getter

@ToString
public class BossChance {
  private final String rarity;
  private final double chance;
  private final int minlevel;
  private final int maxlevel;
  private final float minsize;
  private final float maxsize;
  private final int amountrewards;
  private final List<ItemChance> rewards;

  public BossChance() {
    this.rarity = "common";
    this.chance = 0.1;
    this.minlevel = 105;
    this.maxlevel = 110;
    this.minsize = 2.5f;
    this.maxsize = 5.0f;
    this.amountrewards = 1;
    this.rewards = List.of(new ItemChance());
  }

  public BossChance(String rarity) {
    this.rarity = rarity;
    this.chance = 0.1;
    this.minlevel = 1;
    this.maxlevel = 100;
    this.minsize = 1;
    this.maxsize = 100;
    this.amountrewards = 1;
    this.rewards = List.of(new ItemChance());
  }

  public BossChance(String rarity, double chance, int minlevel, int maxlevel, float minsize, float maxsize, int amountrewards, List<ItemChance> rewards) {
    this.rarity = rarity;
    this.chance = chance;
    this.minlevel = minlevel;
    this.maxlevel = maxlevel;
    this.minsize = minsize;
    this.maxsize = maxsize;
    this.amountrewards = amountrewards;
    this.rewards = rewards;
  }
}
