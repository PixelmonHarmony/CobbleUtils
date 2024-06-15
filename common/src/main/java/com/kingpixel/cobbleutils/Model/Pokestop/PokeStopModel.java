package com.kingpixel.cobbleutils.Model.Pokestop;

import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 14/06/2024 22:42
 */
@Getter
@ToString
public class PokeStopModel {
  private String type;
  private int amountrewards;
  private int cooldown;
  private List<LootPokestop> loot;

  public PokeStopModel() {
    this.type = "default";
    this.amountrewards = 3;
    this.cooldown = 30;
    this.loot = List.of(new LootPokestop(), new LootPokestop(), new LootPokestop());
  }

  public PokeStopModel(String type) {
    this.type = type;
    this.amountrewards = 3;
    this.cooldown = 30;
    this.loot = List.of(new LootPokestop(), new LootPokestop(), new LootPokestop());
  }

  public PokeStopModel(String type, int amountrewards, int cooldown, List<LootPokestop> loot) {
    this.type = type;
    this.amountrewards = amountrewards;
    this.cooldown = cooldown;
    this.loot = loot;
  }

  public List<LootPokestop> generateRewards() {
    List<LootPokestop> rewards = new ArrayList<>();
    for (int i = 0; i < amountrewards; i++) {
      int totalWeight = loot.stream().mapToInt(LootPokestop::getChance).sum();
      int randomWeight = Utils.RANDOM.nextInt(totalWeight);
      int currentWeight = 0;

      for (LootPokestop item : loot) {
        currentWeight += item.getChance();
        if (randomWeight < currentWeight) {
          rewards.add(item);
          break;
        }
      }
    }
    return rewards;
  }

  public int getTotalWeight() {
    return loot.stream()
      .mapToInt(LootPokestop::getChance)
      .sum();
  }

}
