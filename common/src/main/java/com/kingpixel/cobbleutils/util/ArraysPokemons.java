package com.kingpixel.cobbleutils.util;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.PokemonData;
import com.kingpixel.cobbleutils.util.events.ArraysPokemonEvent;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
@Data
public class ArraysPokemons {
  private static List<Species> pokemons = new ArrayList<>();
  private static List<Species> legendarys = new ArrayList<>();
  private static List<Species> ultraBeasts = new ArrayList<>();
  private static Map<ElementalType, List<Pokemon>> pokemonsByType = new HashMap<>();
  private static List<Species> all = new ArrayList<>();

  public static List<Pokemon> getRandomPokemon(TypePokemon typePokemon, int size) {
    List<Species> speciesList;

    switch (typePokemon) {
      case LEGENDARY:
        speciesList = legendarys;
        break;
      case ULTRABEAST:
        speciesList = ultraBeasts;
        break;
      case NORMAL:
      default:
        speciesList = pokemons;
        break;
    }

    return getRandomList(size, speciesList);
  }

  public static <E> List<Pokemon> getRandomPokemon(List<E> TypePokemons, int size) {
    if (TypePokemons.isEmpty() || size <= 0) {
      throw new IllegalArgumentException("La lista de Pokémon o el tamaño no puede ser vacío.");
    }

    List<Pokemon> randomPokemons = new ArrayList<>();
    Set<String> names = new HashSet<>();

    while (randomPokemons.size() < size) {
      E randomItem = getRandomItem(TypePokemons);
      if (randomItem instanceof Species species) {
        Pokemon pokemon = createPokemon(species);
        if (names.add(pokemon.getSpecies().showdownId())) {
          randomPokemons.add(pokemon);
        }
      }
    }

    return randomPokemons;
  }


  public enum TypePokemon {
    NORMAL,
    LEGENDARY,
    ULTRABEAST,
    PARADOX,
    MYTHICAL,
  }

  public static TypePokemon getTypePokemon(Pokemon p) {
    Species species = p.getSpecies();
    if (p.isLegendary()) return TypePokemon.LEGENDARY;
    if (p.isUltraBeast()) return TypePokemon.ULTRABEAST;
    if (species.getLabels().contains("paradox")) return TypePokemon.PARADOX;
    if (species.getLabels().contains("mythical")) return TypePokemon.MYTHICAL;
    return TypePokemon.NORMAL;
  }

  /**
   * Initialize the Pokémon arrays
   */
  public static void init() {
    Collection<Species> species = PokemonSpecies.INSTANCE.getSpecies();
    Set<String> pokeBlacklist = CobbleUtils.config.getBlacklist().stream()
      .map(PokemonData::getPokename).collect(Collectors.toSet());

    List<Species> filteredSpecies = species.stream()
      .filter(s -> s.getNationalPokedexNumber() != 9999 && !pokeBlacklist.contains(s.showdownId()))
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .toList();

    Map<String, List<Species>> sortedSpecies = filteredSpecies.stream()
      .collect(Collectors.groupingBy(s -> {
        Pokemon p = s.create(1);
        TypePokemon typePokemon = getTypePokemon(p);
        return (typePokemon != TypePokemon.NORMAL && typePokemon != TypePokemon.ULTRABEAST
          || CobbleUtils.config.getLegends().contains(PokemonData.from(p)))
          ? "legendary" : p.isUltraBeast() ? "ultrabeast" : "normal";
      }));

    pokemons = new ArrayList<>(sortedSpecies.getOrDefault("normal", new ArrayList<>()));
    legendarys = new ArrayList<>(sortedSpecies.getOrDefault("legendary", new ArrayList<>()));
    ultraBeasts = new ArrayList<>(sortedSpecies.getOrDefault("ultrabeast", new ArrayList<>()));

    pokemonsByType = new HashMap<>();
    species.forEach(species1 -> species1.getForms().forEach(formData -> {
      for (ElementalType type : formData.getTypes()) {
        pokemonsByType.computeIfAbsent(type, k -> new ArrayList<>())
          .add(formData.getAspects().isEmpty() ? formData.getSpecies().create(1)
            : parsePokemonByAspect(formData, species1));
      }
    }));

    ArraysPokemonEvent.FINISH_GENERATE_POKEMONS.emit(species.stream().toList());
  }

  private static Pokemon parsePokemonByAspect(FormData formData, Species species) {
    for (String aspect : formData.getAspects()) {
      if (List.of("male", "female", "unknown").contains(aspect)) continue;
      String form = aspect.replace("-", "_");
      int lastUnderscoreIndex = form.lastIndexOf("_");
      if (lastUnderscoreIndex != -1) {
        form = form.substring(0, lastUnderscoreIndex) + "=" + form.substring(lastUnderscoreIndex + 1);
      }
      return PokemonProperties.Companion.parse(species.showdownId() + " " + form).create();
    }
    return formData.getSpecies().create(1);
  }

