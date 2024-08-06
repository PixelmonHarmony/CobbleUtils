package com.kingpixel.cobbleutils.features.breeding.events;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import kotlin.Unit;

/**
 * @author Carlos Varas Alonso - 23/07/2024 22:07
 */
public class EggThrow {
  public static void register() {
    CobblemonEvents.POKEMON_SENT_PRE.subscribe(Priority.HIGHEST, (evt) -> {
      if (evt.getPokemon().getSpecies().showdownId().equalsIgnoreCase("egg")) {
        evt.getPokemon().setCurrentHealth(0);
      }
      return Unit.INSTANCE;
    });
    CobblemonEvents.BATTLE_STARTED_PRE.subscribe(Priority.NORMAL, (evt) -> {
      evt.getBattle().getPlayers().forEach(player -> Cobblemon.INSTANCE.getStorage().getParty(player).forEach(pokemon -> {
        if (pokemon.showdownId().equalsIgnoreCase("egg")) {
          pokemon.setCurrentHealth(0);
        }
      }));
      return Unit.INSTANCE;
    });
  }
}
