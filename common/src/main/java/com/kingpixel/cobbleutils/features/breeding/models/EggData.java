package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ScalePokemonData;
import com.kingpixel.cobbleutils.features.breeding.events.HatchEggEvent;
import com.kingpixel.cobbleutils.features.breeding.util.AdventureBreeding;
import com.kingpixel.cobbleutils.util.ArraysPokemons;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

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

  public static void spawnEgg(ChunkAccess chunkAccess, ServerLevel level) {
    try {
      // Generar una posición aleatoria dentro del chunk
      int offsetX = Utils.RANDOM.nextInt(16);
      int offsetZ = Utils.RANDOM.nextInt(16);

      // Obtener las coordenadas globales del chunk y sumar los offsets
      int worldX = chunkAccess.getPos().getWorldPosition().getX() + offsetX;
      int worldZ = chunkAccess.getPos().getWorldPosition().getZ() + offsetZ;

      // Obtener la altura en las coordenadas globales
      int worldY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, worldX, worldZ);

      // Verificar que la altura no sea menor que -60
      if (worldY < -60) return;

      // Crear y configurar el huevo
      PokemonEntity egg = createEgg(ArraysPokemons.getRandomPokemon(), level);
      PokemonProperties.Companion.parse("uncatchable=true").apply(egg.getPokemon());
      egg.getPokemon().getPersistentData().putBoolean("EggSpawned", true);

      // Log de depuración
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.info("Egg Spawned at: [" + worldX + ", " + worldY + ", " + worldZ + "]");
      }

      egg.getPokemon().setNickname(Component.literal(CobbleUtils.breedconfig.getNameEgg()));
      // Configurar el huevo
      egg.setPersistenceRequired();
      egg.setInvulnerable(true);
      egg.setNoAi(true);
      egg.setPos(worldX, worldY, worldZ);
      egg.setNoGravity(false); // Esto puede ser redundante ya que la gravedad es habilitada por defecto

      // Añadir el huevo al nivel
      level.addFreshEntity(egg);
    } catch (Exception e) {
      CobbleUtils.LOGGER.error("Error spawning egg: " + e.getMessage());
      e.printStackTrace();
    }
  }


  public void EggToPokemon(Pokemon pokemon) {
    if (species == null || species.isEmpty()) {
      species = ArraysPokemons.getRandomPokemon().getSpecies().showdownId();
    }
    PokemonProperties pokemonProperties = PokemonProperties.Companion.parse(species + " " + form);
    pokemonProperties.setForm(form);
    pokemonProperties.apply(pokemon);
    AbilityTemplate abilityTemplate = Abilities.INSTANCE.get(this.ability);
    if (abilityTemplate != null) {
      pokemon.updateAbility(abilityTemplate.create(false));
    }

    pokemon.setLevel(level);
    pokemon.heal();
    pokemon.setNickname(null);
    ScalePokemonData.getScalePokemonData(pokemon).getRandomPokemonSize().apply(pokemon);

    pokemon.getSpecies().getMoves().getEggMoves().forEach(move -> pokemon.getBenchedMoves().add(BenchedMove.Companion.loadFromJSON(move.create().saveToJSON(new JsonObject()))));

    HatchEggEvent.HATCH_EGG_EVENT.emit(pokemon);

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
    pokemon.setNickname(Component.translatable("Egg " + species + " " + this.cycles + "/" + this.steps));
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
    } else if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      egg = EggData.pokemonToEgg(female, false);
    } else if (female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      egg = EggData.pokemonToEgg(male, false);
    } else if (male.getSpecies().showdownId().equalsIgnoreCase(female.getSpecies().showdownId())) {
      egg = EggData.pokemonToEgg(female, false);
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

    applyInitialIvs(egg, male, female);

    // Nature (Done)
    if (male.heldItem().getItem() == CobblemonItems.EVERSTONE) {
      egg.setNature(male.getNature());
    } else if (female.heldItem().getItem() == CobblemonItems.EVERSTONE) {
      egg.setNature(female.getNature());
    } else {
      egg.setNature(Natures.INSTANCE.getRandomNature());
    }

    // Ability

    applyAbility(parent, egg);


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
  }

  private static void applyInitialIvs(Pokemon egg, Pokemon male, Pokemon female) {
    CobblemonItem maleItem = null;
    CobblemonItem femaleItem = null;

    if (male.heldItem().getItem() instanceof CobblemonItem cobblemonItem) {
      maleItem = cobblemonItem;
    }
    if (female.heldItem().getItem() instanceof CobblemonItem cobblemonItem) {
      femaleItem = cobblemonItem;
    }

    if (maleItem != null && maleItem.equals(femaleItem)) {
      applyIvs(male, female, egg, 3);
    } else {
      boolean applied = false;
      if (maleItem != null && maleItem.equals(CobblemonItems.DESTINY_KNOT) || femaleItem != null && femaleItem.equals(CobblemonItems.DESTINY_KNOT)) {
        applyIvs(male, female, egg, 5);
        applied = true;
      }
      if (maleItem != null && isPowerItem(maleItem)) {
        applyPowerItemEffect(maleItem, male, egg);
        applied = true;
      }
      if (femaleItem != null && isPowerItem(femaleItem)) {
        applyPowerItemEffect(femaleItem, female, egg);
        applied = true;
      }
      if (!applied) {
        applyIvs(male, female, egg, 3);
      }
    }
  }

  private static boolean isPowerItem(CobblemonItem item) {
    return item.equals(CobblemonItems.POWER_WEIGHT) ||
      item.equals(CobblemonItems.POWER_BRACER) ||
      item.equals(CobblemonItems.POWER_BELT) ||
      item.equals(CobblemonItems.POWER_LENS) ||
      item.equals(CobblemonItems.POWER_BAND) ||
      item.equals(CobblemonItems.POWER_ANKLET);
  }

  private static void applyPowerItemEffect(CobblemonItem item, Pokemon parent, Pokemon egg) {
    if (item.equals(CobblemonItems.POWER_WEIGHT)) {
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
    }
  }

  private static void applyIvs(Pokemon male, Pokemon female, Pokemon egg, int amount) {
    Stats[] stats = Arrays.stream(Stats.values())
      .filter(stats1 -> stats1 != Stats.EVASION && stats1 != Stats.ACCURACY)
      .toArray(Stats[]::new);

    Set<Stats> inheritedStats = new HashSet<>();

    while (inheritedStats.size() < amount) {
      Stats stat = stats[Utils.RANDOM.nextInt(stats.length)];
      if (inheritedStats.add(stat)) {
        if (Utils.RANDOM.nextBoolean()) {
          egg.setIV(stat, male.getIvs().get(stat));
        } else {
          egg.setIV(stat, female.getIvs().get(stat));
        }
      }
    }

    for (Stats stat : stats) {
      if (!inheritedStats.contains(stat)) {
        egg.setIV(stat, Utils.RANDOM.nextInt(32));
      }
    }
  }

  private static void applyAbility(Pokemon parent, Pokemon egg) {
    Ability hiddenAbility = PokemonUtils.getAH(parent);
    if (hiddenAbility != null && hiddenAbility.getName().equalsIgnoreCase(parent.getAbility().getName())) {
      if (Utils.RANDOM.nextInt(100) < 70) {
        egg.getPersistentData().putString("ability", hiddenAbility.getName());
      } else {
        Ability randomAbility = PokemonUtils.getRandomAbility(parent.getSpecies());
        if (randomAbility != null) egg.getPersistentData().putString("ability", randomAbility.getName());
      }
    } else {
      Ability randomAbility = PokemonUtils.getRandomAbility(parent.getSpecies());
      if (randomAbility != null) egg.getPersistentData().putString("ability", randomAbility.getName());
    }
  }

  private static PokemonEntity createEgg(Pokemon pokemon, Level level) {
    PokemonEntity egg = PokemonProperties.Companion.parse("egg type_egg=" + pokemon.showdownId()).createEntity(level);
    EggData.applyPersistent(egg.getPokemon(), pokemon, pokemon.getSpecies().showdownId(), false);
    return egg;
  }


  private static Pokemon pokemonToEgg(Pokemon pokemon, boolean dittos) {
    String specie = getExcepcionalSpecie(pokemon);
    Pokemon egg = PokemonProperties.Companion.parse("egg type_egg=" + specie).create();
    EggData.applyPersistent(egg, pokemon, specie, dittos);
    return egg;
  }

  private static String getExcepcionalSpecie(Pokemon pokemon) {
    if (false) {
      return ArraysPokemons.getRandomPokemon().getSpecies().showdownId();
    } else {
      return pokemon.getSpecies().showdownId();
    }
  }


  private static void applyPersistent(Pokemon egg, Pokemon pokemon, String specie, boolean dittos) {
    Species s = null;
    Species last = pokemon.getSpecies();
    if (specie == null) {
      do {
        if (pokemon.getSpecies().getPreEvolution() == null) {
          s = pokemon.getSpecies();
        } else {
          s = pokemon.getSpecies().getPreEvolution().getSpecies();
          if (last.showdownId().equalsIgnoreCase(s.showdownId())) break;
          last = s;
        }

      } while (pokemon.getSpecies().getPreEvolution() != null);
    }
    egg.getPersistentData().putString("species", specie == null ? s.showdownId() : specie);
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
            form = modified;
          }
          egg.getPersistentData().putString("form", form);
        }
      }
    }

    egg.getPersistentData().putInt("level", 1);
    egg.getPersistentData().putInt("steps", 200);
    egg.getPersistentData().putInt("cycles", pokemon.getSpecies().getEggCycles());

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
