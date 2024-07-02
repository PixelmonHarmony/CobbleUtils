package com.kingpixel.cobbleutils.Model;

import lombok.Getter;

/**
 * @author Carlos Varas Alonso - 25/06/2024 22:17
 */
@Getter
public class MoneyChance {
  private int money;
  private int chance;

  public MoneyChance() {
    this.money = 1;
    this.chance = 100;
  }

  public MoneyChance(int money, int chance) {
    this.money = money;
    this.chance = chance;
  }
  
}
