package com.kingpixel.cobbleutils.util;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.kingpixel.cobbleutils.CobbleUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ArraysPokemons {
  public static ArrayList<Species> pokemons = new ArrayList<>();
  public static ArrayList<Species> legendarys = new ArrayList<>();
  public static ArrayList<Species> ultraBeasts = new ArrayList<>();

  /**
   * Initialize the Pokémon arrays
   */
  public static void init() {
    Collection<Species> species = PokemonSpecies.INSTANCE.getSpecies();
    Set<String> pokeBlacklist = new HashSet<>(CobbleUtils.config.getBlacklist());

    List<Species> filteredSpecies = species.stream()
      .filter(species1 -> species1.getNationalPokedexNumber() != 9999)
      .filter(species1 -> !pokeBlacklist.contains(species1.showdownId()))
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .toList();

    Map<String, List<Species>> sortedSpecies = filteredSpecies.stream()
      .collect(Collectors.groupingBy(species1 -> {
        Pokemon p = new Pokemon();
        p.setSpecies(species1);
        if (p.isLegendary() || CobbleUtils.config.getLegends().contains(species1.showdownId())) {
          return "legendary";
        } else if (p.isUltraBeast()) {
          return "ultrabeast";
        } else {
          return "normal";
        }
      }));

    pokemons = new ArrayList<>(sortedSpecies.getOrDefault("normal", Collections.emptyList()).stream()
      .filter(species1 -> CobbleUtils.spawnRates.getRarity(species1) != -1)
      .toList());
    legendarys = new ArrayList<>(sortedSpecies.getOrDefault("legendary", Collections.emptyList()));
    ultraBeasts = new ArrayList<>(sortedSpecies.getOrDefault("ultrabeast", Collections.emptyList()));

    if (CobbleUtils.config.isDebug()) {
      CobbleUtils.LOGGER.info("Pokemons normal: " + pokemons.size());
      CobbleUtils.LOGGER.info("Pokemons legendary: " + legendarys.size());
      CobbleUtils.LOGGER.info("Pokemons ultrabeast: " + ultraBeasts.size());
    }
  }

  /**
   * Check if the Pokémon has a form allowed
   *
   * @param pokemon The Pokémon to check
   *
   * @return If the Pokémon has a form allowed
   */
  private static boolean hasAllowedForm(Pokemon pokemon) {
    if (pokemon.getSpecies().getForms().isEmpty()) return false;
    return pokemon.getSpecies().getForms().stream()
      .map(FormData::getName)
      .anyMatch(CobbleUtils.config.getForms()::contains);
  }

  /**
   * Get a random form of the Pokémon
   *
   * @param pokemon The Pokémon to get the form
   *
   * @return The random form of the Pokémon
   */
  private static FormData getRandomForm(Pokemon pokemon) {
    List<FormData> forms = pokemon.getSpecies().getForms();
    List<String> allowedForms = new ArrayList<>(CobbleUtils.config.getForms());

    // Filtrar las formas del Pokémon para encontrar una forma permitida
    List<FormData> allowedFormsInPokemon = forms.stream()
      .filter(form -> allowedForms.contains(form.getName()))
      .toList();

    return allowedFormsInPokemon.get(new Random().nextInt(allowedFormsInPokemon.size()));
  }


  /**
   * Create a Pokémon from a species
   *
   * @param species The species to create the Pokémon
   *
   * @return The Pokémon created
   */
  private static Pokemon createPokemon(Species species) {
    Pokemon pokemon = PokemonProperties.Companion.parse(species.showdownId()).create();
    if (CobbleUtils.config.isDebug()) {
      CobbleUtils.LOGGER.info("Pokemon: " + species.getName());
      species.getForms().forEach(forma -> {
        CobbleUtils.LOGGER.info(" -" + forma.getName());
      });
    }
    return pokemon;
  }

  /**
   * Get a random Pokémon
   *
   * @return The random Pokémon
   */
  public static Pokemon getRandomPokemon() {
    Pokemon pokemon = createPokemon(pokemons.get(new Random().nextInt(pokemons.size())));
    if (hasAllowedForm(pokemon)) {
      pokemon.setForm(getRandomForm(pokemon));
      return pokemon;
    }
    return pokemon;
  }

  /**
   * Get a random legendary Pokémon
   *
   * @return The random legendary Pokémon
   */
  public static Pokemon getRandomLegendary() {
    Pokemon pokemon = createPokemon(legendarys.get(new Random().nextInt(legendarys.size())));
    if (hasAllowedForm(pokemon)) {
      pokemon.setForm(getRandomForm(pokemon));
      return pokemon;
    }
    return pokemon;
  }

  /**
   * Get a random ultra beast Pokémon
   *
   * @return The random ultra beast Pokémon
   */
  public static Pokemon getRandomUltraBeast() {
    Pokemon pokemon = createPokemon(ultraBeasts.get(new Random().nextInt(ultraBeasts.size())));
    if (hasAllowedForm(pokemon)) {
      pokemon.setForm(getRandomForm(pokemon));
      return pokemon;
    }
    return pokemon;
  }

  /**
   * Get a random Pokemon
   *
   * @param size The size of the list
   *
   * @return List of random Pokémon without repetitions
   */
  public static List<Pokemon> getRandomListPokemonNormal(int size) {
    List<String> names = new ArrayList<>();
    List<Pokemon> pokemons = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Pokemon pokemon = getRandomPokemon();
      if (!names.contains(pokemon.getSpecies().showdownId())) {
        names.add(pokemon.getSpecies().showdownId());
        pokemons.add(pokemon);
      } else {
        i--;
      }
    }
    return pokemons;
  }

  /**
   * Get a random Pokemon
   *
   * @param size    The size of the list
   * @param roundUp If the list is rounded up
   *
   * @return List of random Pokemons with half common pokemons
   */
  public static List<Pokemon> getRandomListPokemonNormalHalf(int size, boolean roundUp) {
    List<String> names = new ArrayList<>();
    List<Pokemon> commonPokemons = new ArrayList<>();
    List<Pokemon> rarePokemons = new ArrayList<>();
    int halfSize;
    if (roundUp) {
      halfSize = (size + 1) / 2;
    } else {
      halfSize = size / 2;
    }

    while (commonPokemons.size() < halfSize) {
      Pokemon pokemon = getRandomPokemon();
      if (PokemonUtils.getRarityS(pokemon).equalsIgnoreCase("common") &&
        !names.contains(pokemon.getSpecies().showdownId())) {
        names.add(pokemon.getSpecies().showdownId());
        commonPokemons.add(pokemon);
      }
    }

    while (commonPokemons.size() + rarePokemons.size() < size) {
      Pokemon pokemon = getRandomPokemon();
      if (!names.contains(pokemon.getSpecies().showdownId())) {
        names.add(pokemon.getSpecies().showdownId());
        if (PokemonUtils.getRarityS(pokemon).equalsIgnoreCase("common")) {
          commonPokemons.add(pokemon);
        } else {
          rarePokemons.add(pokemon);
        }
      }
    }


    // Combine both lists
    List<Pokemon> pokemons = new ArrayList<>(commonPokemons);
    pokemons.addAll(rarePokemons);

    return pokemons;
  }


  /**
   * Get a random legendary Pokemon
   *
   * @param size The size of the list
   *
   * @return List of random legendary Pokemon without repetitions
   */
  public static List<Pokemon> getRandomListPokemonLegendary(int size) {
    List<String> names = new ArrayList<>();
    List<Pokemon> pokemons = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Pokemon pokemon = getRandomLegendary();
      if (!names.contains(pokemon.getSpecies().showdownId())) {
        names.add(pokemon.getSpecies().showdownId());
        pokemons.add(pokemon);
      } else {
        i--;
      }
    }
    return pokemons;
  }

  /**
   * Get a random ultra beast Pokemon
   *
   * @param size The size of the list
   *
   * @return List of random ultra beast Pokemon without repetitions
   */
  public static List<Pokemon> getRandomListPokemonUltraBeast(int size) {
    List<String> names = new ArrayList<>();
    List<Pokemon> pokemons = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Pokemon pokemon = getRandomUltraBeast();
      if (!names.contains(pokemon.getSpecies().showdownId())) {
        names.add(pokemon.getSpecies().showdownId());
        pokemons.add(pokemon);
      } else {
        i--;
      }
    }
    return pokemons;
  }

  /**
   * Get a random Pokemon
   *
   * @param rarity The rarity of the Pokemon
   *
   * @return Pokemon with rarity
   */
  public static Pokemon getRandomPokemonNormalRarity(String rarity) {
    String s;
    do {
      Pokemon pokemon = getRandomPokemon();
      s = PokemonUtils.getRarityS(pokemon);
      if (s.equalsIgnoreCase(rarity)) {
        return pokemon;
      }
    } while (true);
  }
  
}
