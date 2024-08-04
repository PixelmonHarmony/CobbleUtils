package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.JsonObject;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    pokemon.getSpecies().getMoves().getEggMoves().forEach(move -> pokemon.getBenchedMoves().add(BenchedMove.Companion.loadFromJSON(move.create().saveToJSON(new JsonObject()))));

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
    Pokemon male, Pokemon female, ServerPlayer player, PlotBreeding plotBreeding) throws NoPokemonStoreException {
    Pokemon egg;
    if (plotBreeding.getEggs().size() >= CobbleUtils.breedconfig.getMaxeggperplot()) return null;
    if (male == null || female == null) return null;
    if (male.isLegendary() || male.isUltraBeast()) return null;
    if (female.isLegendary() || female.isUltraBeast()) return null;

    if (male.getSpecies().showdownId().equalsIgnoreCase("ditto") && female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      egg = EggData.pokemonToEgg(ArraysPokemons.getRandomPokemon(), true);
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.info("+--------------------------------+");
        CobbleUtils.LOGGER.info("Ditto + Ditto");
        CobbleUtils.LOGGER.info("Male: " + male.getSpecies().showdownId());
        CobbleUtils.LOGGER.info("Female: " + female.getSpecies().showdownId());
        CobbleUtils.LOGGER.info("Egg: " + egg.getPersistentData().getString("species"));
        CobbleUtils.LOGGER.info("+--------------------------------+");
      }
    } else if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      egg = EggData.pokemonToEgg(female, false);
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.info("+--------------------------------+");
        CobbleUtils.LOGGER.info("Ditto + Other");
        CobbleUtils.LOGGER.info("Male: " + male.getSpecies().showdownId());
        CobbleUtils.LOGGER.info("Female: " + female.getSpecies().showdownId());
        CobbleUtils.LOGGER.info("Egg: " + egg.getPersistentData().getString("species"));
        CobbleUtils.LOGGER.info("+--------------------------------+");
      }
    } else if (female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      egg = EggData.pokemonToEgg(male, false);
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.info("+--------------------------------+");
        CobbleUtils.LOGGER.info("Other + Ditto");
        CobbleUtils.LOGGER.info("Male: " + male.getSpecies().showdownId());
        CobbleUtils.LOGGER.info("Female: " + female.getSpecies().showdownId());
        CobbleUtils.LOGGER.info("Egg: " + egg.getPersistentData().getString("species"));
        CobbleUtils.LOGGER.info("+--------------------------------+");
      }
    } else if (male.getSpecies().showdownId().equalsIgnoreCase(female.getSpecies().showdownId())) {
      egg = EggData.pokemonToEgg(female, false);
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.info("+--------------------------------+");
        CobbleUtils.LOGGER.info("Same + Same");
        CobbleUtils.LOGGER.info("Male: " + male.getSpecies().showdownId());
        CobbleUtils.LOGGER.info("Female: " + female.getSpecies().showdownId());
        CobbleUtils.LOGGER.info("Egg: " + egg.getPersistentData().getString("species"));
        CobbleUtils.LOGGER.info("+--------------------------------+");
      }
    } else {
      return null;
    }

    mecanicsLogic(male, female, egg);

    player.sendSystemMessage(
      AdventureBreeding.adventure(
        PokemonUtils.replace(CobbleUtils.breedconfig.getCreateEgg()
            .replace("%egg%", egg.getSpecies().getTranslatedName().getString()),
          List.of(male, female))
      )
    );


    return egg;
  }

  private static void mecanicsLogic(Pokemon male, Pokemon female, Pokemon egg) {


    // IVS
    List<Pokemon> parents = List.of(male, female);
    Pokemon parent = parents.get(Utils.RANDOM.nextInt(parents.size()));

    if (parent.heldItem().getItem() instanceof CobblemonItem item) {
      if (item.equals(CobblemonItems.DESTINY_KNOT)) {
        applyIvs(parent, egg, 5);
      } else if (item.equals(CobblemonItems.POWER_WEIGHT)) {
        egg.setIV(Stats.HP, parent.getIvs().get(Stats.HP));
      } else if (item.equals(CobblemonItems.POWER_BRACER)) {
        egg.setIV(Stats.ATTACK, parent.getIvs().get(Stats.ATTACK));
      } else if (item.equals(CobblemonItems.POWER_BELT)) {
        egg.setIV(Stats.DEFENCE, parent.getIvs().get(Stats.DEFENCE));
      } else if (item.equals(CobblemonItems.POWER_LENS)) {
        egg.setIV(Stats.SPECIAL_ATTACK, parent.getIvs().get(Stats.SPECIAL_ATTACK));
      } else if (item.equals(CobblemonItems.POWER_BAND)) {
        egg.setIV(Stats.SPECIAL_DEFENCE, parent.getIvs().get(Stats.SPECIAL_DEFENCE));
      } else if (item.equals(CobblemonItems.POWER_ANKLET)) {
        egg.setIV(Stats.SPEED, parent.getIvs().get(Stats.SPEED));
      } else {
        applyIvs(parent, egg, 3);
      }
    }

    // Nature
    if (male.heldItem().getItem() == CobblemonItems.EVERSTONE) {
      egg.setNature(male.getNature());
    } else if (female.heldItem().getItem() == CobblemonItems.EVERSTONE) {
      egg.setNature(female.getNature());
    } else {
      egg.setNature(parent.getNature());
    }

    // FriendShip
    egg.setFriendship(255, true);

    // Shiny Rate
    float shinyrate = Cobblemon.INSTANCE.getConfig().getShinyRate();
    float multiplier = CobbleUtils.breedconfig.getMultiplierShiny();

    if (multiplier > 0) {
      if (male.getShiny()) shinyrate /= multiplier;
      if (female.getShiny()) shinyrate /= multiplier;
    }

    if (shinyrate <= 1) {
      egg.setShiny(true);
    } else {
      if (Utils.RANDOM.nextInt((int) shinyrate) == 0) {
        egg.setShiny(true);
      }
    }

    if (CobbleUtils.config.isDebug()) {
      CobbleUtils.LOGGER.info("ShinyRate Egg: " + shinyrate);
    }
  }

  public static void applyIvs(Pokemon parent, Pokemon egg, int amount) {
    Stats[] stats = Stats.values();
    Set<Stats> statsSet = new HashSet<>();

    List<Stats> validStats = Arrays.stream(stats)
      .filter(stat -> stat != Stats.EVASION && stat != Stats.ACCURACY)
      .toList();

    // Añadimos estadísticas al statsSet hasta alcanzar el número deseado
    while (statsSet.size() < amount && !validStats.isEmpty()) {
      Stats stat = validStats.get(Utils.RANDOM.nextInt(validStats.size()));
      if (statsSet.add(stat)) {
        Integer ivValue = parent.getIvs().get(stat);
        if (ivValue != null) {
          if (CobbleUtils.config.isDebug()) {
            CobbleUtils.LOGGER.info("Stat: " + stat);
          }
          egg.setIV(stat, ivValue);
        } else {
          if (CobbleUtils.config.isDebug()) {
            CobbleUtils.LOGGER.warn("IV value for stat " + stat + " is null");
          }
        }
      }
    }

    for (Stats stat : validStats) {
      if (!statsSet.contains(stat)) {
        int randomIvValue = Utils.RANDOM.nextInt(32);
        if (CobbleUtils.config.isDebug()) {
          CobbleUtils.LOGGER.info("Randomizing IV for stat: " + stat + " with value: " + randomIvValue);
        }
        egg.setIV(stat, randomIvValue);
      }
    }
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
      List<String> forms = pokemon.getForm().getAspects();
      if (!forms.isEmpty()) {
        egg.getPersistentData().putString("form", forms.get(0));
      } else {
        egg.getPersistentData().putString("form", "");
      }
      if (CobbleUtils.breedconfig.isObtainAspect()) {
        if (egg.getPersistentData().getString("form").isEmpty()) {
          List<String> aspects = pokemon.getAspects().stream().toList();
          String form = aspects.isEmpty() ? "" : aspects.get(aspects.size() - 1);
          int lastIndex = form.lastIndexOf('-');

          if (lastIndex != -1) {
            String modified = form.substring(0, lastIndex) + '=' + form.substring(lastIndex + 1);
            System.out.println(modified); // Salida: tree=cherry
          } else {
            System.out.println(form); // En caso de que no haya ningún guion en la cadena
          }
          egg.getPersistentData().putString("form", form);
        }
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
