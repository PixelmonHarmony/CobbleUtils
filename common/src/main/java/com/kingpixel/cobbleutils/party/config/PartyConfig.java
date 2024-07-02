package com.kingpixel.cobbleutils.party.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;


/**
 * @author Carlos Varas Alonso - 29/04/2024 0:14
 */
@Getter
public class PartyConfig {
  private int maxPartySize;
  private boolean temporalParty;
  private boolean partyleavewhenexit;


  public PartyConfig() {
    maxPartySize = 5;
    temporalParty = true;
    partyleavewhenexit = true;
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_PARTY, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        PartyConfig config = gson.fromJson(el, PartyConfig.class);
        this.maxPartySize = config.getMaxPartySize();
        this.temporalParty = config.isTemporalParty();
        this.partyleavewhenexit = config.isPartyleavewhenexit();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_PARTY, "config.json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No config.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_PARTY, "config.json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }

  }

}