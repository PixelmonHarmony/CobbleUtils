package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.pokemon.Pokemon;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 23/07/2024 23:01
 */
@Getter
@Setter
@ToString
public class EggData {
  private String species;
  private int level;
  private int steps;
  private int cycles;
  private String nature;
  private String ability;
  private List<ElementalType> type;


  public void apply(Pokemon pokemon) {

  }

  public static EggData from(Pokemon pokemon) {
    EggData eggData = new EggData();
    eggData.setSpecies(pokemon.getPersistentData().getString("species"));
    eggData.setLevel(pokemon.getPersistentData().getInt("level"));
    eggData.setSteps(pokemon.getPersistentData().getInt("steps"));
    eggData.setNature(pokemon.getPersistentData().getString("nature"));
    eggData.setAbility(pokemon.getPersistentData().getString("ability"));
    ElementalType type1 = ElementalTypes.INSTANCE.get(pokemon.getPersistentData().getString("type1"));
    eggData.getType().add(type1);
    ElementalType type2 = ElementalTypes.INSTANCE.get(pokemon.getPersistentData().getString("type2"));
    if (type2 != null) eggData.getType().add(type2);

    return eggData;
  }

  public void steps(Pokemon pokemon, int stepsremove) {
    if (stepsremove == 0) return;
    this.steps -= stepsremove;

    if (steps <= 0) {
      this.cycles--;
      this.steps = getMaxStepsPerCycle();
    }
    updateSteps(pokemon);
    if (this.steps <= 0 && this.cycles <= 0) {
      apply(pokemon);
    }
  }

  private int getMaxStepsPerCycle() {
    return 200;
  }

  private void updateSteps(Pokemon pokemon) {
    pokemon.getPersistentData().putInt("steps", this.steps);
    pokemon.getPersistentData().putInt("cycles", this.cycles);
  }
}
