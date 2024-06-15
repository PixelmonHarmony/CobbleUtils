package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.SizeChance;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * @author Carlos Varas Alonso - 29/04/2024 0:14
 */
@Getter
public class Config {
  private boolean debug;
  private String lang;
  private String fill;
  private boolean randomsize;
  private float minpokemonsize;
  private float maxpokemonsize;
  private List<SizeChance> pokemonsizes;


  public Config() {
    debug = false;
    lang = "en";
    fill = "minecraft:gray_stained_glass_pane";
    randomsize = true;
    minpokemonsize = 0.01f;
    maxpokemonsize = 10f;
    pokemonsizes = List.of(
      new SizeChance("Tiny", 0.5f, 5),
      new SizeChance("Small", 0.75f, 15),
      new SizeChance("Normal", 1.0f, 75),
      new SizeChance("Big", 1.25f, 15),
      new SizeChance("Giant", 1.5f, 5)
    );

  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        Config config = gson.fromJson(el, Config.class);
        debug = config.isDebug();
        lang = config.getLang();
        fill = config.getFill();
        randomsize = config.isRandomsize();
        minpokemonsize = config.getMinpokemonsize();
        maxpokemonsize = config.getMaxpokemonsize();
        pokemonsizes = config.getPokemonsizes();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH, "config.json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No config.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH, "config.json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }

  }

  /**
   * Método para obtener un tamaño de Pokémon basado en las probabilidades configuradas.
   *
   * @return El tamaño del Pokémon seleccionado según las probabilidades.
   */
  public float getRandomPokemonSize() {
    int totalWeight = pokemonsizes.stream().mapToInt(SizeChance::getChance).sum();
    int randomValue = Utils.RANDOM.nextInt(totalWeight) + 1; // +1 para incluir el valor totalWeight

    int currentWeight = 0;
    for (SizeChance sizeChance : pokemonsizes) {
      currentWeight += sizeChance.getChance();
      if (randomValue <= currentWeight) {
        return sizeChance.getSize();
      }
    }
    return 1.0f; // valor predeterminado si algo sale mal
  }
}