package com.kingpixel.cobbleutils.features.breeding.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.kingpixel.cobbleutils.Model.CobbleUtilsTags;
import com.kingpixel.cobbleutils.features.breeding.Breeding;
import kotlin.Unit;

/**
 * @author Carlos Varas Alonso - 08/08/2024 15:39
 */
public class NationalityPokemon {
  public static void register() {
    CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, (evt) -> {
      String country = Breeding.playerCountry.get(evt.getPlayer().getUUID());
      if (country == null) return Unit.INSTANCE;
      evt.getPokemon().getPersistentData().putString(CobbleUtilsTags.COUNTRY_TAG, country);
      return Unit.INSTANCE;
    });
  }
}
