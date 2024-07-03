package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.Model.SizeChance;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * @author Carlos Varas Alonso - 29/04/2024 0:14
 */
@Getter
public class Config {
  private boolean debug;
  private String prefix;
  private String lang;
  private String fill;
  private String ecocommand;
  private List<String> commandrewards;
  private List<String> commandparty;
  private List<String> commmandplugin;
  private boolean randomsize;
  private boolean shulkers;
  private boolean fossil;
  private boolean shinyparticle;
  private boolean pickup;
  private boolean party;
  private boolean rewards;
  private boolean activeshinytoken;
  private boolean directreward;
  private int alertreward;
  private float minpokemonsize;
  private float maxpokemonsize;
  private List<SizeChance> pokemonsizes;
  private List<String> shinytokenBlacklist;
  private List<String> blacklist;
  private List<String> legends;
  private List<String> ultraBeasts;
  private List<String> forms;
  private ItemModel shinytoken;
  private Map<String, ItemModel> itemsCommands;


  public Config() {
    debug = false;
    prefix = "§7[§6CobbleUtils§7] ";
    lang = "en";
    fill = "minecraft:gray_stained_glass_pane";
    ecocommand = "eco deposit %amount% dollars %player%";
    commandparty = List.of("party");
    commandrewards = List.of("rewards");
    commmandplugin = List.of("cobbleutils");
    shulkers = true;
    randomsize = true;
    fossil = true;
    pickup = true;
    shinyparticle = true;
    party = true;
    rewards = true;
    directreward = true;
    activeshinytoken = true;
    minpokemonsize = 0.01f;
    maxpokemonsize = 10f;
    alertreward = 15;
    pokemonsizes = List.of(
      new SizeChance("Tiny", 0.5f, 5),
      new SizeChance("Small", 0.75f, 15),
      new SizeChance("Normal", 1.0f, 75),
      new SizeChance("Big", 1.25f, 15),
      new SizeChance("Giant", 1.5f, 5)
    );
    shinytoken = new ItemModel("minecraft:paper", "<gradient:#e0d234:#ede69a><bold>Shiny Token", List.of("§aShiny Token"));
    shinytokenBlacklist = List.of("ditto");
    blacklist = List.of("ditto");
    legends = List.of("mewtwo", "mew", "rayquaza", "giratina", "arceus", "reshiram", "zekrom", "kyurem", "xerneas",
      "yveltal", "zygarde", "solgaleo", "lunala", "necrozma", "zacian", "zamazenta", "eternatus", "calyrex");
    ultraBeasts = List.of("naganadel", "blacephalon", "stakataka", "kartana", "buzzwole", "pheromosa", "xurkitree",
      "celesteela", "guzzlord", "poipole");
    forms = List.of("Normal", "Hisui", "Galar");
    itemsCommands = new HashMap<>();
    itemsCommands.put("eco", new ItemModel("cobblemon:relic_coin", "<gradient:#e0d234:#ede69a><bold>Money", List.of(
      "§aThis give you a random amount of money")));
    itemsCommands.put("give", new ItemModel("minecraft:chest", "<gradient:#e0d234:#ede69a><bold>Item", List.of(
      "§aThis give you a item")));
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        Config config = gson.fromJson(el, Config.class);
        debug = config.isDebug();
        prefix = config.getPrefix();
        lang = config.getLang();
        fill = config.getFill();
        shinytoken = config.getShinytoken();
        directreward = config.isDirectreward();
        randomsize = config.isRandomsize();
        shulkers = config.isShulkers();
        fossil = config.isFossil();
        pickup = config.isPickup();
        shinyparticle = config.isShinyparticle();
        minpokemonsize = config.getMinpokemonsize();
        maxpokemonsize = config.getMaxpokemonsize();
        pokemonsizes = config.getPokemonsizes();
        party = config.isParty();
        rewards = config.isRewards();
        ecocommand = config.getEcocommand();
        commandparty = config.getCommandparty();
        commandrewards = config.getCommandrewards();
        commmandplugin = config.getCommmandplugin();
        alertreward = config.getAlertreward();
        shinytokenBlacklist = config.getShinytokenBlacklist();
        blacklist = config.getBlacklist();
        itemsCommands = config.getItemsCommands();
        legends = config.getLegends();
        ultraBeasts = config.getUltraBeasts();
        activeshinytoken = config.isActiveshinytoken();
        forms = config.getForms();
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

  public String getSizeName(float size) {
    for (SizeChance sizeChance : pokemonsizes) {
      if (sizeChance.getSize() == size) {
        return sizeChance.getId();
      }
    }
    return "Normal";
  }
}