  private static Pokemon createPokemon(Species species) {
    Pokemon pokemon = PokemonProperties.Companion.parse(species.showdownId()).create();
    if (CobbleUtils.config.isDebug()) {
      CobbleUtils.LOGGER.info("Pokemon: " + species.getName());
      species.getForms().forEach(form -> CobbleUtils.LOGGER.info(" -" + form.getName()));
    }
    return pokemon;
  }

  public static Pokemon getRandomPokemon() {
    return createPokemon(getRandomItem(pokemons));
  }

  public static Pokemon getRandomLegendary() {
    return createPokemon(getRandomItem(legendarys));
  }

  public static Pokemon getRandomUltraBeast() {
    return createPokemon(getRandomItem(ultraBeasts));
  }

  public static <T> T getRandomItem(List<T> list) {
    return list.get(ThreadLocalRandom.current().nextInt(list.size()));
  }

  public static List<Pokemon> getRandomListPokemonNormal(int size) {
    return getRandomList(size, pokemons);
  }

  public static List<Pokemon> getRandomList(int size, List<Species> speciesList) {
    Set<String> names = new HashSet<>();
    List<Pokemon> pokemons = new ArrayList<>();
    while (pokemons.size() < size) {
      Pokemon pokemon = createPokemon(getRandomItem(speciesList));
      if (names.add(pokemon.getSpecies().showdownId())) {
        pokemons.add(pokemon);
      }
    }
    return pokemons;
  }

  public static Pokemon getRandomPokemonNormalRarity(String rarity) {
    return pokemons.stream()
      .map(ArraysPokemons::createPokemon)
      .filter(p -> PokemonUtils.getRarityS(p).equalsIgnoreCase(rarity))
      .findAny()
      .orElseThrow(() -> new IllegalStateException("No Pokémon found with rarity: " + rarity));
  }

  public static Pokemon getRandomPokemon(ElementalType type) {
    return getRandomPokemon(pokemonsByType.get(type), new HashSet<>());
  }

  public static Pokemon getRandomPokemon(List<Pokemon> pokemonList, Set<TypePokemon> blacklist) {
    return getPokemon(blacklist, pokemonList, ThreadLocalRandom.current().nextInt(1, 50));
  }

  @NotNull private static Pokemon getPokemon(Set<TypePokemon> blacklist, List<Pokemon> pokemons, int level) {
    Pokemon p;
    do {
      p = getRandomItem(pokemons);
      p.setLevel(level);
    } while (blacklist.contains(getTypePokemon(p)));
    return p;
  }

  /**
   * Get a list of random Pokémon based on ElementalType and excluding certain TypePokemon.
   *
   * @param types     The list of ElementalTypes to filter the Pokémon.
   * @param blacklist The list of TypePokemon to exclude from the results.
   * @param size      The number of Pokémon to return.
   *
   * @return List of Pokémon matching the given ElementalTypes and excluding the TypePokemon from the blacklist.
   */
  public static List<Pokemon> getPokemonsByTypesAndExclusions(List<ElementalType> types, List<TypePokemon> blacklist, int size) {
    List<Pokemon> filteredPokemons = types.stream()
      .flatMap(type -> pokemonsByType.getOrDefault(type, Collections.emptyList()).stream())
      .filter(pokemon -> !blacklist.contains(getTypePokemon(pokemon)))
      .toList();

    if (filteredPokemons.isEmpty()) {
      throw new IllegalStateException("No Pokémon available for the given types and exclusions.");
    }

    Set<String> selectedNames = new HashSet<>();
    List<Pokemon> selectedPokemons = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      Pokemon pokemon = filteredPokemons.get(new Random().nextInt(filteredPokemons.size()));
      if (!selectedNames.contains(pokemon.getSpecies().showdownId())) {
        selectedPokemons.add(pokemon);
        selectedNames.add(pokemon.getSpecies().showdownId());
      } else {
        i--;
      }
    }

    return selectedPokemons;
  }

  /**
   * Get a list of Pokémon based on ElementalType and excluding certain TypePokemon.
   *
   * @param types     The list of ElementalTypes to filter the Pokémon.
   * @param blacklist The list of TypePokemon to exclude from the results.
   *
   * @return List of Pokémon matching the given ElementalTypes and excluding the TypePokemon from the blacklist.
   */
  public static List<Pokemon> getPokemonsByTypesAndExclusions(List<ElementalType> types, List<TypePokemon> blacklist) {
    return types.stream()
      .flatMap(type -> pokemonsByType.getOrDefault(type, Collections.emptyList()).stream())
      .filter(pokemon -> !blacklist.contains(getTypePokemon(pokemon))) // Exclude based on blacklist
      .distinct() // Ensure no duplicates
      .collect(Collectors.toList());
  }

}
