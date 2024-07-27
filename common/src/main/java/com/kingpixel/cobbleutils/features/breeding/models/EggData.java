package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.pokemon.Pokemon;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.nbt.CompoundTag;

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
  private String nature;
  private String ability;
  private String type;
   

  public EggData() {
    this.species = "";
    this.level = 0;
    this.steps = 0;
    this.nature = "";
    this.ability = "";
    this.type = "";
  }

  public EggData(String species, int level, int steps, String nature, String ability, String type) {
    this.species = species;
    this.level = level;
    this.steps = steps;
    this.nature = nature;
    this.ability = ability;
    this.type = type;
  }

  public static EggData from(Pokemon pokemon) {
    EggData egg = new EggData();
    CompoundTag tag = pokemon.getPersistentData();
    egg.setType(tag.getString("type"));
    egg.setAbility(tag.getString("ability"));
    egg.setNature(tag.getString("nature"));
    egg.setSpecies(tag.getString("species"));
    egg.setLevel(tag.getInt("level"));
    egg.setSteps(tag.getInt("steps"));
    return egg;
  }
}
