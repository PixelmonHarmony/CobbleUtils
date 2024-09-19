package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.Breeding;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;

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
  private long cooldown;

  public PlotBreeding() {
    male = null;
    female = null;
    eggs = new ArrayList<>();
    cooldown = new Date(new Date().getTime() + TimeUnit.MINUTES.toMillis(CobbleUtils.breedconfig.getCooldown())).getTime();
  }

  public void checking(ServerPlayerEntity player) {
    if (!CobbleUtils.breedconfig.isActive()) return;
    if (male == null || female == null) {
      cooldown = new Date(new Date().getTime() + TimeUnit.MINUTES.toMillis(CobbleUtils.breedconfig.getCooldown())).getTime();
      return;
    }

    if (eggs == null) eggs = new ArrayList<>();

    // Obtiene el cooldown configurado en minutos
    long newCooldownMinutes = CobbleUtils.breedconfig.getCooldown();
    long newCooldownMillis = TimeUnit.MINUTES.toMillis(newCooldownMinutes);

    // Si el cooldown actual es mayor que el nuevo cooldown configurado, actualiza el cooldown
    if (cooldown > new Date().getTime() + newCooldownMillis) {
      cooldown = new Date().getTime() + newCooldownMillis;
    }

    if (cooldown < new Date().getTime()) {
      try {
        Pokemon pokemon = EggData.createEgg(
          Pokemon.Companion.loadFromJSON(male),
          Pokemon.Companion.loadFromJSON(female),
          player, this
        );
        if (pokemon != null) {
          if (eggs.size() >= CobbleUtils.breedconfig.getMaxeggperplot())
            return;

          // Establece el nuevo cooldown después de la cría
          cooldown = new Date(new Date().getTime() + newCooldownMillis).getTime();

          if (CobbleUtils.breedconfig.isAutoclaim()) {
            RewardsUtils.saveRewardPokemon(player, pokemon);
          } else {
            eggs.add(pokemon.saveToJSON(new JsonObject()));
          }
          Breeding.managerPlotEggs.writeInfo(player);
        }
      } catch (NoPokemonStoreException e) {
        e.printStackTrace();
      }
    }
  }


  public boolean addMale(Pokemon pokemon) {
    if (pokemon.isLegendary() || pokemon.isUltraBeast())
      return false;
    setMale(pokemon.saveToJSON(new JsonObject()));
    return true;
  }

  public boolean addFemale(Pokemon pokemon) {
    if (pokemon.isLegendary() || pokemon.isUltraBeast())
      return false;
    setFemale(pokemon.saveToJSON(new JsonObject()));
    return true;
  }

  public Pokemon obtainMale() {
    return (male == null ? null : Pokemon.Companion.loadFromJSON(male));
  }

  public Pokemon obtainFemale() {
    return (female == null ? null : Pokemon.Companion.loadFromJSON(female));
  }

  public void add(Pokemon pokemon, Gender gender) {
    if (gender == Gender.MALE) {
      addMale(pokemon);
    } else if (gender == Gender.FEMALE) {
      addFemale(pokemon);
    }
  }

  public Pokemon obtainOtherGender(Gender gender) {
    if (gender == Gender.FEMALE) {
      return obtainMale();
    } else {
      return obtainFemale();
    }
  }
}
