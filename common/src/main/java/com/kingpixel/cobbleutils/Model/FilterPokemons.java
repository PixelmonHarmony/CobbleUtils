package com.kingpixel.cobbleutils.Model;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Carlos Varas Alonso - 23/09/2024 11:54
 */
@Data
public class FilterPokemons {
  private Set<String> whitelistPokemons;
  private Set<String> blacklistPokemons;
  private Set<ElementalType> whitelistTypes;
  private Set<ElementalType> blacklistTypes;
  private Set<String> whitelistLabels;
  private Set<String> blacklistLabels;
  private Set<String> whitelistForms;
  private Set<String> blacklistForms;
  private float spawnrate;

  public FilterPokemons() {
    whitelistPokemons = new HashSet<>();
    blacklistPokemons = new HashSet<>();
    whitelistTypes = new HashSet<>(ElementalTypes.INSTANCE.all());
    blacklistTypes = new HashSet<>();
    whitelistLabels = Set.of(
      "fakemon",
      "legendary"
    );
    blacklistLabels = new HashSet<>();
    whitelistForms = new HashSet<>();
    blacklistForms = new HashSet<>();
    spawnrate = 0f;
  }

  /**
   * Generates a random pokemon
   *
   * @return the pokemon
   */
  public Pokemon generateRandomPokemon() {
    List<Pokemon> allowedPokemons = getAllowedPokemons();
    return allowedPokemons.get(Utils.RANDOM.nextInt(allowedPokemons.size()));
  }

  /**
   * Generates a list of random pokemons
   *
   * @param size the size of the list
   *
   * @return the list of pokemons
   */
  public List<Pokemon> generateRandomPokemons(int size) {
    List<Pokemon> allowedPokemons = getAllowedPokemons();
    List<Pokemon> pokemons = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      pokemons.add(allowedPokemons.get(Utils.RANDOM.nextInt(allowedPokemons.size())));
    }
    return pokemons;
  }

  /**
   * Gets all the allowed pokemons
   *
   * @return the list of allowed pokemons
   */
  public List<Pokemon> getAllowedPokemons() {
    List<Pokemon> allowedPokemons = new ArrayList<>();
    PokemonSpecies.INSTANCE.getSpecies().forEach(pokemon -> {
      List<FormData> forms = pokemon.getForms();
      if (forms.isEmpty()) {
        Pokemon p = pokemon.create(1);
        if (isAllowed(p)) {
          allowedPokemons.add(p);
        }
      } else {
        forms.forEach(form -> {
          Pokemon p;
          List<String> aspects = form.getAspects();
          String aspect = aspects.isEmpty() ? "" : aspects.get(0);

          aspect = aspect.replace("-", "_");

          int lastUnderscore = aspect.lastIndexOf("_");
          if (lastUnderscore != -1) {
            aspect = aspect.substring(0, lastUnderscore) + "=" + aspect.substring(lastUnderscore + 1);
          }

          if (aspects.isEmpty()) {
            p = pokemon.create(1);
          } else {
            if (CobbleUtils.config.isDebug()) {
              CobbleUtils.LOGGER.info("Pokemon: " + pokemon.getName() + " - " + form.getName() + " - " + aspect);
            }
            p = PokemonProperties.Companion.parse(pokemon.showdownId() + " " + aspect).create();
          }

          if (isAllowed(p)) {
            allowedPokemons.add(p);
          }
        });
      }
    });

    return allowedPokemons;
  }

  /**
   * Checks if a pokemon is allowed
   *
   * @param pokemon the pokemon
   *
   * @return true if the pokemon is allowed
   */
  private boolean isAllowed(Pokemon pokemon) {
    if (whitelistPokemons.contains("*") || whitelistPokemons.contains(pokemon.showdownId())) return true;

    if (blacklistPokemons.contains(pokemon.showdownId()) || blacklistPokemons.contains("*")) return false;

    if (whitelistTypes.contains(pokemon.getPrimaryType()) || whitelistTypes.contains(pokemon.getSecondaryType()))
      return true;

    if (blacklistTypes.contains(pokemon.getPrimaryType()) || blacklistTypes.contains(pokemon.getSecondaryType()))
      return false;

    if (whitelistLabels.contains("*") || whitelistLabels.stream().anyMatch(label -> pokemon.getForm().getLabels().contains(label)))
      return true;

    if (blacklistLabels.stream().anyMatch(label -> pokemon.getForm().getLabels().contains(label)) || blacklistLabels.contains("*"))
      return false;

    if (whitelistForms.contains("*") || whitelistForms.contains(pokemon.getForm().formOnlyShowdownId())) return true;

    if (blacklistForms.contains(pokemon.getForm().formOnlyShowdownId()) || blacklistForms.contains("*")) return false;

    return false;
  }


}
