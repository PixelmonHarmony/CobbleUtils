package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.Cobblemon;
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
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.CobbleUtilsTags;
import com.kingpixel.cobbleutils.Model.PokemonChance;
import com.kingpixel.cobbleutils.Model.PokemonData;
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
import net.minecraft.text.Text;

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

  public void EggToPokemon(Pokemon pokemon) {
    if (!pokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) return;

    PokemonProperties pokemonProperties = PokemonProperties.Companion.parse(species + " " + form);

    pokemonProperties.setForm(form);

    pokemon.createPokemonProperties(List.of(
      PokemonPropertyExtractor.GENDER,
      PokemonPropertyExtractor.FRIENDSHIP
    )).apply(pokemon);

    pokemonProperties.apply(pokemon);

    AbilityTemplate abilityTemplate;
    if (ability.isEmpty()) {
      abilityTemplate = PokemonUtils.getRandomAbility(pokemon).getTemplate();
    } else {
      abilityTemplate = Abilities.INSTANCE.get(this.ability);
    }

    if (abilityTemplate != null) {
      pokemon.updateAbility(abilityTemplate.create(false));
    }

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
    if (!pokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) return;
    //checkingdata(pokemon);
    if (stepsremove == 0) return;
    this.steps -= stepsremove;

    if (steps <= 0) {
      this.cycles--;
      this.steps = getMaxStepsPerCycle();
    }
    updateSteps(pokemon);
    if (this.cycles < 0) {
      EggToPokemon(pokemon);
    }
  }

  /*private void checkingdata(Pokemon pokemon) {
    Pokemon isempty;
    if (pokemon.getPersistentData().getString("species").isEmpty()) {
      isempty = PokemonUtils.getFirstEvolution(ArraysPokemons.getRandomPokemon());
      pokemon.getPersistentData().putString("species", isempty.getSpecies().showdownId());

      if (pokemon.getPersistentData().getInt("level") == 0) {
        pokemon.getPersistentData().putInt("level", 1);
      }
      if (pokemon.getPersistentData().getString("ability").isEmpty()) {
        pokemon.getPersistentData().putString("ability", PokemonUtils.getRandomAbility(isempty).getName());
      }
      if (pokemon.getPersistentData().getString("size").isEmpty()) {
        pokemon.getPersistentData().putString("size", ScalePokemonData.getScalePokemonData(isempty).getRandomPokemonSize().getId());
      }
    }
  }*/

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

    if (plotBreeding.getEggs().size() >= CobbleUtils.breedconfig.getMaxeggperplot())
      return null;

    return createEgg(male, female, player);
  }

  public static Pokemon createEgg(
    Pokemon male, Pokemon female, ServerPlayerEntity player) throws NoPokemonStoreException {

    // Validación inicial
    if (male == null || female == null)
      return null;
    if (male.isLegendary() || male.isUltraBeast())
      return null;
    if (female.isLegendary() || female.isUltraBeast())
      return null;

    // Intercambiar posiciones si Ditto está en la posición female
    if (female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      Pokemon temp = male;
      male = female;
      female = temp;
    }

    Pokemon usePokemonToEgg;
    Pokemon egg;
    boolean random = false;
    // Caso cuando Ditto está en la posición male
    if (male.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (female.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
        // Ambos Pokémon son Ditto
        if (!CobbleUtils.breedconfig.isDoubleditto())
          return null;
        do {
          usePokemonToEgg = ArraysPokemons.getRandomPokemon();
        } while (usePokemonToEgg.isUltraBeast() || usePokemonToEgg.isLegendary());
        random = true;
      } else {
        // Solo uno es Ditto
        if (!CobbleUtils.breedconfig.isDitto())
          return null;
        usePokemonToEgg = female;
      }
    } else if (male.getSpecies().showdownId().equalsIgnoreCase(female.getSpecies().showdownId())) {
      // Ambos Pokémon son de la misma especie (y ninguno es Ditto)
      usePokemonToEgg = female;
    } else {
      // Diferentes especies, verificar compatibilidad
      if (isCompatible(male, female)) {
        usePokemonToEgg = female;
      } else {
        return null;
      }
    }

    egg = EggData.pokemonToEgg(usePokemonToEgg, false, female);

    if (random) egg.getPersistentData().putBoolean("random", true);

    // Aplicar la lógica de mecánicas y tamaño
    mechanicsLogic(male, female, usePokemonToEgg, egg);

    ScalePokemonData.getScalePokemonData(usePokemonToEgg).getRandomPokemonSize().apply(egg);

    // Enviar mensaje al jugador
    player.sendMessage(
      AdventureBreeding.adventure(
        PokemonUtils.replace(CobbleUtils.breedconfig.getCreateEgg()
            .replace("%egg%", egg.getPersistentData().getString("species")),
          List.of(male, female, egg))));

    return egg;
  }


  public static boolean isCompatible(Pokemon male, Pokemon female) {
    if (male.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED)) return false;
    if (female.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED)) return false;
    if (isDitto(male) || isDitto(female)) return true;
    return female.getForm().getEggGroups().stream()
      .anyMatch(eggGroup -> male.getForm().getEggGroups().contains(eggGroup));
  }

  private static boolean isDitto(Pokemon pokemon) {
    return pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto");
  }

  private static void infoPokemon(Pokemon pokemon) {
    CobbleUtils.LOGGER.info("Species: " + pokemon.getSpecies().showdownId());
    CobbleUtils.LOGGER.info("Nature: " + pokemon.getNature().getName());
    CobbleUtils.LOGGER.info("Ability: " + pokemon.getAbility().getName());
    CobbleUtils.LOGGER.info("Form: " + pokemon.getForm().getName());
    CobbleUtils.LOGGER.info("Forms: ");
    pokemon.getSpecies().getForms().forEach(formData -> {
      CobbleUtils.LOGGER.info("Form -> " + formData.getName());
    });
    CobbleUtils.LOGGER.info("Features: ");
    pokemon.getFeatures().forEach((feature) -> CobbleUtils.LOGGER.info("Feature -> " + feature.getName()));
    CobbleUtils.LOGGER.info("Aspects: ");
    pokemon.getForm().getAspects().forEach((aspect) -> CobbleUtils.LOGGER.info("Aspect -> " + aspect));
    CobbleUtils.LOGGER.info("Labels: ");
    pokemon.getForm().getLabels().forEach((label) -> CobbleUtils.LOGGER.info("Label -> " + label));
    CobbleUtils.LOGGER.info("IVS: ");
    pokemon.getIvs().forEach((Ivs) -> CobbleUtils.LOGGER.info(Ivs.getKey().getShowdownId() + " -> " + Ivs.getValue()));
  }

  private static void mechanicsLogic(Pokemon male, Pokemon female, Pokemon usePokemonToEgg, Pokemon egg) {
    // Especie para el huevo
    Pokemon eggSpecie = PokemonUtils.getEvolutionPokemonEgg(usePokemonToEgg.getSpecies());

    // Form
    String form = getForm(female);


    Pokemon firstEvolution =
      PokemonProperties.Companion.parse(eggSpecie.getSpecies().showdownId() + " " + form).create();

    // IVS
    egg.createPokemonProperties(List.of(PokemonPropertyExtractor.IVS, PokemonPropertyExtractor.GENDER)).apply(egg);

    applyInitialIvs(egg, male, female);

    boolean isDoubleEverStone = (male.heldItem().getItem() == EVERSTONE
      && female.heldItem().getItem() == EVERSTONE);

    // Nature (Done)
    if (isDoubleEverStone) {
      List<Pokemon> parents = List.of(male, female);
      egg.setNature(parents.get(Utils.RANDOM.nextInt(parents.size())).getNature());
    } else if (male.heldItem().getItem() == EVERSTONE) {
      egg.setNature(male.getNature());
    } else if (female.heldItem().getItem() == EVERSTONE) {
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

    // PokeBall
    if (CobbleUtils.breedconfig.isObtainPokeBallFromMother()) {
      if (female.getSpecies().showdownId().equalsIgnoreCase(male.getSpecies().showdownId())) {
        if (Utils.RANDOM.nextBoolean()) {
          egg.setCaughtBall(male.getCaughtBall());
        } else {
          egg.setCaughtBall(female.getCaughtBall());
        }
      } else {
        egg.setCaughtBall(female.getCaughtBall());
      }
    }

    // Size
    ScalePokemonData.getScalePokemonData(usePokemonToEgg).getRandomPokemonSize().apply(egg);

    // Gender
    egg.createPokemonProperties(PokemonPropertyExtractor.GENDER).apply(egg);

    // Egg Moves
    List<String> moves = new ArrayList<>();
    male.getMoveSet().getMoves().forEach(move -> moves.add(move.getName()));
    female.getMoveSet().getMoves().forEach(move -> moves.add(move.getName()));

    male.getBenchedMoves().forEach(move -> moves.add(move.getMoveTemplate().getName()));
    female.getBenchedMoves().forEach(move -> moves.add(move.getMoveTemplate().getName()));

    List<String> names = new ArrayList<>();
    female.getForm().getMoves().getEggMoves().forEach(eggmove -> {
      if (moves.contains(eggmove.getName())) {
        names.add(eggmove.getName());
      }
    });
    

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
    boolean sameItem = false;

    if (male != null && male.heldItem() != null && male.heldItem().getItem() instanceof CobblemonItem cobblemonItem) {
      maleItem = cobblemonItem;
    }
    if (female != null && female.heldItem() != null && female.heldItem().getItem() instanceof CobblemonItem cobblemonItem) {
      femaleItem = cobblemonItem;
    }

    if (maleItem != null && femaleItem != null && maleItem.equals(femaleItem)) {
      sameItem = true;
    }

    if (maleItem == null && femaleItem == null) {
      logicIvs(male, female, female, null, egg);
    } else if (sameItem && maleItem == DESTINY_KNOT) {
      applyDestinyKnot(male, female, egg);
    } else {
      if (maleItem == DESTINY_KNOT) {
        logicIvs(male, female, female, femaleItem, egg);
        logicIvs(male, female, male, maleItem, egg);
      } else if (femaleItem == DESTINY_KNOT) {
        logicIvs(male, female, male, maleItem, egg);
        logicIvs(male, female, female, femaleItem, egg);
      } else {
        if (isPowerItem(maleItem) && isPowerItem(femaleItem)) {
          logicIvs(male, female, male, maleItem, egg);
          logicIvs(male, female, female, femaleItem, egg);
        } else if (isPowerItem(maleItem)) {
          logicIvs(male, female, female, femaleItem, egg);
          logicIvs(male, female, male, maleItem, egg);
        } else if (isPowerItem(femaleItem)) {
          logicIvs(male, female, male, maleItem, egg);
          logicIvs(male, female, female, femaleItem, egg);
        } else {
          logicIvs(male, female, null, null, egg);
        }

      }
    }

  }


  private static void logicIvs(Pokemon male, Pokemon female, Pokemon select, CobblemonItem itemPokemon, Pokemon egg) {
    if (itemPokemon == null) {
      // 2 Random IVs
      List<Stats> stats = new ArrayList<>(Arrays.stream(Stats.values()).toList());
      stats.remove(Stats.EVASION);
      stats.remove(Stats.ACCURACY);

      for (int i = 0; i < 2; i++) {
        Stats stat = stats.remove(Utils.RANDOM.nextInt(stats.size()));
        egg.setIV(stat, Utils.RANDOM.nextInt(32));
      }
    } else if (itemPokemon == DESTINY_KNOT) {
      applyDestinyKnot(male, female, egg);
    } else if (isPowerItem(itemPokemon)) {
      applyIvsPower(select, egg, itemPokemon);
    }
  }

  private static boolean isPowerItem(CobblemonItem item) {
    if (item == null) return false;
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
      egg.setIV(Stats.HP, select.getIvs().getOrDefault(Stats.HP));
    } else if (itemSame.equals(POWER_BRACER)) {
      egg.setIV(Stats.ATTACK, select.getIvs().getOrDefault(Stats.ATTACK));
    } else if (itemSame.equals(POWER_BELT)) {
      egg.setIV(Stats.DEFENCE, select.getIvs().getOrDefault(Stats.DEFENCE));
    } else if (itemSame.equals(POWER_ANKLET)) {
      egg.setIV(Stats.SPEED, select.getIvs().getOrDefault(Stats.SPEED));
    } else if (itemSame.equals(POWER_LENS)) {
      egg.setIV(Stats.SPECIAL_ATTACK, select.getIvs().getOrDefault(Stats.SPECIAL_ATTACK));
    } else if (itemSame.equals(POWER_BAND)) {
      egg.setIV(Stats.SPECIAL_DEFENCE, select.getIvs().getOrDefault(Stats.SPECIAL_DEFENCE));
    }
  }

  private enum Family {
    MALE, FEMALE
  }

  private static void applyDestinyKnot(Pokemon male, Pokemon female, Pokemon egg) {
    List<Stats> stats = new ArrayList<>(Arrays.stream(Stats.values())
      .filter(stat -> stat != Stats.EVASION && stat != Stats.ACCURACY)
      .toList());

    for (int i = 0; i < 5; i++) {
      Stats stat = stats.remove(Utils.RANDOM.nextInt(stats.size()));
      Pokemon selectedParent = Utils.RANDOM.nextBoolean() ? male : female;
      egg.setIV(stat, selectedParent.getIvs().getOrDefault(stat));
    }
  }


  private static void applyAbility(Pokemon male, Pokemon female, Pokemon firstEvolution, Pokemon egg) {
    boolean maleHiddenAbility = PokemonUtils.haveAH(male);
    boolean femaleHiddenAbility = PokemonUtils.haveAH(female);
    egg.getPersistentData().remove("ability");
    if (maleHiddenAbility || femaleHiddenAbility) {
      if (Utils.RANDOM.nextInt(100) < 70) {
        Ability ability1 = PokemonUtils.getAH(firstEvolution);
        egg.getPersistentData().putString("ability", ability1.getName());
      } else {
        Ability randomAbility = PokemonUtils.getRandomAbility(firstEvolution);
        egg.getPersistentData().putString("ability", randomAbility.getName());
      }
    } else {
      Ability randomAbility = PokemonUtils.getRandomAbility(firstEvolution);
      egg.getPersistentData().putString("ability", randomAbility.getName());
    }
  }

  private static Pokemon pokemonToEgg(Pokemon usePokemon, boolean dittos, Pokemon female) {
    String specie = getExcepcionalSpecie(usePokemon);
    Pokemon egg = PokemonProperties.Companion.parse("egg type_egg=" + specie).create();
    EggData.applyPersistent(egg, usePokemon, specie, dittos, female);
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

 /* private static void applyForm(Pokemon pokemon) {
    PokemonProperties pokemonProperties = new PokemonProperties();
    Species species = pokemon.getSpecies();
    FormData form = pokemon.getForm();
    List<FormData> forms = pokemon.getSpecies().getForms();

    pokemonProperties.setSpecies(species.showdownId());

    Optional<FormData> matchingForm = forms.stream()
      .filter(f -> f.formOnlyShowdownId().equals(form.formOnlyShowdownId()))
      .findFirst();

    if (matchingForm.isPresent()) {
      FormData foundForm = matchingForm.get();

      for (String aspect : foundForm.getAspects()) {
        // alternative form
        pokemonProperties.getCustomProperties().add(new FlagSpeciesFeature(aspect, true));

        // regional bias
        String regionBias = aspect.split("-")[aspect.split("-").length - 1];
        pokemonProperties.getCustomProperties().add(
          new StringSpeciesFeature("region_bias", regionBias)
        );
      }
    }
  }*/

  @Getter
  public static class EggForm {
    private String form;
    private List<String> pokemons;

    public EggForm(String form, List<String> pokemons) {
      this.form = form;
      this.pokemons = pokemons;
    }
  }

  @Getter
  public static class EggSpecialForm {
    private String form;
    private List<PokemonData> pokemons;

    public EggSpecialForm(String form, List<PokemonData> pokemons) {
      this.form = form;
      this.pokemons = pokemons;
    }
  }


  @Getter
  @Setter
  public static class PokemonRareMecanic {
    private List<PokemonChance> pokemons = new ArrayList<>();

    public PokemonRareMecanic() {

    }

    public PokemonRareMecanic(List<PokemonChance> pokemons) {
      this.pokemons = pokemons;
    }

  }


  private static String getForm(Pokemon pokemon) {
    String form;

    switch (pokemon.getSpecies().showdownId()) {
      case "perrserker":
      case "sirfetchd":
      case "mrrime":
      case "cursola":
      case "obstagoon":
      case "runerigus":
        return "galarian";
      case "clodsire":
        return "paldean";
      case "overqwil":
      case "sneasler":
        return "hisuian";
    }


    AtomicReference<String> configForm = new AtomicReference<>();

    CobbleUtils.breedconfig.getEggForms().stream()
      .filter(eggForm -> eggForm.getPokemons().contains(pokemon.getSpecies().showdownId()))
      .findFirst()
      .ifPresent(eggForm -> configForm.set(eggForm.getForm()));

    CobbleUtils.breedconfig.getEggSpecialForms().stream()
      .filter(eggSpecialForm -> eggSpecialForm.getPokemons().stream()
        .anyMatch(pokemonData -> pokemonData.getPokename().equalsIgnoreCase(pokemon.getSpecies().showdownId())))
      .findFirst()
      .ifPresent(eggSpecialForm -> configForm.set(eggSpecialForm.getForm()));

    if (configForm.get() != null) {
      return configForm.get();
    }


    if (pokemon.getForm().getAspects().isEmpty()) {
      form = "";
    } else {
      form = pokemon.getForm().getAspects().get(0);
    }

    form = form.replace("-", "_");

    int lastUnderscoreIndex = form.lastIndexOf("_");

    if (lastUnderscoreIndex != -1) {
      form = form.substring(0, lastUnderscoreIndex) + "=" + form.substring(lastUnderscoreIndex + 1);
    }
    return form;
  }

  private static void applyPersistent(Pokemon egg, Pokemon pokemon, String lure_species, boolean dittos,
                                      Pokemon female) {
    Pokemon firstEvolution;

    if (lure_species == null) {
      firstEvolution = PokemonUtils.getEvolutionPokemonEgg(pokemon.getSpecies());
    } else {
      firstEvolution = PokemonProperties.Companion.parse(lure_species).create();
    }

    egg.getPersistentData().putString("species", firstEvolution.showdownId());
    egg.getPersistentData().putString("nature", pokemon.getNature().getName().getPath());
    egg.getPersistentData().putString("ability", pokemon.getAbility().getTemplate().getName().toLowerCase().trim());
    egg.getPersistentData().putString("form", getForm(female));
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

  public static String getInformation(Pokemon pokemon) {
    return pokemon.getPersistentData().asString();
  }

  public String getInfo() {
    return "Species: " + species + " Level: " + level + " Steps: " + steps + " Cycles: " + cycles + " Ability: "
      + ability + " Form:" + form + " Moves:" + moves;
  }
}
