package com.kingpixel.cobbleutils.Model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 28/06/2024 9:41
 */
@Getter
@Setter
@ToString
public class RewardsData {
  private String playername;
  private UUID playeruuid;
  private List<ItemObject> items;
  private List<JsonObject> pokemons;
  private List<String> commands;

  public RewardsData() {
  }

  public RewardsData(String playername, UUID playeruuid) {
    this.playername = playername;
    this.playeruuid = playeruuid;
    this.items = new ArrayList<>();
    this.pokemons = new ArrayList<>();
    this.commands = new ArrayList<>();
  }

  public RewardsData(String playername, UUID playeruuid, List<ItemObject> items, List<JsonObject> pokemons,
                     List<String> commands) {
    this.playername = playername;
    this.playeruuid = playeruuid;
    this.items = items;
    this.pokemons = pokemons;
    this.commands = commands;
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_REWARDS_DATA, playeruuid + ".json",
      el -> {
        Gson gson = Utils.newWithoutSpacingGson();
        RewardsData rewards = gson.fromJson(el, RewardsData.class);
        this.playername = rewards.getPlayername();
        this.playeruuid = rewards.getPlayeruuid();
        this.items = rewards.getItems();
        this.pokemons = rewards.getPokemons();
        this.commands = rewards.getCommands();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_REWARDS_DATA, playeruuid + ".json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No config.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newWithoutSpacingGson();
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

  public int getAmount() {
    return getCommands().size() + getItems().size() + getPokemons().size();
  }
}
