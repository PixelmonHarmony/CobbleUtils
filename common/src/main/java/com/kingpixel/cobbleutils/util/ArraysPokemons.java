package com.kingpixel.cobbleutils.util;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.kingpixel.cobbleutils.CobbleUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ArraysPokemons {
  public static ArrayList<Species> pokemons = new ArrayList<>();
  public static ArrayList<Species> legendarys = new ArrayList<>();

  public static void init() {
    Collection<Species> species = PokemonSpecies.INSTANCE.getSpecies();
    Set<String> pokeBlacklist = new HashSet<>(CobbleUtils.config.getBlacklist());

    // Primero filtramos los Pokémon que no deben estar en la lista principal
    List<Species> filteredSpecies = species.stream()
      .filter(species1 -> species1.getNationalPokedexNumber() != 9999)
      .filter(species1 -> !pokeBlacklist.contains(species1.showdownId()))
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .toList();

    // Luego particionamos en legendarios y no legendarios
    Map<Boolean, List<Species>> sortedSpecies = filteredSpecies.stream()
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .collect(Collectors.partitioningBy(species1 -> {
        Pokemon p = new Pokemon();
        p.setSpecies(species1);
        boolean isLegendary = p.isLegendary()
          || CobbleUtils.config.getLegends().contains(species1.showdownId());
        return isLegendary;
      }));

    // Filtramos los no legendarios con rareza -1
    pokemons = new ArrayList<>(sortedSpecies.get(false).stream()
      .filter(species1 -> CobbleUtils.spawnRates.getRarity(species1) != -1)
      .toList());

    legendarys = new ArrayList<>(sortedSpecies.get(true));
  }
}
