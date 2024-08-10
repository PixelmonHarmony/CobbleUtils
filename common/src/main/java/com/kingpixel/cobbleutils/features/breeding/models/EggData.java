package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.CobbleUtilsTags;
import com.kingpixel.cobbleutils.Model.ScalePokemonData;
import com.kingpixel.cobbleutils.features.breeding.events.HatchEggEvent;
import com.kingpixel.cobbleutils.features.breeding.util.AdventureBreeding;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cobblemon.mod.common.CobblemonItems.*;

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
  private boolean random;

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


      egg.getPokemon().setNickname(Component.literal(CobbleUtils.breedconfig.getNameAbandonedEgg()));

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

    pokemon.setFriendship(255, true);
    pokemon.setLevel(level);
    pokemon.heal();
    pokemon.setNickname(null);

    //pokemon.getSpecies().getMoves().getEggMoves().forEach(move -> pokemon.getBenchedMoves().add(BenchedMove
    // .Companion.loadFromJSON(move.create().saveToJSON(new JsonObject()))));


    removeAllpersistent(pokemon);
    HatchEggEvent.HATCH_EGG_EVENT.emit(pokemon);
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
    pokemon.getPersistentData().remove("random");
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
    eggData.setRandom(pokemon.getPersistentData().getBoolean("random"));
    return eggData;
  }

  public void steps(Pokemon pokemon, int stepsremove) {
    if (pokemon == null) return;
    if (stepsremove == 0) return;
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
      return CobbleUtils.breedconfig.getSteps();
    } else {
      return 0;
    }
  }

  private void updateSteps(Pokemon pokemon) {
    pokemon.getPersistentData().putInt("steps", this.steps);
    pokemon.getPersistentData().putInt("cycles", this.cycles);
    if (random) {
      pokemon.setNickname(Component.literal(CobbleUtils.breedconfig.getNameRandomEgg() + " " + this.cycles + "/" + this.steps));
    } else {
      pokemon.setNickname(Component.literal(CobbleUtils.breedconfig.getNameEgg()
        .replace("%pokemon%", species) + " " + this.cycles + "/" + this.steps));
    }
  }

  public static Pokemon createEgg(
    Pokemon male, Pokemon female, ServerPlayer player, PlotBreeding plotBreeding) throws NoPokemonStoreException {
    Pokemon egg;
    if (plotBreeding.getEggs().size() >= CobbleUtils.breedconfig.getMaxeggperplot()) return null;
    if (male == null || female == null) return null;
    if (male.isLegendary() || male.isUltraBeast()) return null;
    if (female.isLegendary() || female.isUltraBeast()) return null;

    Pokemon usePokemonToEgg;


    if (male.getSpecies().showdownId().equalsIgnoreCase("ditto") && female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (!CobbleUtils.breedconfig.isDoubleditto()) return null;
      do {
        usePokemonToEgg = ArraysPokemons.getRandomPokemon();
      } while (usePokemonToEgg.isUltraBeast() || usePokemonToEgg.isLegendary());
      egg = EggData.pokemonToEgg(usePokemonToEgg, true);
      egg.getPersistentData().putBoolean("random", true);
    } else if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (!CobbleUtils.breedconfig.isDitto()) return null;
      usePokemonToEgg = female;
      egg = EggData.pokemonToEgg(usePokemonToEgg, false);
    } else if (female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (!CobbleUtils.breedconfig.isDitto()) return null;
      usePokemonToEgg = male;
      egg = EggData.pokemonToEgg(usePokemonToEgg, false);
    } else if (male.getSpecies().showdownId().equalsIgnoreCase(female.getSpecies().showdownId())) {
      usePokemonToEgg = female;
      egg = EggData.pokemonToEgg(usePokemonToEgg, false);
    } else {
      if (isCompatible(male, female)) {
        usePokemonToEgg = female;
        egg = EggData.pokemonToEgg(usePokemonToEgg, false);
      } else {
        return null;
      }
    }


    mecanicsLogic(male, female, usePokemonToEgg, egg);
    ScalePokemonData.getScalePokemonData(usePokemonToEgg).getRandomPokemonSize().apply(egg);

    player.sendSystemMessage(
      AdventureBreeding.adventure(
        PokemonUtils.replace(CobbleUtils.breedconfig.getCreateEgg()
            .replace("%egg%", egg.getPersistentData().getString("species")),
          List.of(male, female))
      )
    );


    return egg;
  }

  public static Pokemon createEgg(
    Pokemon male, Pokemon female, ServerPlayer player) throws NoPokemonStoreException {
    Pokemon egg;
    if (male == null || female == null) return null;
    if (male.isLegendary() || male.isUltraBeast()) return null;
    if (female.isLegendary() || female.isUltraBeast()) return null;

    Pokemon usePokemonToEgg;


    if (male.getSpecies().showdownId().equalsIgnoreCase("ditto") && female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (!CobbleUtils.breedconfig.isDoubleditto()) return null;
      do {
        usePokemonToEgg = ArraysPokemons.getRandomPokemon();
      } while (usePokemonToEgg.isUltraBeast() || usePokemonToEgg.isLegendary());
      egg = EggData.pokemonToEgg(usePokemonToEgg, true);
    } else if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (!CobbleUtils.breedconfig.isDitto()) return null;
      usePokemonToEgg = female;
      egg = EggData.pokemonToEgg(usePokemonToEgg, false);
    } else if (female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (!CobbleUtils.breedconfig.isDitto()) return null;
      usePokemonToEgg = male;
      egg = EggData.pokemonToEgg(usePokemonToEgg, false);
    } else if (male.getSpecies().showdownId().equalsIgnoreCase(female.getSpecies().showdownId())) {
      usePokemonToEgg = female;
      egg = EggData.pokemonToEgg(usePokemonToEgg, false);
    } else {
      if (isCompatible(male, female)) {
        usePokemonToEgg = female;
        egg = EggData.pokemonToEgg(usePokemonToEgg, false);
      } else {
        return null;
      }
    }


    mecanicsLogic(male, female, usePokemonToEgg, egg);
    ScalePokemonData.getScalePokemonData(usePokemonToEgg).getRandomPokemonSize().apply(egg);

    player.sendSystemMessage(
      AdventureBreeding.adventure(
        PokemonUtils.replace(CobbleUtils.breedconfig.getCreateEgg()
            .replace("%egg%", egg.getPersistentData().getString("species")),
          List.of(male, female))
      )
    );


    return egg;
  }

  public static boolean isCompatible(Pokemon male, Pokemon female) {
    return female.getSpecies().getEggGroups().stream()
      .anyMatch(eggGroup -> male.getSpecies().getEggGroups().contains(eggGroup));
  }

  private static void mecanicsLogic(Pokemon male, Pokemon female, Pokemon usePokemonToEgg, Pokemon egg) {
    Species firstEvolution = PokemonUtils.getFirstEvolution(usePokemonToEgg.getSpecies());

    // IVS
    egg.createPokemonProperties(List.of(PokemonPropertyExtractor.IVS, PokemonPropertyExtractor.GENDER)).apply(egg);
    applyInitialIvs(egg, male, female);

    boolean isDoubleEverStone = (male.heldItem().getItem() == CobblemonItems.EVERSTONE && female.heldItem().getItem() == CobblemonItems.EVERSTONE);

    // Nature (Done)
    if (isDoubleEverStone) {
      List<Pokemon> parents = List.of(male, female);
      egg.setNature(parents.get(Utils.RANDOM.nextInt(parents.size())).getNature());
    } else if (male.heldItem().getItem() == CobblemonItems.EVERSTONE) {
      egg.setNature(male.getNature());
    } else if (female.heldItem().getItem() == CobblemonItems.EVERSTONE) {
      egg.setNature(female.getNature());
    } else {
      egg.setNature(Natures.INSTANCE.getRandomNature());
    }

    // Ability
    applyAbility(male, female, firstEvolution, egg);


    // FriendShip
    egg.setFriendship(255, true);

    // Shiny Rate
    float shinyrate = Cobblemon.INSTANCE.getConfig().getShinyRate();
    float multiplier = CobbleUtils.breedconfig.getMultiplierShiny();

    if (multiplier > 0) {
      if (male.getShiny()) shinyrate /= multiplier;
      if (female.getShiny()) shinyrate /= multiplier;
    }

    if (CobbleUtils.breedconfig.isMethodmasuda()) {
      String maleCountry = male.getPersistentData().getString(CobbleUtilsTags.COUNTRY_TAG);
      String femaleCountry = female.getPersistentData().getString(CobbleUtilsTags.COUNTRY_TAG);
      if (!maleCountry.isEmpty() && !femaleCountry.isEmpty()) {
        if (!maleCountry.equalsIgnoreCase(femaleCountry)) {
          shinyrate /= CobbleUtils.breedconfig.getMultipliermasuda();
        }
      }
    }

    if (shinyrate <= 1) {
      egg.setShiny(true);
    } else {
      if (Utils.RANDOM.nextInt((int) shinyrate) == 0) {
        egg.setShiny(true);
      }
    }

    // Gender
    egg.createPokemonProperties(PokemonPropertyExtractor.GENDER).apply(egg);

    // Egg Moves
/*    List<Move> eggMoves = new ArrayList<>();
    male.getMoveSet().forEach(move -> {
      PokemonUtils.isEggMove(male, move);
    });*/
  }

  private static void applyInitialIvs(Pokemon egg, Pokemon male, Pokemon female) {
    CobblemonItem maleItem = null;
    CobblemonItem femaleItem = null;
    CobblemonItem ItemSame = null;
    boolean sameItem = false;
    if (male.heldItem().getItem() instanceof CobblemonItem cobblemonItem) {
      maleItem = cobblemonItem;
    }
    if (female.heldItem().getItem() instanceof CobblemonItem cobblemonItem) {
      femaleItem = cobblemonItem;
    }

    if (maleItem != null && femaleItem != null) {
      ItemSame = maleItem;
      sameItem = maleItem.equals(femaleItem);
    }

    List<Pokemon> parents = List.of(male, female);
    Pokemon random = parents.get(Utils.RANDOM.nextInt(parents.size()));
    if (ItemSame == CobblemonItems.DESTINY_KNOT) {
      applyDestinyKnot(male, female, egg);
    } else if (sameItem) {
      applyIvsPower(random, egg, ItemSame);
    } else {
      applyIvs(male, maleItem, female, femaleItem, egg);
    }

  }

  private static void applyIvs(Pokemon male, CobblemonItem maleItem, Pokemon female, CobblemonItem femaleItem, Pokemon egg) {
    if (maleItem != null) {
      logicIvs(male, female, maleItem, egg);
    }
    if (femaleItem != null) {
      logicIvs(male, female, femaleItem, egg);
    }
  }

  private static void logicIvs(Pokemon male, Pokemon female, CobblemonItem item, Pokemon egg) {
    if (item == DESTINY_KNOT) {
      applyDestinyKnot(male, female, egg);
    } else {
      applyIvsPower(male, egg, item);
    }
  }


  private static void applyIvsPower(Pokemon random, Pokemon egg, CobblemonItem itemSame) {
    if (itemSame.equals(POWER_WEIGHT)) {
      egg.setIV(Stats.HP, random.getIvs().get(Stats.HP));
    } else if (itemSame.equals(POWER_BRACER)) {
      egg.setIV(Stats.ATTACK, random.getIvs().get(Stats.ATTACK));
    } else if (itemSame.equals(POWER_BELT)) {
      egg.setIV(Stats.DEFENCE, random.getIvs().get(Stats.DEFENCE));
    } else if (itemSame.equals(POWER_ANKLET)) {
      egg.setIV(Stats.SPEED, random.getIvs().get(Stats.SPEED));
    } else if (itemSame.equals(POWER_LENS)) {
      egg.setIV(Stats.SPECIAL_ATTACK, random.getIvs().get(Stats.SPECIAL_ATTACK));
    } else if (itemSame.equals(POWER_BAND)) {
      egg.setIV(Stats.SPECIAL_DEFENCE, random.getIvs().get(Stats.SPECIAL_DEFENCE));
    }
  }

  private static void applyDestinyKnot(Pokemon male, Pokemon female, Pokemon egg) {
    List<Stats> stats = new ArrayList<>(Arrays.stream(Stats.values()).toList());
    stats.remove(Stats.EVASION);
    stats.remove(Stats.ACCURACY);
    for (int i = 0; i < 5; i++) {
      List<Pokemon> pokemons = List.of(male, female);
      Pokemon pokemon = pokemons.get(Utils.RANDOM.nextInt(pokemons.size()));
      Stats stat = stats.get(Utils.RANDOM.nextInt(stats.size()));
      Integer n = pokemon.getIvs().get(stat);
      if (n == null) {
        n = Utils.RANDOM.nextInt(32);
      }
      egg.setIV(stat, n);
    }
  }


  private static void applyAbility(Pokemon male, Pokemon female, Species firstEvolution, Pokemon egg) {
    boolean maleHiddenAbility = PokemonUtils.haveAH(male);
    boolean femaleHiddenAbility = PokemonUtils.haveAH(female);
    Ability obtainHability = null;
    if (maleHiddenAbility && femaleHiddenAbility) {
      if (Utils.RANDOM.nextInt(100) < 70) {
        egg.getPersistentData().putString("ability", obtainHability.getName());
      } else {
        Ability randomAbility = PokemonUtils.getRandomAbility(firstEvolution);
        if (randomAbility != null) egg.getPersistentData().putString("ability", randomAbility.getName());
      }
    } else {
      Ability randomAbility = PokemonUtils.getRandomAbility(firstEvolution);
      if (randomAbility != null) egg.getPersistentData().putString("ability", randomAbility.getName());
    }
  }

  private static PokemonEntity createEgg(Pokemon pokemon, Level level) {
    PokemonEntity egg = PokemonProperties.Companion.parse("egg type_egg=" + pokemon.showdownId()).createEntity(level);
    List<PokemonPropertyExtractor> pokemonProperties = new ArrayList<>();
    pokemonProperties.add(PokemonPropertyExtractor.IVS);
    pokemonProperties.add(PokemonPropertyExtractor.ABILITY);
    pokemonProperties.add(PokemonPropertyExtractor.GENDER);
    pokemonProperties.add(PokemonPropertyExtractor.NATURE);
    egg.getPokemon().createPokemonProperties(pokemonProperties).apply(egg);
    ScalePokemonData.getScalePokemonData(pokemon).getRandomPokemonSize().apply(egg.getPokemon());
    String specie = getExcepcionalSpecie(pokemon);
    EggData.applyPersistent(egg.getPokemon(), pokemon, specie, false);
    return egg;
  }


  private static Pokemon pokemonToEgg(Pokemon pokemon, boolean dittos) {
    String specie = getExcepcionalSpecie(pokemon);
    Pokemon egg = PokemonProperties.Companion.parse("egg type_egg=" + specie).create();
    EggData.applyPersistent(egg, pokemon, specie, dittos);
    return egg;
  }

  private static List<String> pokemonExceptions = new ArrayList<>();

  static {
    pokemonExceptions.add("snorlax");
  }

  private static String getExcepcionalSpecie(Pokemon pokemon) {
    pokemonExceptions.forEach(
      pokemonException -> CobbleUtils.LOGGER.info("Pokemon Exception: " + pokemonException)
    );
    if (pokemonExceptions.contains(pokemon.getSpecies().showdownId())) {
      return pokemon.getSpecies().showdownId();
    } else {
      return null;
    }
  }


  private static void applyPersistent(Pokemon egg, Pokemon pokemon, String species, boolean dittos) {
    Species firstEvolution;
    if (species == null) {
      firstEvolution = PokemonUtils.getFirstEvolution(pokemon.getSpecies());
    } else {
      firstEvolution = PokemonProperties.Companion.parse(species).create().getSpecies();
    }

    egg.getPersistentData().putString("species", firstEvolution.showdownId());
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
    egg.getPersistentData().putInt("steps", CobbleUtils.breedconfig.getSteps());
    egg.getPersistentData().putInt("cycles", pokemon.getSpecies().getEggCycles());

    egg.setScaleModifier(ScalePokemonData.getScalePokemonData(pokemon).getRandomPokemonSize().getSize());
    if (dittos) {
      egg.setNickname(Component.literal(CobbleUtils.breedconfig.getNameAbandonedEgg()));
      egg.getPersistentData().putBoolean("random", true);
    } else {
      egg.setNickname(AdventureTranslator.toNativeComponent(
        PokemonUtils.replace(
          CobbleUtils.breedconfig.getNameEgg(), pokemon
        )
      ));
    }
  }


  public String getInfo() {
    return "Species: " + species + " Level: " + level + " Steps: " + steps + " Cycles: " + cycles + " Ability: " + ability + " Form:" + form;
  }
}
