package com.kingpixel.cobbleutils.Model.options;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Carlos Varas Alonso - 20/07/2024 6:33
 */
@Getter
@ToString
public class Pokerus {
  private final boolean active;
  private final double multiplier;
  private final int rarity;
  private final boolean roundup;
  private final boolean canspawnwithpokerus;

  public Pokerus() {
    this.active = true;
    this.multiplier = 2;
    this.rarity = 10000;
    this.roundup = true;
    this.canspawnwithpokerus = false;
  }

  public Pokerus(boolean active, double multiplier, int rarity, boolean roundup, boolean canspawnwithpokerus) {
    this.active = active;
    this.multiplier = multiplier;
    this.rarity = rarity;
    this.roundup = roundup;
    this.canspawnwithpokerus = canspawnwithpokerus;
  }

  public static void apply(Pokemon pokemon) {
    pokemon.getPersistentData().putBoolean("pokerus", true);
  }

  public static void applywithrarity(Pokemon pokemon) {
    if (pokemon.getPersistentData().getBoolean("pokerus")) return;
    boolean apply = Utils.RANDOM.nextInt(CobbleUtils.config.getPokerus().getRarity()) == 0;
    if (CobbleUtils.config.isDebug() && apply) {
      CobbleUtils.LOGGER.info("Applying Pokerus to " + pokemon.getDisplayName());
    }
    pokemon.getPersistentData().putBoolean("pokerus", apply);
  }

  public Pokemon apply(Pokemon pokemon, boolean battle) {
    if (pokemon == null) return null;
    if (!active) return pokemon;
    if (pokemon.getPersistentData().getBoolean("pokerus")) return pokemon;
    boolean apply = Utils.RANDOM.nextInt(rarity) == 0;
    if (battle) {
      pokemon.getPersistentData().putBoolean("pokerus", apply);
    } else {
      if (canspawnwithpokerus)
        pokemon.getPersistentData().putBoolean("pokerus", apply);
    }
    if (CobbleUtils.config.isDebug() && apply) {
      CobbleUtils.LOGGER.info("Applying Pokerus to " + pokemon.getDisplayName());
    }
    return pokemon;
  }


}
