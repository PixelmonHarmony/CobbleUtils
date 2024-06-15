package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.Pokestop.LootPokestop;
import com.kingpixel.cobbleutils.Model.Pokestop.PokeStopModel;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 14/06/2024 22:41
 */
@Getter
@ToString
public class PokestopConfig {
  private List<PokeStopModel> pokestops;

  public PokestopConfig() {
    this.pokestops = List.of(
      new PokeStopModel("common", 3, 30, List.of(
        new LootPokestop("cobblemon:poke_ball", 100),
        new LootPokestop("cobblemon:great_ball", 50),
        new LootPokestop("cobblemon:ultra_ball", 25))),
      new PokeStopModel("uncommon", 3, 60, List.of(
        new LootPokestop("cobblemon:great_ball", 100),
        new LootPokestop("cobblemon:ultra_ball", 50),
        new LootPokestop("cobblemon:master_ball", 10))),
      new PokeStopModel("rare", 3, 90, List.of(
        new LootPokestop("cobblemon:ultra_ball", 100),
        new LootPokestop("cobblemon:master_ball", 50),
        new LootPokestop("cobblemon:master_ball", 10)))
    );
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH, "pokestops.json",
      el -> {
        Gson gson = Utils.newGson();
        PokestopConfig config = gson.fromJson(el, PokestopConfig.class);
        pokestops = config.pokestops;
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH, "pokestops.json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write pokestops.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No pokestops.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH, "pokestops.json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write pokestops.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }
  }

  public PokeStopModel getPokestop(String type) {
    return pokestops.stream().filter(pokestop -> pokestop.getType().equals(type)).findFirst().orElse(null);
  }
}
