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
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ArraysPokemons {
  public static List<Species> pokemons = new ArrayList<>();
  public static List<Species> legendarys = new ArrayList<>();
  public static List<Species> ultraBeasts = new ArrayList<>();
  public static Map<ElementalType, List<Pokemon>> pokemonsByType = new HashMap<>();
  public static List<Species> all = new ArrayList<>();

  public enum TypePokemon {
    NORMAL,
    LEGENDARY,
    ULTRABEAST,
    PARADOX,
    MYTHICAL,
  }

  public static TypePokemon getTypePokemon(Pokemon p) {
    if (p.isLegendary()) {
      return TypePokemon.LEGENDARY;
    } else if (p.isUltraBeast()) {
      return TypePokemon.ULTRABEAST;
    } else if (p.getSpecies().getLabels().contains("paradox")) {
      return TypePokemon.PARADOX;
    } else if (p.getSpecies().getLabels().contains("mythical")) {
      return TypePokemon.MYTHICAL;
    } else {
      return TypePokemon.NORMAL;
    }
  }

  /**
   * Initialize the Pokémon arrays
   */
  public static void init() {
    Collection<Species> species = PokemonSpecies.INSTANCE.getSpecies();
    all = new ArrayList<>(species);
    Set<String> pokeBlacklist = new HashSet<>();
    CobbleUtils.config.getBlacklist().forEach(pokemonData -> pokeBlacklist.add(pokemonData.getPokename()));

    List<Species> filteredSpecies = species.stream()
      .filter(species1 -> species1.getNationalPokedexNumber() != 9999)
      .filter(species1 -> !pokeBlacklist.contains(species1.showdownId()))
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .toList();

    Map<String, List<Species>> sortedSpecies = filteredSpecies.stream()
      .collect(Collectors.groupingBy(species1 -> {
        Pokemon p = species1.create(1);
        TypePokemon typePokemon = getTypePokemon(p);
        if ((typePokemon != TypePokemon.NORMAL && typePokemon != TypePokemon.ULTRABEAST) || CobbleUtils.config.getLegends().contains(PokemonData.from(p))) {
          return "legendary";
        } else if (p.isUltraBeast()) {
          return "ultrabeast";
        } else {
          return "normal";
        }
      }));

    pokemons = new ArrayList<>(sortedSpecies.getOrDefault("normal", new ArrayList<>()).stream()
      .filter(species1 -> CobbleUtils.spawnRates.getRarity(species1) != -1)
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .toList());
    legendarys = new ArrayList<>(sortedSpecies.getOrDefault("legendary", new ArrayList<>()).stream()
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .toList());
    ultraBeasts = new ArrayList<>(sortedSpecies.getOrDefault("ultrabeast", new ArrayList<>()).stream()
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .toList());

    all = all.stream()
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .toList();

    pokemonsByType = new HashMap<>();

    all.forEach(species1 -> species1.getForms().forEach(formData -> {
      for (ElementalType type : formData.getTypes()) {
        if (!pokemonsByType.containsKey(type)) {
          pokemonsByType.put(type, new ArrayList<>());
        }
        if (formData.getAspects().isEmpty()) {
          pokemonsByType.get(type).add(formData.getSpecies().create(1));
        } else {
          formData.getAspects().forEach(aspect -> {
            if (aspect.equals("male") || aspect.equals("female") || aspect.equals("unknown")) return;

            String form = aspect;

            form = form.replace("-", "_");

            int lastUnderscoreIndex = form.lastIndexOf("_");

            if (lastUnderscoreIndex != -1) {
              form = form.substring(0, lastUnderscoreIndex) + "=" + form.substring(lastUnderscoreIndex + 1);
            }
            pokemonsByType.get(type).add(PokemonProperties.Companion.parse(species1.showdownId() + " " + form).create());
          });
        }
      }
    }));

    ArraysPokemonEvent.FINISH_GENERATE_POKEMONS.emit(all);

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

  public static Pokemon getRandomPokemon(ElementalType type) {
    List<Pokemon> pokemonList = pokemonsByType.get(type);
    if (pokemonList.isEmpty()) {
      throw new IllegalStateException("No Pokémon available for the given type.");
    }

    int level = Utils.RANDOM.nextInt(1, 50);
    Pokemon p = pokemonList.get(Utils.RANDOM.nextInt(pokemonList.size()));
    p.setLevel(level);
    return p;
  }

  public static Pokemon getRandomPokemon(ElementalType type, Set<TypePokemon> blacklist) {
    List<Pokemon> pokemonList = pokemonsByType.get(type);
    if (pokemonList.isEmpty()) {
      throw new IllegalStateException("No Pokémon available for the given type.");
    }

    int level = Utils.RANDOM.nextInt(1, 50);
    return getPokemon((Set<TypePokemon>) blacklist, pokemonList, level);
  }

  public static Pokemon getRandomPokemon(Set<ElementalType> types) {
    List<Pokemon> pokemons = types.stream()
      .flatMap(type -> pokemonsByType.get(type).stream())
      .toList();

    if (pokemons.isEmpty()) {
      return getRandomPokemon(); // Assuming this method exists and returns a default Pokémon.
    }

    int level = Utils.RANDOM.nextInt(1, 50);
    Pokemon p = pokemons.get(Utils.RANDOM.nextInt(pokemons.size()));
    p.setLevel(level);
    return p;
  }

  public static Pokemon getRandomPokemon(Set<ElementalType> types, Set<TypePokemon> blacklist) {
    List<Pokemon> pokemons = types.stream()
      .flatMap(type -> pokemonsByType.get(type).stream())
      .collect(Collectors.toList());

    if (pokemons.isEmpty()) {
      return getRandomPokemon(); // Assuming this method exists and returns a default Pokémon.
    }

    int level = Utils.RANDOM.nextInt(1, 50);
    return getPokemon(blacklist, pokemons, level);
  }

  @NotNull private static Pokemon getPokemon(Set<TypePokemon> blacklist, List<Pokemon> pokemons, int level) {
    Pokemon p;
    TypePokemon typePokemon;

    do {
      p = pokemons.get(Utils.RANDOM.nextInt(pokemons.size()));
      p.setLevel(level);
      typePokemon = getTypePokemon(p);
    } while (blacklist.contains(typePokemon));

    return p;
  }

  public static Iterable<Pokemon> getRandomPokemons(Set<ElementalType> types) {
    return types.stream()
      .flatMap(type -> pokemonsByType.get(type).stream())
      .peek(pokemon -> pokemon.setLevel(Utils.RANDOM.nextInt(1, 50)))
      .collect(Collectors.toList());
  }

  public static Iterable<Pokemon> getRandomPokemons(Set<ElementalType> types, Set<TypePokemon> blacklist) {
    return types.stream()
      .flatMap(type -> pokemonsByType.get(type).stream())
      .peek(pokemon -> pokemon.setLevel(Utils.RANDOM.nextInt(1, 50)))
      .filter(pokemon -> !blacklist.contains(getTypePokemon(pokemon)))
      .toList();
  }

}
