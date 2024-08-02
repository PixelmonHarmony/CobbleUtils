package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.CobbleUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 02/08/2024 13:42
 */
@Getter
@Setter
@ToString
public class PlotBreeding {
  private JsonObject male;
  private JsonObject female;
  private List<JsonObject> eggs;
  private int amount;
  private long cooldown;

  public PlotBreeding() {
    male = null;
    female = null;
    eggs = new ArrayList<>();
    amount = 0;
    cooldown = new Date(1).getTime();
  }

  public void checking(ServerPlayer player) {
    if (male == null || female == null) return;

    if (cooldown < new Date().getTime()) {
      try {
        Pokemon pokemon = EggData.createEgg(Pokemon.Companion.loadFromJSON(male)
          , Pokemon.Companion.loadFromJSON(female),
          player);
        if (pokemon != null) {
          if (eggs.size() >= CobbleUtils.breedconfig.getMaxeggperplot()) {
            return;
          }
          eggs.add(pokemon.saveToJSON(new JsonObject()));
          cooldown =
            new Date(new Date().getTime() + TimeUnit.MINUTES.toMillis(CobbleUtils.breedconfig.getCooldown())).getTime();
        }
      } catch (NoPokemonStoreException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void addMale(Pokemon pokemon) {
    if (pokemon.isLegendary() || pokemon.isUltraBeast()) return;
    setMale(pokemon.saveToJSON(new JsonObject()));
  }

  public Pokemon obtainMale() {
    return (male == null ? null : Pokemon.Companion.loadFromJSON(male));
  }

  public Pokemon obtainFemale() {
    return (female == null ? null : Pokemon.Companion.loadFromJSON(female));
  }
}
