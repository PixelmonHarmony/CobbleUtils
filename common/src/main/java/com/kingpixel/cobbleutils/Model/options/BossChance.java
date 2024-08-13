package com.kingpixel.cobbleutils.Model.options;

import com.kingpixel.cobbleutils.Model.ItemChance;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 20/07/2024 9:06
 */
@Getter
@Setter
@ToString
@Data
public class BossChance {
  private String rarity;
  private double chance;
  private int minlevel;
  private int maxlevel;
  private float minsize;
  private float maxsize;
  private int amountrewards;
  private boolean allrewards;
  private PokemonDataBoss pokemons;
  private List<ItemChance> rewards;

  public BossChance() {
    this.rarity = "common";
    this.chance = 0.1;
    this.minlevel = 105;
    this.maxlevel = 110;
    this.minsize = 1.5f;
    this.maxsize = 2.0f;
    this.amountrewards = 1;
    this.allrewards = false;
    this.pokemons = new PokemonDataBoss();
    this.rewards = ItemChance.defaultItemChances();
  }

  public BossChance(String rarity) {
    this.rarity = rarity;
    this.chance = 0.1;
    this.minlevel = 1;
    this.maxlevel = 100;
    this.minsize = 1.5f;
    this.maxsize = 2.0f;
    this.amountrewards = 1;
    this.allrewards = false;
    this.pokemons = new PokemonDataBoss();
    this.rewards = ItemChance.defaultItemChances();
  }

  public BossChance(String rarity, double chance, int minlevel, int maxlevel, float minsize, float maxsize, int amountrewards, List<ItemChance> rewards) {
    this.rarity = rarity;
    this.chance = chance;
    this.minlevel = minlevel;
    this.maxlevel = maxlevel;
    this.minsize = minsize;
    this.maxsize = maxsize;
    this.amountrewards = amountrewards;
    this.rewards = ItemChance.defaultItemChances();
    this.pokemons = new PokemonDataBoss();
    this.allrewards = false;
  }

}
