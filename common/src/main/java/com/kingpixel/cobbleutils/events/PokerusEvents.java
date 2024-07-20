package com.kingpixel.cobbleutils.events;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.EVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.options.Pokerus;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Carlos Varas Alonso - 20/07/2024 6:45
 */
public class PokerusEvents {
  public static void register() {

    EntityEvent.ADD.register((entity, level) -> {
      if (!CobbleUtils.config.getPokerus().isActive()) return EventResult.pass();
      if (entity instanceof PokemonEntity pokemon) {
        if (pokemon.getPokemon().isPlayerOwned()) return EventResult.pass();
        CobbleUtils.config.getPokerus().apply(pokemon.getPokemon(), false);
      }
      return EventResult.pass();
    });

    CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, (evt) -> {
      List<Pokemon> pokemons = new ArrayList<>();
      Pokemon pokemonkilled = evt.getKilled().getEffectedPokemon();
      for (ActiveBattlePokemon activeBattlePokemon : evt.getBattle().getActivePokemon()) {
        assert activeBattlePokemon.getBattlePokemon() != null;
        Pokemon pokemon = activeBattlePokemon.getBattlePokemon().getEntity().getPokemon();
        if (pokemon.isPlayerOwned()) {
          pokemons.add(pokemon);
        }
      }

      for (Pokemon pokemon : pokemons) {
        boolean pokerus = pokemon.getPersistentData().getBoolean("pokerus");
        if (pokerus)
          pokemonkilled.getSpecies().getEvYield().forEach((stat, integer) -> {
            EVs evs = pokemon.getEvs();
            double multiplier = CobbleUtils.config.getPokerus().getMultiplier();
            int adjustedValue;
            if (integer < 0) return;
            if (integer == 1) {
              adjustedValue = (int) multiplier;
            } else {
              double value = integer * multiplier - 3;
              if (CobbleUtils.config.getPokerus().isRoundup()) {
                adjustedValue = (int) Math.ceil(value);
              } else {
                adjustedValue = (int) Math.floor(value);
              }
            }

            evs.set(stat, evs.get(stat) + adjustedValue);
          });
      }

      return Unit.INSTANCE;
    });


    CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, (evt) -> {
      if (!CobbleUtils.config.getPokerus().isActive()) return Unit.INSTANCE;

      List<ServerPlayer> players = new ArrayList<>();
      Set<PokemonEntity> processedPokemon = new HashSet<>();

      // Process all battle actors
      evt.getWinners().forEach(winner -> {
        if (winner instanceof PlayerBattleActor playerBattleActor) {
          players.add(playerBattleActor.getEntity());
        }
      });

      evt.getLosers().forEach(loser -> {
        if (loser instanceof PlayerBattleActor playerBattleActor) {
          players.add(playerBattleActor.getEntity());
        }
      });

      // Process each player's party for Pokerus
      players.forEach(player -> {
        PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
        AtomicBoolean hasPokerus = new AtomicBoolean(false);
        playerPartyStore.forEach(pokemon -> {
          if (pokemon.getPersistentData().getBoolean("pokerus"))
            hasPokerus.set(true);
        });

        if (hasPokerus.get()) {
          playerPartyStore.forEach(Pokerus::applywithrarity);
        }
      });

      return Unit.INSTANCE;
    });

  }

  /*private static void processPokemon(PokemonBattleActor pokemonBattleActor, BattleVictoryEvent evt,
                                      Set<PokemonEntity> processedPokemon, boolean isWinner) {
    PokemonEntity entity = pokemonBattleActor.getEntity();
    if (entity != null && processedPokemon.add(entity)) {
      Pokemon pokemon = entity.getPokemon();
      if (pokemon.getPersistentData().getBoolean("pokerus")) {
        if (isWinner) {
          // Apply EVs bonus for Pokerus for winning Pokémon
          evt.getLosers().forEach(loser -> {
            if (loser instanceof PokemonBattleActor loserPokemonActor) {
              PokemonEntity loserEntity = loserPokemonActor.getEntity();
              if (loserEntity != null) {
                loserEntity.getPokemon().getSpecies().getEvYield().forEach((stat, value) -> {
                  EVs evs = pokemon.getEvs();
                  int bonusValue;
                  if (value == 0) return;
                  if (value == 1) {
                    bonusValue = (int) CobbleUtils.config.getPokerus().getMultiplier();
                  } else {
                    bonusValue = (int) (value * CobbleUtils.config.getPokerus().getMultiplier());
                  }
                  evs.set(stat, evs.get(stat) + bonusValue);
                });
              }
            }
          });
        }
      } else {
        CobbleUtils.config.getPokerus().apply(pokemon, true);
      }
    }
  }*/
}
