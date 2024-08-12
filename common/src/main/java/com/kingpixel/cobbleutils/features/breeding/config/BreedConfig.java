package com.kingpixel.cobbleutils.features.breeding.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.features.breeding.models.Incense;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * @author Carlos Varas Alonso - 29/04/2024 0:14
 */
@Getter
@ToString
@Data
public class BreedConfig {
  private String prefix;
  private boolean active;
  private boolean changeuipasture;
  private boolean shifttoopen;
  private boolean autoclaim;
  private boolean obtainAspect;
  private boolean methodmasuda;
  private boolean ditto;
  private boolean doubleditto;
  private boolean spawnEggWorld;
  private List<String> eggcommand;
  private String titleselectplot;
  private String titleplot;
  private String titleselectpokemon;
  private String nameAbandonedEgg;
  private String nameEgg;
  private String nameRandomEgg;
  private float multipliermasuda;
  private float multiplierShiny;
  private int checkEggToBreedInSeconds;
  private int cooldown;
  private int maxeggperplot;
  private int maxplots;
  private int rowmenuselectplot;
  private int rowmenuplot;
  private int rowmenuselectpokemon;
  private int percentagespawnegg;
  private int steps;
  private int cooldowninstaBreedInSeconds;
  private int cooldowninstaHatchInSeconds;
  private String createEgg;
  private String notcancreateEgg;
  private String notdoubleditto;
  private String notditto;
  private String notCompatible;
  private String blacklisted;
  private ItemModel plotItem;
  private ItemModel maleSelectItem;
  private ItemModel femaleSelectItem;
  private List<Integer> plotSlots;
  private List<String> blacklist;
  private List<String> whitelist;
  private List<Incense> incenses;
  //private List<String> nationalities;


  public BreedConfig() {
    this.prefix = "&7[<#82d448>Breeding&7] &8»";
    this.eggcommand = List.of("daycare", "breed");
    this.titleselectplot = "<#82d448>Select Plot";
    this.titleplot = "<#82d448>Plot";
    this.titleselectpokemon = "<#82d448>Select Pokemon";
    this.active = false;
    this.autoclaim = false;
    this.obtainAspect = false;
    this.changeuipasture = true;
    this.methodmasuda = true;
    this.ditto = true;
    this.doubleditto = true;
    this.spawnEggWorld = false;
    this.shifttoopen = true;
    this.multipliermasuda = 1.5f;
    this.multiplierShiny = 1.5f;
    this.cooldown = 30;
    this.maxeggperplot = 3;
    this.maxplots = 3;
    this.steps = 256;
    this.checkEggToBreedInSeconds = 15;
    this.rowmenuselectplot = 3;
    this.rowmenuplot = 3;
    this.rowmenuselectpokemon = 6;
    this.percentagespawnegg = 5;
    this.cooldowninstaBreedInSeconds = 60;
    this.cooldowninstaHatchInSeconds = 60;
    this.plotItem = new ItemModel(0, "minecraft:turtle_egg", "<#82d448>Plot", List.of(
      "&9male: &6%pokemon1%",
      "&dfemale: &6%pokemon2%",
      "&7Eggs: &6%eggs%",
      "&7Cooldown: &6%cooldown%"
    ), 0);

    this.plotSlots = List.of(10,
      12,
      14,
      16,
      18,
      20,
      22,
      24,
      26);
    this.createEgg = "%prefix% <#ecca18>%pokemon1% %shiny1% &f(%form1%&f) <#64de7c>and <#ecca18>%pokemon2% %shiny2% &f(%form2%&f) <#64de7c>have created an egg <#ecca18>%egg%<#64de7c>!";
    this.notcancreateEgg = "%prefix% <#ecca18>%pokemon1% %shiny1% &f(%form1%&f) <#d65549>and <#ecca18>%pokemon2% %shiny2% &f(%form2%&f) <#d65549>can't create an egg!";
    this.notdoubleditto = "%prefix% <#d65549>you can't use two dittos!";
    this.notditto = "%prefix% <#d65549>you can't use one ditto!";
    this.blacklisted = "%prefix% <#ecca18>%pokemon% <#d65549>is blacklisted!";
    this.blacklist = List.of("pokestop", "egg");
    this.whitelist = List.of("manaphy");
    this.nameEgg = "Egg %pokemon%";
    this.nameRandomEgg = "Random Egg";
    this.nameAbandonedEgg = "Abandoned Egg";
    this.notCompatible = "%prefix% <#d65549>%pokemon1% and %pokemon2% is not compatible!";
    this.maleSelectItem = new ItemModel(0, "minecraft:light_blue_wool", "Male", List.of(""), 0);
    this.femaleSelectItem = new ItemModel(0, "minecraft:pink_wool", "Female", List.of(""), 0);
    this.incenses = Incense.defaultIncenses();
    //this.nationalities = List.of("es", "en", "fr", "de", "it", "pt", "jp", "ko", "zh", "ru");
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_BREED, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        BreedConfig config = gson.fromJson(el, BreedConfig.class);
        prefix = config.getPrefix();
        active = config.isActive();
        changeuipasture = config.isChangeuipasture();
        createEgg = config.getCreateEgg();
        ditto = config.isDitto();
        doubleditto = config.isDoubleditto();
        cooldown = config.getCooldown();
        maxeggperplot = config.getMaxeggperplot();
        maxplots = config.getMaxplots();
        notcancreateEgg = config.getNotcancreateEgg();
        autoclaim = config.isAutoclaim();
        notdoubleditto = config.getNotdoubleditto();
        notditto = config.getNotditto();
        spawnEggWorld = config.isSpawnEggWorld();
        blacklist = config.getBlacklist();
        blacklisted = config.getBlacklisted();
        percentagespawnegg = config.getPercentagespawnegg();
        plotItem = config.getPlotItem();
        multiplierShiny = config.getMultiplierShiny();
        eggcommand = config.getEggcommand();
        nameEgg = config.getNameEgg();
        obtainAspect = config.isObtainAspect();
        plotSlots = config.getPlotSlots();
        rowmenuplot = config.getRowmenuplot();
        rowmenuselectplot = config.getRowmenuselectplot();
        rowmenuselectpokemon = config.getRowmenuselectpokemon();
        methodmasuda = config.isMethodmasuda();
        titleplot = config.getTitleplot();
        titleselectplot = config.getTitleselectplot();
        titleselectpokemon = config.getTitleselectpokemon();
        multipliermasuda = config.getMultipliermasuda();
        whitelist = config.getWhitelist();
        steps = config.getSteps();
        checkEggToBreedInSeconds = config.getCheckEggToBreedInSeconds();
        nameAbandonedEgg = config.getNameAbandonedEgg();
        nameRandomEgg = config.getNameRandomEgg();
        notCompatible = config.getNotCompatible();
        cooldowninstaBreedInSeconds = config.getCooldowninstaBreedInSeconds();
        cooldowninstaHatchInSeconds = config.getCooldowninstaHatchInSeconds();
        maleSelectItem = config.getMaleSelectItem();
        femaleSelectItem = config.getFemaleSelectItem();
        shifttoopen = config.isShifttoopen();
        incenses = config.getIncenses();

        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_BREED, "config.json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No config.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_BREED, "config.json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }

  }
}