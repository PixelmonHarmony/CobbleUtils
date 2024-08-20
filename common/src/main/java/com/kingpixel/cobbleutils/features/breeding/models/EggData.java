package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
  private String moves;
  private boolean random;

  public static PokemonEntity spawnEgg(Chunk chunkAccess, ServerWorld level) {
    try {
      // Generar una posición aleatoria dentro del chunk
      int offsetX = Utils.RANDOM.nextInt(16);
      int offsetZ = Utils.RANDOM.nextInt(16);

      // Obtener las coordenadas globales del chunk y sumar los offsets
      int worldX = chunkAccess.getPos().getStartPos().getX() + offsetX;
      int worldZ = chunkAccess.getPos().getStartPos().getZ() + offsetZ;

      // Obtener la altura en las coordenadas globales
      int worldY = level.getTopY(Heightmap.Type.MOTION_BLOCKING, worldX, worldZ);

      // Verificar que la altura no sea menor que -60
      if (worldY < -60) return null;

      // Crear y configurar el huevo
      PokemonEntity egg = createEgg(ArraysPokemons.getRandomPokemon(), level);
      PokemonProperties.Companion.parse("uncatchable=true").apply(egg.getPokemon());
      egg.getPokemon().getPersistentData().putBoolean("EggSpawned", true);

      // Log de depuración
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.info("Egg Spawned at: [" + worldX + ", " + worldY + ", " + worldZ + "]");
      }

      egg.getPokemon().setNickname(Text.literal(CobbleUtils.breedconfig.getNameAbandonedEgg()));

      // Configurar el huevo
      egg.setPersistent();
      egg.setInvulnerable(true);
      egg.setAiDisabled(true);
      egg.setPos(worldX, worldY, worldZ);
      egg.setNoGravity(false); // Esto puede ser redundante ya que la gravedad es habilitada por defecto

      // Añadir el huevo al nivel
      return egg;
    } catch (Exception e) {
      CobbleUtils.LOGGER.error("Error spawning egg: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
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


    if (moves != null && !moves.isEmpty()) {
      try {
        // Parsear el JSON string como un JsonObject
        JsonObject jsonObject = JsonParser.parseString(moves).getAsJsonObject();

        // Obtener el JsonArray bajo la clave "moves"
        JsonArray jsonArray = jsonObject.getAsJsonArray("moves");
        CobbleUtils.LOGGER.info("JsonArray Moves: " + jsonArray.toString());
        jsonArray.forEach(element -> {
          CobbleUtils.LOGGER.info(element.getAsString());

          // Validar y agregar los movimientos a BenchedMoves
          Move move = Moves.INSTANCE.getByName(element.getAsString()).create();
          if (move != null) {
            JsonObject moveJson = move.saveToJSON(new JsonObject());
            BenchedMove benchedMove = BenchedMove.Companion.loadFromJSON(moveJson);
            pokemon.getBenchedMoves().add(benchedMove);
          } else {
            CobbleUtils.LOGGER.error("Move not found: " + element.getAsString());
          }
        });
      } catch (Exception e) {
        CobbleUtils.LOGGER.error("Error to process JSON ARRAY: " + e.getMessage());
      }
    }


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
    pokemon.getPersistentData().remove("moves");
  }

  public static EggData from(Pokemon pokemon) {
    if (pokemon == null)
      return null;
    EggData eggData = new EggData();
    eggData.setSpecies(pokemon.getPersistentData().getString("species"));
    eggData.setLevel(pokemon.getPersistentData().getInt("level"));
    eggData.setSteps(pokemon.getPersistentData().getInt("steps"));
    eggData.setAbility(pokemon.getPersistentData().getString("ability"));
    eggData.setCycles(pokemon.getPersistentData().getInt("cycles"));
    eggData.setSize(pokemon.getPersistentData().getString("size"));
    eggData.setForm(pokemon.getPersistentData().getString("form"));
    eggData.setRandom(pokemon.getPersistentData().getBoolean("random"));
    eggData.setMoves(pokemon.getPersistentData().getString("moves"));
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
      pokemon.setNickname(
        Text.literal(CobbleUtils.breedconfig.getNameRandomEgg() + " " + this.cycles + "/" + this.steps));
    } else {
      pokemon.setNickname(Text.literal(CobbleUtils.breedconfig.getNameEgg()
        .replace("%pokemon%", species) + " " + this.cycles + "/" + this.steps));
    }
  }

  public static Pokemon createEgg(
    Pokemon male, Pokemon female, ServerPlayerEntity player, PlotBreeding plotBreeding)
    throws NoPokemonStoreException {
    Pokemon egg;
    if (plotBreeding.getEggs().size() >= CobbleUtils.breedconfig.getMaxeggperplot())
      return null;
    if (male == null || female == null)
      return null;
    if (male.isLegendary() || male.isUltraBeast())
      return null;
    if (female.isLegendary() || female.isUltraBeast())
      return null;

    Pokemon usePokemonToEgg;

    if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")
      && female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (!CobbleUtils.breedconfig.isDoubleditto())
        return null;
      do {
        usePokemonToEgg = ArraysPokemons.getRandomPokemon();
      } while (usePokemonToEgg.isUltraBeast() || usePokemonToEgg.isLegendary());
      egg = EggData.pokemonToEgg(usePokemonToEgg, true);
      egg.getPersistentData().putBoolean("random", true);
    } else if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (!CobbleUtils.breedconfig.isDitto())
        return null;
      usePokemonToEgg = female;
      egg = EggData.pokemonToEgg(usePokemonToEgg, false);
    } else if (female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (!CobbleUtils.breedconfig.isDitto())
        return null;
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

    player.sendMessage(
      AdventureBreeding.adventure(
        PokemonUtils.replace(CobbleUtils.breedconfig.getCreateEgg()
            .replace("%egg%", egg.getPersistentData().getString("species")),
          List.of(male, female))));

    return egg;
  }

  public static Pokemon createEgg(
    Pokemon male, Pokemon female, ServerPlayerEntity player) throws NoPokemonStoreException {
    Pokemon egg;
    if (male == null || female == null)
      return null;
    if (male.isLegendary() || male.isUltraBeast())
      return null;
    if (female.isLegendary() || female.isUltraBeast())
      return null;

    Pokemon usePokemonToEgg;

    if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")
      && female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (!CobbleUtils.breedconfig.isDoubleditto())
        return null;
      do {
        usePokemonToEgg = ArraysPokemons.getRandomPokemon();
      } while (usePokemonToEgg.isUltraBeast() || usePokemonToEgg.isLegendary());
      egg = EggData.pokemonToEgg(usePokemonToEgg, true);
    } else if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (!CobbleUtils.breedconfig.isDitto())
        return null;
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

    player.sendMessage(
      AdventureBreeding.adventure(
        PokemonUtils.replace(CobbleUtils.breedconfig.getCreateEgg()
            .replace("%egg%", egg.getPersistentData().getString("species")),
          List.of(male, female))));

    return egg;
  }

  public static boolean isCompatible(Pokemon male, Pokemon female) {
    if (male.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED)) return false;
    if (female.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED)) return false;
    return female.getForm().getEggGroups().stream()
      .anyMatch(eggGroup -> male.getForm().getEggGroups().contains(eggGroup));
  }

  private static void mecanicsLogic(Pokemon male, Pokemon female, Pokemon usePokemonToEgg, Pokemon egg) {
    Species firstspecie = PokemonUtils.getFirstEvolution(usePokemonToEgg.getSpecies());
    Pokemon firstEvolution =
      PokemonProperties.Companion.parse(firstspecie.showdownId() + " form=" + female.getForm().showdownId() + " " + female.getForm().showdownId() + " " + female.getForm().getName()).create();
    firstEvolution.setForm(female.getForm());
    // IVS
    egg.createPokemonProperties(List.of(PokemonPropertyExtractor.IVS, PokemonPropertyExtractor.GENDER)).apply(egg);
    applyInitialIvs(egg, male, female);

    boolean isDoubleEverStone = (male.heldItem().getItem() == CobblemonItems.EVERSTONE
      && female.heldItem().getItem() == CobblemonItems.EVERSTONE);

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
      if (male.getShiny())
        shinyrate /= multiplier;
      if (female.getShiny())
        shinyrate /= multiplier;
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
    List<String> moves = new ArrayList<>();
    male.getMoveSet().getMoves().forEach(move -> moves.add(move.getName()));
    female.getMoveSet().getMoves().forEach(move -> moves.add(move.getName()));

    List<String> names = new ArrayList<>();
    female.getForm().getMoves().getEggMoves().forEach(eggmove -> {
      if (moves.contains(eggmove.getName())) {
        names.add(eggmove.getName());
      }
    });

    /*if (names.isEmpty()) {
      List<MoveTemplate> movesegg = female.getForm().getMoves().getEggMoves();
      if (!movesegg.isEmpty()) {
        names.add(movesegg.get(Utils.RANDOM.nextInt(movesegg.size())).getName());
      }
    }*/

    if (!names.isEmpty()) {
      JsonArray jsonArray = new JsonArray();
      names.forEach(jsonArray::add);

      JsonObject jsonObject = new JsonObject();
      jsonObject.add("moves", jsonArray);

      egg.getPersistentData().putString("moves", jsonObject.toString());
    }
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
    Pokemon select = parents.get(Utils.RANDOM.nextInt(parents.size()));
    if (ItemSame == CobblemonItems.DESTINY_KNOT) {
      applyDestinyKnot(male, female, egg);
    } else if (sameItem) {
      applyIvsPower(select, egg, ItemSame);
    } else {
      applyIvs(male, maleItem, female, femaleItem, egg);
    }

  }

  private static void applyIvs(Pokemon male, CobblemonItem maleItem, Pokemon female, CobblemonItem femaleItem,
                               Pokemon egg) {
    if (hasPowerItem(male)) {
      if (femaleItem != null) {
        logicIvs(male, female, female, femaleItem, egg);
      }
      if (maleItem != null) {
        logicIvs(male, female, male, maleItem, egg);
      }
    } else if (hasPowerItem(female)) {
      if (maleItem != null) {
        logicIvs(male, female, male, maleItem, egg);
      }
      if (femaleItem != null) {
        logicIvs(male, female, female, femaleItem, egg);
      }
    } else {
      if (maleItem != null) {
        logicIvs(male, female, male, maleItem, egg);
      }
      if (femaleItem != null) {
        logicIvs(male, female, female, femaleItem, egg);
      }
    }

  }

  private static void logicIvs(Pokemon male, Pokemon female, Pokemon select, CobblemonItem item, Pokemon egg) {
    if (item == DESTINY_KNOT) {
      applyDestinyKnot(male, female, egg);
    } else if (isPowerItem(item)) {
      applyIvsPower(select, egg, item);
    } else {
      // Todo: Do something ?
    }
  }

  private static boolean isPowerItem(CobblemonItem item) {
    return item == POWER_WEIGHT ||
      item == POWER_BRACER ||
      item == POWER_BELT ||
      item == POWER_ANKLET ||
      item == POWER_LENS ||
      item == POWER_BAND;
  }

  private static boolean hasPowerItem(Pokemon pokemon) {
    return pokemon.heldItem().getItem() == POWER_WEIGHT ||
      pokemon.heldItem().getItem() == POWER_BRACER ||
      pokemon.heldItem().getItem() == POWER_BELT ||
      pokemon.heldItem().getItem() == POWER_ANKLET ||
      pokemon.heldItem().getItem() == POWER_LENS ||
      pokemon.heldItem().getItem() == POWER_BAND;
  }

  private static void applyIvsPower(Pokemon select, Pokemon egg, CobblemonItem itemSame) {
    if (itemSame.equals(POWER_WEIGHT)) {
      egg.setIV(Stats.HP, select.getIvs().get(Stats.HP));
    } else if (itemSame.equals(POWER_BRACER)) {
      egg.setIV(Stats.ATTACK, select.getIvs().get(Stats.ATTACK));
    } else if (itemSame.equals(POWER_BELT)) {
      egg.setIV(Stats.DEFENCE, select.getIvs().get(Stats.DEFENCE));
    } else if (itemSame.equals(POWER_ANKLET)) {
      egg.setIV(Stats.SPEED, select.getIvs().get(Stats.SPEED));
    } else if (itemSame.equals(POWER_LENS)) {
      egg.setIV(Stats.SPECIAL_ATTACK, select.getIvs().get(Stats.SPECIAL_ATTACK));
    } else if (itemSame.equals(POWER_BAND)) {
      egg.setIV(Stats.SPECIAL_DEFENCE, select.getIvs().get(Stats.SPECIAL_DEFENCE));
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
      Integer ivsPokemon = pokemon.getIvs().get(stat);

      if (ivsPokemon == null) {
        ivsPokemon = Utils.RANDOM.nextInt(32);
      }

      if (ivsPokemon > egg.getIvs().get(stat)) {
        egg.setIV(stat, ivsPokemon);
      }

    }
  }

  private static void applyAbility(Pokemon male, Pokemon female, Pokemon firstEvolution, Pokemon egg) {
    boolean maleHiddenAbility = PokemonUtils.haveAH(male);
    boolean femaleHiddenAbility = PokemonUtils.haveAH(female);
    if (maleHiddenAbility || femaleHiddenAbility) {
      if (Utils.RANDOM.nextInt(100) < 70) {
        egg.getPersistentData().putString("ability", PokemonUtils.getAH(female).getName());
      } else {
        Ability randomAbility = PokemonUtils.getRandomAbility(female);
        egg.getPersistentData().putString("ability", randomAbility.getName());
      }
    } else {
      Ability randomAbility = PokemonUtils.getRandomAbility(female);
      egg.getPersistentData().putString("ability", randomAbility.getName());
    }
  }

  private static PokemonEntity createEgg(Pokemon pokemon, World level) {
    PokemonEntity egg = PokemonProperties.Companion.parse("egg type_egg=" + pokemon.showdownId()).createEntity(level);
    List<PokemonPropertyExtractor> pokemonProperties = new ArrayList<>();
    pokemonProperties.add(PokemonPropertyExtractor.IVS);
    pokemonProperties.add(PokemonPropertyExtractor.ABILITY);
    pokemonProperties.add(PokemonPropertyExtractor.GENDER);
    pokemonProperties.add(PokemonPropertyExtractor.NATURE);
    egg.getPokemon().createPokemonProperties(pokemonProperties).apply(egg);
    ScalePokemonData.getScalePokemonData(pokemon).getRandomPokemonSize().apply(egg.getPokemon());
    EggData.applyPersistent(egg.getPokemon(), pokemon, null, false);
    return egg;
  }

  private static Pokemon pokemonToEgg(Pokemon usePokemon, boolean dittos) {
    String specie = getExcepcionalSpecie(usePokemon);
    Pokemon egg = PokemonProperties.Companion.parse("egg type_egg=" + specie).create();
    EggData.applyPersistent(egg, usePokemon, specie, dittos);
    return egg;
  }


  private static String getExcepcionalSpecie(Pokemon pokemon) {
    AtomicReference<String> s = new AtomicReference<>();
    CobbleUtils.breedconfig.getIncenses().forEach(incense -> {
      if (s.get() == null) {
        s.set(incense.getChild(pokemon));
      }
    });
    return s.get();
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
      egg.setNickname(Text.literal(CobbleUtils.breedconfig.getNameAbandonedEgg()));
      egg.getPersistentData().putBoolean("random", true);
    } else {
      egg.setNickname(AdventureTranslator.toNativeComponent(
        PokemonUtils.replace(
          CobbleUtils.breedconfig.getNameEgg(), pokemon)));
    }
  }

  public String getInfo() {
    return "Species: " + species + " Level: " + level + " Steps: " + steps + " Cycles: " + cycles + " Ability: "
      + ability + " Form:" + form + " Moves:" + moves;
  }
}
