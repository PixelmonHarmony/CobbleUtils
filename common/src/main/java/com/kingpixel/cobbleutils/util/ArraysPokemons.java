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

  private static boolean hasAllowedForm(Pokemon pokemon) {
    if (pokemon.getSpecies().getForms().isEmpty()) return false;
    return pokemon.getSpecies().getForms().stream()
      .map(FormData::getName)
      .anyMatch(CobbleUtils.config.getForms()::contains);
  }

  private static FormData getRandomForm(Pokemon pokemon) {
    List<FormData> forms = pokemon.getSpecies().getForms();
    List<String> allowedForms = new ArrayList<>(CobbleUtils.config.getForms());

    // Filtrar las formas del Pokémon para encontrar una forma permitida
    List<FormData> allowedFormsInPokemon = forms.stream()
      .filter(form -> allowedForms.contains(form.getName()))
      .toList();
    
    return allowedFormsInPokemon.get(new Random().nextInt(allowedFormsInPokemon.size()));
  }


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

  public static Pokemon getRandomPokemon() {
    Pokemon pokemon = createPokemon(pokemons.get(new Random().nextInt(pokemons.size())));
    if (hasAllowedForm(pokemon)) {
      pokemon.setForm(getRandomForm(pokemon));
      return pokemon;
    }
    return pokemon;
  }


  public static Pokemon getRandomLegendary() {
    Pokemon pokemon = createPokemon(legendarys.get(new Random().nextInt(legendarys.size())));
    if (hasAllowedForm(pokemon)) {
      pokemon.setForm(getRandomForm(pokemon));
      return pokemon;
    }
    return pokemon;
  }


  public static Pokemon getRandomUltraBeast() {
    Pokemon pokemon = createPokemon(ultraBeasts.get(new Random().nextInt(ultraBeasts.size())));
    if (hasAllowedForm(pokemon)) {
      pokemon.setForm(getRandomForm(pokemon));
      return pokemon;
    }
    return pokemon;
  }

}
