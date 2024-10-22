package com.kingpixel.cobbleutils.Model;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Data;

import java.util.*;

/**
 * @author Carlos Varas Alonso - 23/09/2024 11:54
 */
@Data
public class FilterPokemons {
  // Cache <ModId, <Id, List<Pokemon>>>
  private static final Map<String, Map<String, List<Pokemon>>> cache = new HashMap<>();

  // Order
  private Set<orderFilter> order;

  private enum orderFilter {
    POKEMON,
    TYPE,
    LABEL,
    FORM,
    RARITY
  }

  // Pokemons
  private Set<String> whitelistPokemons;
  private Set<String> blacklistPokemons;
  // Types
  private Set<ElementalType> whitelistTypes;
  private Set<ElementalType> blacklistTypes;
  // Labels
  private Set<String> whitelistLabels;
  private Set<String> blacklistLabels;
  // Forms
  private Set<String> whitelistForms;
  private Set<String> blacklistForms;
  // Aspects
  private Set<String> whitelistAspects;
  private Set<String> blacklistAspects;

  // Rarity
  private Set<String> whitelistRarity;
  private Set<String> blacklistRarity;
  // Also implemented
  private boolean alsoImplemented;
  // Also First Evolution
  private boolean notEvolution;


  public FilterPokemons() {
    // Order
    order = new HashSet<>(Arrays.stream(orderFilter.values()).toList());

    // Pokemons
    whitelistPokemons = new HashSet<>();
    blacklistPokemons = Set.of(
      "egg",
      "pokestop"
    );

    // Types
    whitelistTypes = new HashSet<>(ElementalTypes.INSTANCE.all());
    blacklistTypes = new HashSet<>();

    // Labels
    whitelistLabels = new HashSet<>();
    blacklistLabels = Set.of(
      "mega",
      "gmax",
      "fakemon",
      "custom"
    );

    // Forms
    whitelistForms = new HashSet<>();
    blacklistForms = new HashSet<>();

    // Aspects
    blacklistAspects = new HashSet<>();

    // Rarities
    whitelistRarity = new HashSet<>(CobbleUtils.config.getRarity().keySet());
    blacklistRarity = Set.of("Unknown");

    alsoImplemented = true;
    notEvolution = false;
  }

  public static void removeCache(String modid) {
    cache.remove(modid);
  }

  /**
   * Gets the cache of the pokemons
   *
   * @param modId the mod id
   * @param id    the id
   *
   * @return the list of pokemons
   */
  private List<Pokemon> getCachePokemons(String modId, String id) {
    List<Pokemon> allowedPokemons;
    if (cache.containsKey(modId) && cache.get(modId).containsKey(id)) {
      if (cache.get(modId).get(id).isEmpty()) {
        allowedPokemons = getAllowedPokemons();
        cache.get(modId).put(id, allowedPokemons);
      }
      allowedPokemons = cache.get(modId).get(id);
    } else {
      allowedPokemons = getAllowedPokemons();
      cache.putIfAbsent(modId, new HashMap<>());
      cache.get(modId).put(id, allowedPokemons);
    }
    return allowedPokemons;
  }

  /**
   * Gets a pokemon with properties
   *
   * @param pokemon the pokemon
   *
   * @return the pokemon
   */
  private Pokemon getPokemon(Pokemon pokemon) {
    pokemon.createPokemonProperties(List.of(
      PokemonPropertyExtractor.NATURE,
      PokemonPropertyExtractor.IVS,
      PokemonPropertyExtractor.GENDER
    )).apply(pokemon);
    return pokemon;
  }

  /**
   * Generates a random pokemon
   *
   * @param modId the mod id
   * @param id    the id
   *
   * @return the pokemon
   */
  public Pokemon generateRandomPokemon(String modId, String id) {
    List<Pokemon> allowedPokemons = getCachePokemons(modId, id);
    return getPokemon(allowedPokemons.get(Utils.RANDOM.nextInt(allowedPokemons.size())));
  }


  /**
   * Generates a list of random pokemons
   *
   * @param modId the mod id
   * @param id    the id
   * @param size  the size of the list
   *
   * @return the list of pokemons
   */
  public List<Pokemon> generateRandomPokemons(String modId, String id, int size) {
    List<Pokemon> allowedPokemons = getCachePokemons(modId, id);

    List<Pokemon> pokemons = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      pokemons.add(getPokemon(allowedPokemons.get(Utils.RANDOM.nextInt(allowedPokemons.size()))));
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
          if (aspects.isEmpty()) {
            p = pokemon.create(1);
          } else {
            String aspect = aspects.get(0);

            aspect = aspect.replace("-", "_");

            int lastUnderscore = aspect.lastIndexOf("_");
            if (lastUnderscore != -1) {
              aspect = aspect.substring(0, lastUnderscore) + "=" + aspect.substring(lastUnderscore + 1);
            }

            if (blacklistAspects.contains(aspect)) return;

            if (CobbleUtils.config.isDebug()) {
              CobbleUtils.LOGGER.info("Pokemon: " + pokemon.showdownId() + " Aspect: " + aspect);
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

  private boolean isFirstEvolution(Pokemon pokemon) {
    return pokemon.getPreEvolution() != null;
  }

  private boolean canEgg(Pokemon pokemon) {
    return !pokemon.getForm().getEggGroups().contains(EggGroup.DITTO) || !pokemon.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED);
  }

  /**
   * Checks if a pokemon is allowed
   *
   * @param pokemon the pokemon
   *
   * @return true if the pokemon is allowed
   */
  private boolean isAllowed(Pokemon pokemon) {
    if (notEvolution && isFirstEvolution(pokemon)) return false;
    if (alsoImplemented && !pokemon.getSpecies().getImplemented()) return false;

    // Precalcular tipos
    ElementalType primaryType = pokemon.getPrimaryType();
    ElementalType secondaryType = pokemon.getSecondaryType();
    String showdownId = pokemon.showdownId();
    String rarity = PokemonUtils.getRarityS(pokemon);
    boolean allowed = false;

    for (orderFilter filter : order) {
      switch (filter) {
        case POKEMON:
          if (blacklistPokemons.contains(showdownId) || blacklistPokemons.contains("*")) {
            return false;
          }
          allowed |= whitelistPokemons.contains("*") || whitelistPokemons.contains(showdownId);
          break;

        case TYPE:
          if (blacklistTypes.contains(primaryType) || blacklistTypes.contains(secondaryType)) {
            return false;
          }
          allowed |= whitelistTypes.contains(primaryType) || whitelistTypes.contains(secondaryType);
          break;

        case LABEL:
          boolean hasBlacklistedLabel = blacklistLabels.stream().anyMatch(label -> pokemon.getForm().getLabels().contains(label));
          if (hasBlacklistedLabel || blacklistLabels.contains("*")) {
            return false;
          }
          allowed |= whitelistLabels.contains("*") || whitelistLabels.stream().anyMatch(label -> pokemon.getForm().getLabels().contains(label));
          break;

        case FORM:
          if (blacklistForms.contains(pokemon.getForm().formOnlyShowdownId()) || blacklistForms.contains("*")) {
            return false;
          }
          allowed |= whitelistForms.contains("*") || whitelistForms.contains(pokemon.getForm().formOnlyShowdownId());
          break;

        case RARITY:
          if (blacklistRarity.contains(rarity) || blacklistRarity.contains("*")) {
            return false;
          }
          allowed |= whitelistRarity.contains("*") || whitelistRarity.contains(rarity);
          break;
      }
    }
    return allowed;
  }
}
