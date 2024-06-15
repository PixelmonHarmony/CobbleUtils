package com.kingpixel.cobbleutils.manager;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.Pokestop.PokeStopData;
import com.kingpixel.cobbleutils.util.PokeStopUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 14/06/2024 22:41
 */
@Getter
@Setter
@ToString
public class PokestopManager {
  // UUID player, UUID pokestop, Date (Cooldown de los jugadores)
  private Map<UUID, Map<UUID, Date>> cooldownplayer;
  // UUID pokestop, String type
  private Map<UUID, PokeStopData> typepokestop;

  public PokestopManager() {
    cooldownplayer = new HashMap<>();
    typepokestop = new HashMap<>();
  }

  public PokestopManager(Map<UUID, Map<UUID, Date>> cooldownplayer) {
    this.cooldownplayer = cooldownplayer;
  }

  public PokestopManager(Map<UUID, Map<UUID, Date>> cooldownplayer, Map<UUID, PokeStopData> typepokestop) {
    this.cooldownplayer = cooldownplayer;
    this.typepokestop = typepokestop;
  }

  /**
   * Method to initialize the config.
   */
  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_DATA,
      "pokestop.json",
      el -> {
        Gson gson = Utils.newWithoutSpacingGson();
        PokestopManager pokestopManager = gson.fromJson(el, PokestopManager.class);
        cooldownplayer = pokestopManager.getCooldownplayer();
        typepokestop = pokestopManager.getTypepokestop();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_DATA, "pokestop.json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write pokestop.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No pokestop.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newWithoutSpacingGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_DATA, "pokestop.json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write pokestop.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }
  }

  public void writeInfo() {
    Gson gson = Utils.newWithoutSpacingGson();
    String data = gson.toJson(this);
    CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_DATA, "pokestop.json",
      data);

    if (!futureWrite.join()) {
      CobbleUtils.LOGGER.fatal("Could not write pokestop.json file for " + CobbleUtils.MOD_NAME + ".");
    }
  }

  public void addIfNotExistsPlayer(UUID playeruuid) {
    if (!cooldownplayer.containsKey(playeruuid)) {
      cooldownplayer.put(playeruuid, new HashMap<>());
    }
  }

  public void addIfNotExistsPokestop(UUID playeruuid, UUID pokestopuuid, int cooldown) {
    if (!cooldownplayer.containsKey(playeruuid)) {
      cooldownplayer.put(playeruuid, new HashMap<>());
    }
    if (!cooldownplayer.get(playeruuid).containsKey(pokestopuuid)) {
      cooldownplayer.get(playeruuid).put(pokestopuuid, PokeStopUtils.getCooldown(cooldown));
    } else {
      cooldownplayer.get(playeruuid).replace(pokestopuuid, PokeStopUtils.getCooldown(cooldown));
    }
  }
}
