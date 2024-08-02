package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.util.AdventureBreeding;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

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
  private String size;


  public void apply(Pokemon pokemon) {
    PokemonProperties.Companion.parse(species).apply(pokemon);
    pokemon.setAbility$common(Objects.requireNonNull(Abilities.INSTANCE.get(this.ability)).create(true));
    pokemon.setLevel(level);
    pokemon.setNature(Objects.requireNonNull(Natures.INSTANCE.getNature(this.nature)));
    pokemon.heal();
    pokemon.setNickname(null);
    pokemon.setShiny(Utils.RANDOM.nextInt((int) Cobblemon.INSTANCE.getConfig().getShinyRate()) == 0);
    removeall(pokemon);
  }

  private void removeall(Pokemon pokemon) {
    pokemon.getPersistentData().remove("species");
    pokemon.getPersistentData().remove("level");
    pokemon.getPersistentData().remove("steps");
    pokemon.getPersistentData().remove("nature");
    pokemon.getPersistentData().remove("ability");
    pokemon.getPersistentData().remove("cycles");
  }


  public static EggData from(Pokemon pokemon) {
    EggData eggData = new EggData();
    eggData.setSpecies(pokemon.getPersistentData().getString("species"));
    eggData.setLevel(pokemon.getPersistentData().getInt("level"));
    eggData.setSteps(pokemon.getPersistentData().getInt("steps"));
    eggData.setNature(pokemon.getPersistentData().getString("nature"));
    eggData.setAbility(pokemon.getPersistentData().getString("ability"));
    eggData.setCycles(pokemon.getPersistentData().getInt("cycles"));
    eggData.setSize(pokemon.getPersistentData().getString("size"));


    return eggData;
  }

  public void steps(Pokemon pokemon, int stepsremove) {
    if (stepsremove == 0) return;
    if (CobbleUtils.config.isDebug()) {
      CobbleUtils.LOGGER.info(this.toString());
    }
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
    if (cycles > 0) {
      return 200;
    } else {
      return 0;
    }
  }

  private void updateSteps(Pokemon pokemon) {
    pokemon.getPersistentData().putInt("steps", this.steps);
    pokemon.getPersistentData().putInt("cycles", this.cycles);
  }

  public static Pokemon createEgg(
    @NotNull Pokemon male, @NotNull Pokemon female, @NotNull ServerPlayer player) throws NoPokemonStoreException {
    Pokemon egg = new Pokemon();

    if (male.isLegendary() || male.isUltraBeast()) {
      return null;
    }

    if (male.showdownId().equalsIgnoreCase("ditto") && female.showdownId().equalsIgnoreCase("ditto")) {
      PokemonProperties.Companion.parse("random").apply(egg);
    } else if (male.showdownId().equalsIgnoreCase("ditto")) {
      PokemonProperties.Companion.parse(female.showdownId()).apply(egg);
    } else if (female.showdownId().equalsIgnoreCase("ditto")) {
      PokemonProperties.Companion.parse(male.showdownId()).apply(egg);
    }

    player.sendSystemMessage(
      AdventureBreeding.adventure(
        PokemonUtils.replace(CobbleUtils.breedconfig.getCreateEgg()
            .replace("%egg%", egg.getSpecies().getTranslatedName().getString()),
          List.of(male, female))
      )
    );


    return egg;
  }

  public String getInfo() {
    return "Species: " + species + " Level: " + level + " Steps: " + steps + " Cycles: " + cycles + " Nature: " + nature + " Ability: " + ability;
  }
}
