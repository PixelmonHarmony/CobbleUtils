package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.Breeding;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    cooldown = applyCooldown(null); // Aplicar cooldown inicial sin jugador
  }

  private long applyCooldown(ServerPlayerEntity player) {
    AtomicInteger cooldown = new AtomicInteger(CobbleUtils.breedconfig.getCooldown());
    CobbleUtils.breedconfig.getCooldowns().forEach((permission, time) -> {
      if (player != null && LuckPermsUtil.checkPermission(player, permission)) {
        if (time < cooldown.get()) {
          cooldown.set(time);
        }
      }
    });
    return TimeUnit.MINUTES.toMillis(cooldown.get()); // Devuelve solo el tiempo de cooldown en milisegundos
  }

  private Pokemon getPokemonMale() {
    return Pokemon.Companion.loadFromJSON(male);
  }

  private Pokemon getPokemonFemale() {
    return Pokemon.Companion.loadFromJSON(female);
  }

  public void checking(ServerPlayerEntity player) {
    if (!CobbleUtils.breedconfig.isActive()) return;
    boolean banPokemon = false;
    if (CobbleUtils.breedconfig.getBlacklist().contains((male == null ? "null" : getPokemonMale().showdownId()))) {
      Cobblemon.INSTANCE.getStorage().getParty(player).add(getPokemonMale());
      male = null;
      banPokemon = true;
    }

    if (CobbleUtils.breedconfig.getBlacklist().contains((female == null ? "null" : getPokemonFemale().showdownId()))) {
      Cobblemon.INSTANCE.getStorage().getParty(player).add(getPokemonFemale());
      female = null;
      banPokemon = true;
    }

    if (banPokemon) {
      Breeding.managerPlotEggs.writeInfo(player);
      return;
    }

    // Obtener el cooldown del jugador en milisegundos
    long playerCooldownMillis = applyCooldown(player);

    long currentTime = new Date().getTime();
    long remainingCooldown = cooldown - currentTime; // Cooldown restante en milisegundos

    // Si el cooldown restante es mayor que el nuevo cooldown basado en permisos, ajustarlo
    if (remainingCooldown > playerCooldownMillis) {
      cooldown = currentTime + playerCooldownMillis;
    }

    if (male == null || female == null) {
      cooldown = currentTime + playerCooldownMillis;
      return;
    }

    if (eggs == null) eggs = new ArrayList<>();

    // Verifica si el cooldown actual ha expirado
    if (cooldown < currentTime) {
      try {
        Pokemon pokemon = EggData.createEgg(getPokemonMale(), getPokemonFemale(), player, this);
        if (pokemon != null) {
          if (eggs.size() >= CobbleUtils.breedconfig.getMaxeggperplot())
            return;

          // Establece el nuevo cooldown personalizado después de la cría
          cooldown = currentTime + playerCooldownMillis;

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
