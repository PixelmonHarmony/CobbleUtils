package com.kingpixel.cobbleutils.Model;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.util.ArraysPokemons;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 09/07/2024 20:19
 */
@Getter
@ToString
public class PokemonObject {
  private JsonObject pokemon;
  private String rarity;
  private Date date;

  public PokemonObject(JsonObject pokemon, Date date) {
    this.pokemon = pokemon;
    this.rarity = PokemonUtils.getRarityS(Pokemon.Companion.loadFromJSON(pokemon));
    this.date = date;
  }

  private boolean expired() {
    if (date == null) {
      return false;
    } else {
      new Date().after(date);
    }
    return true;
  }

  /**
   * Get the Pokemon id
   *
   * @return The Pokémon id
   */
  private String getPokemonId() {
    return getPokemon().getSpecies().showdownId();
  }

  /**
   * Get the Pokémon
   *
   * @return The Pokémon
   */
  private Pokemon getPokemon() {
    return Pokemon.Companion.loadFromJSON(pokemon);
  }

  /**
   * Get a new Pokémon
   */
  private void getNewPokemon() {
    this.pokemon = ArraysPokemons.getRandomPokemonNormalRarity(this.rarity).saveToJSON(new JsonObject());
  }

  /**
   * Get a list of Pokémon
   *
   * @param size    The size of the list
   * @param expired If the Pokémon is expired
   * @param minutes The minutes of expiration
   *
   * @return List of Pokémon
   */
  public static List<PokemonObject> getListPokemon(int size, boolean expired, int minutes) {
    List<Pokemon> p = ArraysPokemons.getRandomListPokemonNormalHalf(size, true);
    List<PokemonObject> pokemons = new ArrayList<>();
    p.forEach(pokemon -> {
      PokemonObject pokemonObject;
      if (!expired) {
        pokemonObject = new PokemonObject(pokemon.saveToJSON(new JsonObject()), null);
      } else {
        pokemonObject = new PokemonObject(pokemon.saveToJSON(new JsonObject()), new Date(TimeUnit.MINUTES.toMillis(minutes)));
      }
      pokemons.add(pokemonObject);
    });
    return pokemons;
  }

  /**
   * Get a list of Pokemon
   *
   * @param size    The size of the list
   * @param expired If the Pokémon is expired
   * @param minutes The minutes of expiration
   *
   * @return List of Pokemon legendaries
   */
  public static List<PokemonObject> getListLegendaries(int size, boolean expired, int minutes) {
    List<Pokemon> p = ArraysPokemons.getRandomListPokemonLegendary(size);
    List<PokemonObject> pokemons = new ArrayList<>();
    p.forEach(pokemon -> {
      PokemonObject pokemonObject;
      if (!expired) {
        pokemonObject = new PokemonObject(pokemon.saveToJSON(new JsonObject()), null);
      } else {
        pokemonObject = new PokemonObject(pokemon.saveToJSON(new JsonObject()), new Date(TimeUnit.MINUTES.toMillis(minutes)));
      }
      pokemons.add(pokemonObject);
    });
    return pokemons;
  }

  /**
   * Get a list of Pokemon
   *
   * @param size    The size of the list
   * @param expired If the Pokémon is expired
   * @param minutes The minutes of expiration
   *
   * @return List of Pokemon ultrabeast
   */
  public static List<PokemonObject> getListUltraBeast(int size, boolean expired, int minutes) {
    List<Pokemon> p = ArraysPokemons.getRandomListPokemonUltraBeast(size);
    List<PokemonObject> pokemons = new ArrayList<>();
    p.forEach(pokemon -> {
      PokemonObject pokemonObject;
      if (!expired) {
        pokemonObject = new PokemonObject(pokemon.saveToJSON(new JsonObject()), null);
      } else {
        pokemonObject = new PokemonObject(pokemon.saveToJSON(new JsonObject()), new Date(TimeUnit.MINUTES.toMillis(minutes)));
      }
      pokemons.add(pokemonObject);
    });
    return pokemons;
  }
}
