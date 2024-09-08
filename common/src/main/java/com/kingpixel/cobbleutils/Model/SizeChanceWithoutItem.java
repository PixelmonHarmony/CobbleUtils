package com.kingpixel.cobbleutils.Model;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 15/07/2024 0:30
 */
@Getter
@ToString
@EqualsAndHashCode
public class SizeChanceWithoutItem {
  private String id;
  private float size;
  private int chance;

  public SizeChanceWithoutItem() {
    this.id = "normal";
    this.size = 1f;
    this.chance = 100;
  }

  public SizeChanceWithoutItem(float size, int chance) {
    this.size = size;
    this.chance = chance;
  }

  public SizeChanceWithoutItem(String id, float size, int chance) {
    this.id = id;
    this.size = size;
    this.chance = chance;
  }

  public static List<SizeChanceWithoutItem> transform(List<SizeChance> pokemonsizes) {
    List<SizeChanceWithoutItem> sizes = new ArrayList<>();
    for (SizeChance sizeChance : pokemonsizes) {
      sizes.add(new SizeChanceWithoutItem(sizeChance.getId(), sizeChance.getSize(), sizeChance.getChance()));
    }
    return sizes;
  }

  public void apply(Pokemon pokemon) {
    if (CobbleUtils.config.isDebug()) {
      CobbleUtils.LOGGER.info("Pokemon: " + pokemon.showdownId() + " ID: " + this.id + " Size: " + this.size +
        " Chance: " + this.chance);
    }
    pokemon.setScaleModifier(this.size);
    pokemon.getPersistentData().putString(CobbleUtilsTags.SIZE_TAG, this.id);
  }
}
