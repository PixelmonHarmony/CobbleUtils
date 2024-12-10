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
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution;
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
import com.kingpixel.cobbleutils.events.ScaleEvent;
import com.kingpixel.cobbleutils.features.breeding.events.HatchEggEvent;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
  private int HP;
  private int Attack;
  private int Defense;
  private int SpecialAttack;
  private int SpecialDefense;
  private int Speed;

  public void EggToPokemon(ServerPlayerEntity player, Pokemon pokemon) {
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
    pokemon.setFaintedTimer(Cobblemon.INSTANCE.getConfig().getDefaultFaintTimer());
    pokemon.setHealTimer(Cobblemon.INSTANCE.getConfig().getHealTimer());

    if (moves != null && !moves.isEmpty()) {
      try {
        // Parsear el JSON string como un JsonObject
        JsonObject jsonObject = JsonParser.parseString(moves).getAsJsonObject();

        // Obtener el JsonArray bajo la clave "moves"
        JsonArray jsonArray = jsonObject.getAsJsonArray("moves");
        jsonArray.forEach(element -> {
          Move move = Moves.INSTANCE.getByName(element.getAsString()).create();
          JsonObject moveJson = move.saveToJSON(new JsonObject());
          BenchedMove benchedMove = BenchedMove.Companion.loadFromJSON(moveJson);
          pokemon.getBenchedMoves().add(benchedMove);
        });
      } catch (Exception e) {
        CobbleUtils.LOGGER.error("Error to process JSON ARRAY: " + e.getMessage());
      }
    }


    removePersistent(pokemon);
    HatchEggEvent.HATCH_EGG_EVENT.emit(player, pokemon);
  }

  private void removePersistent(Pokemon pokemon) {
    pokemon.getPersistentData().remove("species");
    pokemon.getPersistentData().remove("level");
    pokemon.getPersistentData().remove("steps");
    pokemon.getPersistentData().remove("nature");
    pokemon.getPersistentData().remove("ability");
    pokemon.getPersistentData().remove("cycles");
    pokemon.getPersistentData().remove("form");
    pokemon.getPersistentData().remove("random");
    pokemon.getPersistentData().remove("moves");
    applyPersistentIvs(pokemon, "HP", Stats.HP);
    applyPersistentIvs(pokemon, "Attack", Stats.ATTACK);
    applyPersistentIvs(pokemon, "Defense", Stats.DEFENCE);
    applyPersistentIvs(pokemon, "SpecialAttack", Stats.SPECIAL_ATTACK);
    applyPersistentIvs(pokemon, "SpecialDefense", Stats.SPECIAL_DEFENCE);
    applyPersistentIvs(pokemon, "Speed", Stats.SPEED);
  }

  private static void applyPersistentIvs(Pokemon pokemon, String tag, Stats stats) {
    if (pokemon.getPersistentData().contains(tag)) {
      pokemon.setIV(stats, pokemon.getPersistentData().getInt(tag));
      pokemon.getPersistentData().remove(tag);
    }
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
    if (pokemon.getPersistentData().contains("HP")) {
      eggData.setHP(pokemon.getPersistentData().getInt("HP"));
    }
    if (pokemon.getPersistentData().contains("Attack")) {
      eggData.setAttack(pokemon.getPersistentData().getInt("Attack"));
    }
    if (pokemon.getPersistentData().contains("Defense")) {
      eggData.setDefense(pokemon.getPersistentData().getInt("Defense"));
    }
    if (pokemon.getPersistentData().contains("SpecialAttack")) {
      eggData.setSpecialAttack(pokemon.getPersistentData().getInt("SpecialAttack"));
    }
    if (pokemon.getPersistentData().contains("SpecialDefense")) {
      eggData.setSpecialDefense(pokemon.getPersistentData().getInt("SpecialDefense"));
    }
    if (pokemon.getPersistentData().contains("Speed")) {
      eggData.setSpeed(pokemon.getPersistentData().getInt("Speed"));
    }
    return eggData;
  }

  public void steps(ServerPlayerEntity player, Pokemon pokemon, int stepsremove) {
    if (!pokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) return;
    if (stepsremove == 0) return;
    this.steps -= stepsremove;

    if (steps <= 0) {
      this.cycles--;
      this.steps = getMaxStepsPerCycle();
    }
    updateSteps(pokemon);
    if (this.cycles < 0) {
      EggToPokemon(player, pokemon);
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

    if (plotBreeding.getEggs().size() >= CobbleUtils.breedconfig.getMaxeggperplot())
      return null;

    return createEgg(male, female, player);
  }

  public static Pokemon createEgg(
    Pokemon male, Pokemon female, ServerPlayerEntity player) throws NoPokemonStoreException {

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
          usePokemonToEgg = CobbleUtils.breedconfig.getPokemonsForDoubleDitto().generateRandomPokemon(CobbleUtils.MOD_ID, "breeding");
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

    if (!egg.showdownId().equalsIgnoreCase("egg")) {
      player.sendMessage(
        AdventureTranslator.toNative(
          "%prefix% The CobbleUtils datapack is not installed, please notify the Owner/Admin about this.",
          CobbleUtils.breedconfig.getPrefix()
        )
      );
      return PokemonProperties.Companion.parse("rattata").create();
    }

    if (random) egg.getPersistentData().putBoolean("random", true);

    // Aplicar la lógica de mecánicas y tamaño
    mechanicsLogic(male, female, usePokemonToEgg, egg);

    ScaleEvent.solveScale(egg);


    // Enviar mensaje al jugador
    PlayerUtils.sendMessage(player, PokemonUtils.replace(CobbleUtils.breedconfig.getCreateEgg()
        .replace("%egg%", egg.getPersistentData().getString("species")),
      List.of(male, female, egg)), CobbleUtils.breedconfig.getPrefix());
    return egg;
  }


  public static boolean isCompatible(Pokemon male, Pokemon female) {
    if (male.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED)) return false;
    if (female.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED)) return false;
    if (isDitto(male) || isDitto(female)) return true;
    return female.getForm().getEggGroups().stream()
      .anyMatch(eggGroup -> male.getForm().getEggGroups().contains(eggGroup));
  }

  public static boolean isDitto(Pokemon pokemon) {
    if (pokemon == null) return false;
    return pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto");
  }

  private static void mechanicsLogic(Pokemon male, Pokemon female, Pokemon usePokemonToEgg, Pokemon egg) {
    // Especie para el huevo
    Pokemon eggSpecie = PokemonUtils.getEvolutionPokemonEgg(usePokemonToEgg.getSpecies());

    // Form
    String form = getForm(female);

    Pokemon firstEvolution = PokemonProperties.Companion.parse(eggSpecie.getSpecies().showdownId() + " " + form).create();

    // IVS
    applyInitialIvs(egg, male, female);

    // Nature (Done)
    applyNature(male, female, egg);

    // Ability (Done)
    applyAbility(male, female, firstEvolution, egg);

    // Shiny Rate (Done)
    applyShinyRate(male, female, egg);

    // PokeBall (Done)
    applyPokeBall(male, female, egg);

    // Egg Moves (Done)
    applyEggMoves(male, female, usePokemonToEgg, egg);
  }

  private static void applyEggMoves(Pokemon male, Pokemon female, Pokemon usePokemonToEgg, Pokemon egg) {
    if (Utils.RANDOM.nextDouble(100) >= CobbleUtils.breedconfig.getSuccessItems().getPercentageEggMoves()) return; //
    // default 0%
    List<String> moves = new ArrayList<>();
    male.getAllAccessibleMoves().forEach(move -> moves.add(move.getName()));
    female.getAllAccessibleMoves().forEach(move -> moves.add(move.getName()));

    List<String> names = new ArrayList<>();
    usePokemonToEgg.getForm().getMoves().getEggMoves().forEach(eggmove -> {
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

  private static void applyPokeBall(Pokemon male, Pokemon female, Pokemon egg) {
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
  }

  private static void applyShinyRate(Pokemon male, Pokemon female, Pokemon egg) {
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

    shinyrate = (int) Math.min(0, shinyrate);
    if (shinyrate <= 1) {
      egg.setShiny(true);
    } else {
      egg.setShiny(Utils.RANDOM.nextInt((int) shinyrate) == 0);
    }
  }

  private static void applyNature(Pokemon male, Pokemon female, Pokemon egg) {
    boolean hasDoubleEverstone = male.heldItem().getItem() == EVERSTONE && female.heldItem().getItem() == EVERSTONE;
    boolean hasEverstone = male.heldItem().getItem() == EVERSTONE || female.heldItem().getItem() == EVERSTONE;
    boolean isSuccess = Utils.RANDOM.nextDouble() * 100 <= CobbleUtils.breedconfig.getSuccessItems().getPercentageEverStone();

    if (isSuccess && hasEverstone) {
      if (hasDoubleEverstone) {
        egg.setNature(Utils.RANDOM.nextBoolean() ? male.getNature() : female.getNature());
      } else {
        egg.setNature(male.heldItem().getItem() == EVERSTONE ? male.getNature() : female.getNature());
      }
    } else {
      egg.setNature(Natures.INSTANCE.getRandomNature());
    }
  }


  private static final List<Stats> stats =
    new ArrayList<>(Arrays.stream(Stats.values()).filter(stats1 -> stats1 != Stats.EVASION && stats1 != Stats.ACCURACY).toList());

  /**
   * Apply the initial IVs to the egg
   *
   * @param egg    The egg
   * @param male   The male pokemon
   * @param female The female pokemon
   */
  private static void applyInitialIvs(Pokemon egg, Pokemon male, Pokemon female) {
    List<Pokemon> pokemons = List.of(male, female);
    List<Pokemon> bracelets = new ArrayList<>();
    List<Stats> cloneStats = new ArrayList<>(stats);

    AtomicInteger numIvsToTransfer = new AtomicInteger(CobbleUtils.breedconfig.getDefaultNumIvsToTransfer());

    pokemons.forEach(pokemon -> {
      if (pokemon.heldItem().getItem() instanceof CobblemonItem item) {
        if (isPowerItem(item)) {
          bracelets.add(pokemon);
        } else if (item.equals(DESTINY_KNOT)) {
          numIvsToTransfer.set(CobbleUtils.breedconfig.getNumberIvsDestinyKnot());
        }
      }
    });


    bracelets.forEach(pBracelet -> {
      applyIvsPower(pBracelet, egg, (CobblemonItem) pBracelet.heldItem().getItem(), cloneStats);
      numIvsToTransfer.decrementAndGet();
    });
    applyIvs(male, female, egg, numIvsToTransfer.get(), cloneStats);

    cloneStats.forEach(rStats -> {
      int random = Utils.RANDOM.nextInt(CobbleUtils.breedconfig.getMaxIvsRandom() + 1);
      if (CobbleUtils.breedconfig.isShowIvs()) {
        egg.setIV(rStats, random);
      } else {
        egg.setIV(rStats, 0);
        egg.getPersistentData().putInt(getName(rStats), random);
      }
    });
  }

  private static void applyIvs(Pokemon male, Pokemon female, Pokemon egg, int amount, List<Stats> stats) {
    if (Utils.RANDOM.nextDouble(100) >= CobbleUtils.breedconfig.getSuccessItems().getPercentageDestinyKnot() && haveDestinyKnot(female, male))
      return;

    for (int i = 0; i < amount; i++) {
      Stats stat = stats.remove(Utils.RANDOM.nextInt(stats.size()));
      Pokemon selectedParent = Utils.RANDOM.nextBoolean() ? male : female;
      if (CobbleUtils.breedconfig.isShowIvs()) {
        egg.setIV(stat, selectedParent.getIvs().getOrDefault(stat));
      } else {
        egg.setIV(stat, 0);
        egg.getPersistentData().putInt(getName(stat), selectedParent.getIvs().getOrDefault(stat));
      }
    }
  }

  private static String getName(Stats stats) {
    return switch (stats) {
      case HP -> "HP";
      case ATTACK -> "Attack";
      case DEFENCE -> "Defense";
      case SPECIAL_ATTACK -> "SpecialAttack";
      case SPECIAL_DEFENCE -> "SpecialDefense";
      case SPEED -> "Speed";
      default -> "";
    };
  }

  private static boolean haveDestinyKnot(Pokemon female, Pokemon male) {
    return female.heldItem().getItem().equals(DESTINY_KNOT) || male.heldItem().getItem().equals(DESTINY_KNOT);
  }


  private static boolean isPowerItem(CobblemonItem item) {
    if (item == null) return false;
    return item.equals(POWER_WEIGHT) || item.equals(POWER_BRACER) || item.equals(POWER_BELT) || item.equals(POWER_ANKLET) || item.equals(POWER_LENS) || item.equals(POWER_BAND);
  }

  private static void applyIvsPower(Pokemon select, Pokemon egg, CobblemonItem bracelet, List<Stats> stats) {
    if (bracelet == null) return;
    if (Utils.RANDOM.nextDouble(100) >= CobbleUtils.breedconfig.getSuccessItems().getPercentagePowerItem()) return;
    Stats stat = null;
    if (bracelet.equals(POWER_WEIGHT)) {
      stat = Stats.HP;
      if (CobbleUtils.breedconfig.isShowIvs()) {
        egg.setIV(stat, select.getIvs().getOrDefault(stat));
      } else {
        egg.setIV(stat, 0);
        egg.getPersistentData().putInt("HP", select.getIvs().getOrDefault(stat));
      }
    } else if (bracelet.equals(POWER_BRACER)) {
      stat = Stats.ATTACK;
      if (CobbleUtils.breedconfig.isShowIvs()) {
        egg.setIV(stat, select.getIvs().getOrDefault(stat));
      } else {
        egg.setIV(stat, 0);
        egg.getPersistentData().putInt("Attack", select.getIvs().getOrDefault(stat));
      }
    } else if (bracelet.equals(POWER_BELT)) {
      stat = Stats.DEFENCE;
      if (CobbleUtils.breedconfig.isShowIvs()) {
        egg.setIV(stat, select.getIvs().getOrDefault(stat));
      } else {
        egg.setIV(stat, 0);
        egg.getPersistentData().putInt("Defense", select.getIvs().getOrDefault(stat));
      }
    } else if (bracelet.equals(POWER_ANKLET)) {
      stat = Stats.SPEED;
      if (CobbleUtils.breedconfig.isShowIvs()) {
        egg.setIV(stat, select.getIvs().getOrDefault(stat));
      } else {
        egg.setIV(stat, 0);
        egg.getPersistentData().putInt("Speed", select.getIvs().getOrDefault(stat));
      }
    } else if (bracelet.equals(POWER_LENS)) {
      stat = Stats.SPECIAL_ATTACK;
      if (CobbleUtils.breedconfig.isShowIvs()) {
        egg.setIV(stat, select.getIvs().getOrDefault(stat));
      } else {
        egg.setIV(stat, 0);
        egg.getPersistentData().putInt("SpecialAttack", select.getIvs().getOrDefault(stat));
      }
    } else if (bracelet.equals(POWER_BAND)) {
      stat = Stats.SPECIAL_DEFENCE;
      if (CobbleUtils.breedconfig.isShowIvs()) {
        egg.setIV(stat, select.getIvs().getOrDefault(stat));
      } else {
        egg.setIV(stat, 0);
        egg.getPersistentData().putInt("SpecialDefense", select.getIvs().getOrDefault(stat));
      }
    }
    stats.remove(stat);
  }

  private static void applyAbility(Pokemon male, Pokemon female, Pokemon firstEvolution, Pokemon egg) {
    if (isDitto(male) && isDitto(female)) {
      Ability randomAbility = PokemonUtils.getRandomAbility(firstEvolution);
      egg.getPersistentData().putString("ability", randomAbility.getName());
      return;
    }
    List<Pokemon> pokemons = new ArrayList<>();
    if (!isDitto(male)) pokemons.add(male);
    if (!isDitto(female)) pokemons.add(female);

    boolean applyAh = false;

    if (!pokemons.isEmpty()) {
      boolean isAH = pokemons.stream().anyMatch(PokemonUtils::isAH);
      boolean success = Utils.RANDOM.nextDouble(100) <= CobbleUtils.breedconfig.getSuccessItems().getPercentageTransmitAH();
      boolean evolutionOrSameSpecie = isEvolutionOrSameSpecie(male, female);
      if (isAH && evolutionOrSameSpecie && success) applyAh = true;
    }
    if (applyAh) {
      egg.getPersistentData().putString("ability", PokemonUtils.getAH(firstEvolution).getName());
    } else {
      egg.getPersistentData().putString("ability", PokemonUtils.getRandomAbility(firstEvolution).getName());
    }
  }

  private static boolean isEvolutionOrSameSpecie(@NotNull Pokemon male, @NotNull Pokemon female) {
    if (isDitto(male) || isDitto(female)) return true;
    if (male.getForm().showdownId().equalsIgnoreCase(female.getForm().showdownId())) return true;

    Set<String> evolutionsMale = new HashSet<>();
    evolutionsMale.add(male.getForm().showdownId());
    Set<String> evolutionsFemale = new HashSet<>();
    evolutionsFemale.add(female.getForm().showdownId());

    PreEvolution malePreEvolution = male.getForm().getPreEvolution();
    while (malePreEvolution != null) {
      evolutionsMale.add(malePreEvolution.getForm().showdownId());
      malePreEvolution = malePreEvolution.getForm().getPreEvolution();
    }

    PreEvolution femalePreEvolution = female.getForm().getPreEvolution();
    while (femalePreEvolution != null) {
      evolutionsFemale.add(femalePreEvolution.getForm().showdownId());
      femalePreEvolution = femalePreEvolution.getForm().getPreEvolution();
    }

    return evolutionsMale.contains(female.getForm().showdownId()) || evolutionsFemale.contains(male.getForm().showdownId());
  }


  private static Pokemon pokemonToEgg(Pokemon usePokemon, boolean dittos, Pokemon female) {
    String specie = getExcepcionalSpecie(usePokemon);
    return EggData.applyPersistent(usePokemon, specie, dittos, female);
  }


  private static String getExcepcionalSpecie(Pokemon pokemon) {
    if (CobbleUtils.breedconfig.getIncenses().isEmpty()) return null;
    AtomicReference<String> s = new AtomicReference<>();
    CobbleUtils.breedconfig.getIncenses().forEach(incense -> {
      if (s.get() == null) {
        s.set(incense.getChild(pokemon));
      }
    });
    return s.get();
  }

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
    private List<PokemonChance> pokemons;

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

      if (CobbleUtils.breedconfig.getBlacklistForm().contains(configForm.get())) configForm.set("");
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

    if (CobbleUtils.breedconfig.getBlacklistForm().contains(form)) form = "";

    return form;
  }

  private static Pokemon applyPersistent(Pokemon pokemon, String lure_species, boolean dittos,
                                         Pokemon female) {
    Pokemon firstEvolution;

    if (lure_species == null) {
      firstEvolution = PokemonUtils.getEvolutionPokemonEgg(pokemon.getSpecies());
    } else {
      firstEvolution = PokemonProperties.Companion.parse(lure_species).create();
    }

    Pokemon egg = PokemonProperties.Companion.parse("egg type_egg=" + firstEvolution.showdownId()).create();

    // TODO: Remove the message when the pokemon is healed
    egg.setFaintedTimer(999999999);
    egg.setHealTimer(999999999);


    egg.getPersistentData().putString("species", firstEvolution.getSpecies().showdownId());
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
          CobbleUtils.breedconfig.getNameEgg()
            .replace("%pokemon%", firstEvolution.getSpecies().showdownId()),
          pokemon)));
    }
    return egg;
  }

  public String getInfo() {
    return "Species: " + species + " Level: " + level + " Steps: " + steps + " Cycles: " + cycles + " Ability: "
      + ability + " Form:" + form + " Moves:" + moves;
  }
}
