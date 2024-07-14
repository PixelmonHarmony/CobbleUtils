package com.kingpixel.cobbleutils.party.models;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.PlayerInfo;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 28/06/2024 2:38
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class PartyData {
  private String name;
  private PlayerInfo owner;
  private Set<PlayerInfo> members;
  private Set<UUID> invites;

  public PartyData(String name, PlayerInfo owner) {
    this.name = name;
    this.owner = owner;
    this.members = new HashSet<>();
    this.members.add(owner);
    this.invites = new HashSet<>();
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_PARTY_DATA, name + ".json",
      el -> {
        Gson gson = Utils.newWithoutSpacingGson();
        PartyData config = gson.fromJson(el, PartyData.class);
        name = config.getName();
        owner = config.getOwner();
        members = config.getMembers();
        invites = config.getInvites();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_PARTY_DATA, name + ".json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No config.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newWithoutSpacingGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_PARTY_DATA, name + ".json",
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
    CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_PARTY_DATA, name + ".json",
      data);

    if (!futureWrite.join()) {
      CobbleUtils.LOGGER.fatal("Could not write userinfo file for PokedexRewards.");
    }
  }
}
