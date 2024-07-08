package com.kingpixel.cobbleutils.Model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 28/06/2024 9:41
 */
@Getter
@ToString
public class RewardsData {
  private String playername;
  private UUID playeruuid;
  private List<ItemObject> items;
  private List<JsonObject> pokemons;
  private List<String> commands;
  private Date lastjoin;

  public RewardsData() {
  }

  public RewardsData(String playername, UUID playeruuid) {
    this.playername = playername;
    this.playeruuid = playeruuid;
    this.items = new ArrayList<>();
    this.pokemons = new ArrayList<>();
    this.commands = new ArrayList<>();
    this.lastjoin = new Date();
  }

  public RewardsData(String playername, UUID playeruuid, List<ItemObject> items, List<JsonObject> pokemons,
                     List<String> commands) {
    this.playername = playername;
    this.playeruuid = playeruuid;
    this.items = items;
    this.pokemons = pokemons;
    this.commands = commands;
    this.lastjoin = new Date();
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_REWARDS_DATA, playeruuid + ".json",
      el -> {
        Gson gson = Utils.newGson();
        RewardsData rewards = gson.fromJson(el, RewardsData.class);
        this.playername = rewards.getPlayername();
        this.playeruuid = rewards.getPlayeruuid();
        this.items = rewards.getItems();
        if (items == null) items = new ArrayList<>();
        this.pokemons = rewards.getPokemons();
        if (pokemons == null) pokemons = new ArrayList<>();
        this.commands = rewards.getCommands();
        if (commands == null) commands = new ArrayList<>();
        this.lastjoin = new Date();
        if (lastjoin == null) lastjoin = new Date();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_REWARDS_DATA, playeruuid + ".json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No config.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_REWARDS_DATA, playeruuid + ".json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }
    writeInfo();
  }


  public void writeInfo() {
    Gson gson = Utils.newWithoutSpacingGson();
    String data = gson.toJson(this);
    CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_REWARDS_DATA, playeruuid + ".json",
      data);

    if (!futureWrite.join()) {
      CobbleUtils.LOGGER.fatal("Could not write userinfo file for PokedexRewards.");
    }
  }
}
