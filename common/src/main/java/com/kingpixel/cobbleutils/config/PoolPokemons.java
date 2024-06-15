package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.PokemonChance;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 13/06/2024 12:05
 */
@Getter
@ToString
public class PoolPokemons {
  private Map<String, List<PokemonChance>> randompokemons;


  public PoolPokemons() {
    randompokemons = Map.of(
      "legendary", List.of(new PokemonChance("mewtwo", 1)),
      "rare", List.of(new PokemonChance("articuno", 1), new PokemonChance("zapdos", 1), new PokemonChance("moltres", 1)),
      "common", List.of(new PokemonChance("pikachu", 1), new PokemonChance("charmander", 1), new PokemonChance("bulbasaur", 1))
    );
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_RANDOM, "pokemons.json",
      el -> {
        Gson gson = Utils.newGson();
        PoolPokemons config = gson.fromJson(el, PoolPokemons.class);
        randompokemons = config.getRandompokemons();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_RANDOM, "pokemons.json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write items.json file for" + CobbleUtils.MOD_NAME + " .");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No items.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_RANDOM, "pokemons.json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write items.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }

  }

  /**
   * Método para obtener un Pokémon aleatorio basado en las probabilidades configuradas.
   *
   * @param category La categoría de Pokémon de la que seleccionar.
   *
   * @return El Pokémon seleccionado según las probabilidades.
   */
  public String getRandomPokemon(String category) {
    List<PokemonChance> pokemons = randompokemons.get(category);
    if (pokemons == null || pokemons.isEmpty()) {
      return null;
    }

    int totalWeight = pokemons.stream().mapToInt(PokemonChance::getChance).sum();
    int randomValue = Utils.RANDOM.nextInt(totalWeight) + 1;

    int currentWeight = 0;
    for (PokemonChance pokemonChance : pokemons) {
      currentWeight += pokemonChance.getChance();
      if (randomValue <= currentWeight) {
        return pokemonChance.getPokemon();
      }
    }
    return null;
  }
}