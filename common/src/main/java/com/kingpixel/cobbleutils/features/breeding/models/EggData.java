package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ScalePokemonData;
import com.kingpixel.cobbleutils.features.breeding.util.AdventureBreeding;
import com.kingpixel.cobbleutils.util.ArraysPokemons;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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
  private String ability;
  private String size;
  private String form;


  public void EggToPokemon(Pokemon pokemon) {
    PokemonProperties pokemonProperties = PokemonProperties.Companion.parse(species + " " + form);
    pokemonProperties.setForm(form);
    pokemonProperties.apply(pokemon);
    AbilityTemplate abilityTemplate = Abilities.INSTANCE.get(this.ability);
    if (abilityTemplate != null) {
      pokemon.setAbility$common(abilityTemplate.create(false));
    }
    pokemon.setLevel(level);
    pokemon.heal();
    pokemon.setNickname(null);
    pokemon.setShiny(Utils.RANDOM.nextInt((int) Cobblemon.INSTANCE.getConfig().getShinyRate()) == 0);
    removeAllpersistent(pokemon);
  }

  private void removeAllpersistent(Pokemon pokemon) {
    pokemon.getPersistentData().remove("species");
    pokemon.getPersistentData().remove("level");
    pokemon.getPersistentData().remove("steps");
    pokemon.getPersistentData().remove("nature");
    pokemon.getPersistentData().remove("ability");
    pokemon.getPersistentData().remove("cycles");
    pokemon.getPersistentData().remove("size");
    pokemon.getPersistentData().remove("form");

  }


  public static EggData from(Pokemon pokemon) {
    if (pokemon == null) return null;
    EggData eggData = new EggData();
    eggData.setSpecies(pokemon.getPersistentData().getString("species"));
    eggData.setLevel(pokemon.getPersistentData().getInt("level"));
    eggData.setSteps(pokemon.getPersistentData().getInt("steps"));
    eggData.setAbility(pokemon.getPersistentData().getString("ability"));
    eggData.setCycles(pokemon.getPersistentData().getInt("cycles"));
    eggData.setSize(pokemon.getPersistentData().getString("size"));
    eggData.setForm(pokemon.getPersistentData().getString("form"));
    return eggData;
  }

  public void steps(Pokemon pokemon, int stepsremove) {
    if (pokemon == null) return;
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
      EggToPokemon(pokemon);
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
    Pokemon male, Pokemon female, ServerPlayer player) throws NoPokemonStoreException {
    Pokemon egg = new Pokemon();

    if (male.isLegendary() || male.isUltraBeast()) return null;


    if (male.showdownId().equalsIgnoreCase("ditto") && female.showdownId().equalsIgnoreCase("ditto")) {
      egg = EggData.pokemonToEgg(ArraysPokemons.getRandomPokemon(), true);
    } else if (male.showdownId().equalsIgnoreCase("ditto")) {
      egg = EggData.pokemonToEgg(PokemonProperties.Companion.parse(female.showdownId()).create(), false);
    } else if (female.showdownId().equalsIgnoreCase("ditto")) {
      egg = EggData.pokemonToEgg(PokemonProperties.Companion.parse(male.showdownId()).create(), false);
    } else if (male.showdownId().equalsIgnoreCase(female.showdownId()) && male.getGender() == Gender.MALE && female.getGender() == Gender.FEMALE) {
      egg = EggData.pokemonToEgg(female, false);
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

  private static Pokemon pokemonToEgg(Pokemon pokemon, boolean dittos) {
    Pokemon egg = PokemonProperties.Companion.parse("egg type_egg=" + pokemon.getSpecies().showdownId()).create();
    egg.setNature(pokemon.getNature());
    EggData.applyPersistent(egg, pokemon, dittos);
    return egg;
  }


  private static void applyPersistent(Pokemon egg, Pokemon pokemon, boolean dittos) {
    Species s;
    Species last = pokemon.getSpecies();
    do {
      if (pokemon.getSpecies().getPreEvolution() == null) {
        s = pokemon.getSpecies();
      } else {
        s = pokemon.getSpecies().getPreEvolution().getSpecies();
        if (last.showdownId().equalsIgnoreCase(s.showdownId())) break;
        last = s;
      }
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.info("PreEvolution: " + s.showdownId());
      }
    } while (pokemon.getSpecies().getPreEvolution() != null);

    egg.getPersistentData().putString("species", s.showdownId());
    egg.getPersistentData().putString("nature", pokemon.getNature().getName().getPath());
    egg.getPersistentData().putString("ability", pokemon.getAbility().getTemplate().getName().toLowerCase().trim());
    if (dittos) {
      List<FormData> forms = pokemon.getSpecies().getForms();
      if (CobbleUtils.config.isDebug()) {
        forms.forEach(form -> CobbleUtils.LOGGER.info("Form: " + form.getAspects()));
      }
      if (!forms.isEmpty()) {
        int rforms = forms.size() > 1 ? Utils.RANDOM.nextInt(forms.size() - 1) : 0;
        List<String> aspects = forms.get(rforms).getAspects();
        if (!aspects.isEmpty()) {
          int raspect = aspects.size() > 1 ? Utils.RANDOM.nextInt(aspects.size() - 1) : 0;
          egg.getPersistentData().putString("form", aspects.get(raspect));
        } else {
          egg.getPersistentData().putString("form", "");
        }
      } else {
        egg.getPersistentData().putString("form", "");
      }
    } else {
      List<String> aspects = pokemon.getForm().getAspects();
      if (!aspects.isEmpty()) {
        egg.getPersistentData().putString("form", aspects.get(0));
      } else {
        egg.getPersistentData().putString("form", "");
      }
    }

    egg.getPersistentData().putInt("level", 1);
    if (CobbleUtils.config.isDebug()) {
      egg.getPersistentData().putInt("cycles", 0);
      egg.getPersistentData().putInt("steps", 0);
    } else {
      egg.getPersistentData().putInt("steps", 200);
      egg.getPersistentData().putInt("cycles", pokemon.getSpecies().getEggCycles());
    }

    egg.setScaleModifier(ScalePokemonData.getScalePokemonData(pokemon).getRandomPokemonSize().getSize());
    if (dittos) {
      egg.setNickname(Component.literal("Egg Random"));
    } else {
      egg.setNickname(Component.literal("Egg " + pokemon.getSpecies().getTranslatedName().getString()));
    }
  }


  public String getInfo() {
    return "Species: " + species + " Level: " + level + " Steps: " + steps + " Cycles: " + cycles + " Ability: " + ability + " Form:" + form;
  }
}
