package com.kingpixel.cobbleutils.features.breeding.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.FilterPokemons;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.Model.PokemonChance;
import com.kingpixel.cobbleutils.Model.PokemonData;
import com.kingpixel.cobbleutils.features.breeding.models.EggData;
import com.kingpixel.cobbleutils.features.breeding.models.Incense;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
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
  private boolean obtainPokeBallFromMother;
  private List<String> eggcommand;
  private String titleselectplot;
  private String titleplot;
  private String titleemptyplot;
  private String titleselectpokemon;
  private String nameAbandonedEgg;
  private String nameEgg;
  private String nameRandomEgg;
  private float multipliermasuda;
  private float multiplierShiny;
  private boolean haveMaxNumberIvsForRandom;
  private SuccessItems successItems;
  private int maxIvsRandom;
  private int numberIvsDestinyKnot;
  private int checkEggToBreedInSeconds;
  private int tickstocheck;
  private String permissionAutoClaim;
  private int cooldown;
  private int defaultNumberPlots;
  private Map<String, Integer> cooldowns;
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
  private String notbreedable;
  private String notdoubleditto;
  private String notditto;
  private String notCompatible;
  private String blacklisted;

  private List<EggData.EggForm> eggForms;
  private List<EggData.EggSpecialForm> eggSpecialForms;
  private List<EggData.PokemonRareMecanic> pokemonRareMechanics;

  private ItemModel plotItem;
  private ItemModel plotThereAreEggs;
  private ItemModel maleSelectItem;
  private ItemModel femaleSelectItem;
  private List<Integer> plotSlots;
  // Menu donde seleccionar el pokemon y recoges
  private ItemModel emptySlots;
  private List<Integer> maleSlots;
  private List<Integer> femaleSlots;
  private List<Integer> eggSlots;
  private List<String> blacklist;
  private List<String> whitelist;
  private List<String> blacklistForm;
  private List<Incense> incenses;
  private FilterPokemons pokemonsForDoubleDitto;
  //private List<String> nationalities;


  public BreedConfig() {
    this.prefix = "&7[<#82d448>Breeding&7] &8»";
    this.eggcommand = List.of("daycare", "pokebreed", "breed");
    this.titleselectplot = "<#82d448>Select Plot";
    this.titleplot = "<#82d448>Plot";
    this.titleemptyplot = "<#82d448>Plot";
    this.titleselectpokemon = "<#82d448>Select Pokemon";
    this.active = true;
    this.autoclaim = false;
    this.obtainAspect = false;
    this.changeuipasture = true;
    this.methodmasuda = true;
    this.ditto = true;
    this.doubleditto = true;
    this.spawnEggWorld = false;
    this.shifttoopen = true;
    this.obtainPokeBallFromMother = true;
    this.numberIvsDestinyKnot = 5;
    this.tickstocheck = 20;
    this.multipliermasuda = 1.5f;
    this.multiplierShiny = 1.5f;
    this.permissionAutoClaim = "cobbleutils.breeding.autoclaim";
    this.cooldown = 30;
    this.cooldowns = Map.of(
      "cobbleutils.breeding.cooldown.vip", 15,
      "cobbleutils.breeding.cooldown.vip+", 10,
      "cobbleutils.breeding.cooldown.vip++", 5
    );
    this.defaultNumberPlots = 1;
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
    this.maxIvsRandom = 25;
    this.haveMaxNumberIvsForRandom = false;
    this.successItems = new SuccessItems();
    this.plotItem = new ItemModel(0, "minecraft:turtle_egg", "<#82d448>Plot", List.of(
      "&9male: &6%pokemon1% &f(&b%form1%&f) &f(&b%item1%&f)",
      "&dfemale: &6%pokemon2% &f(&b%form2%&f) &f(&b%item2%&f)",
      "&7Eggs: &6%eggs%",
      "&7Cooldown: &6%cooldown%"
    ), 0);

    this.plotSlots = List.of(
      10,
      12,
      14,
      16,
      18,
      20,
      22,
      24,
      26
    );
    this.plotThereAreEggs = new ItemModel(0, "minecraft:lime_wool", "", List.of(), 0);
    this.maleSlots = List.of();
    this.femaleSlots = List.of();
    this.eggSlots = List.of();
    this.emptySlots = new ItemModel(0, "minecraft:paper", "", List.of(""), 0);
    this.createEgg = "%prefix% <#ecca18>%pokemon1% %shiny1% &f(%form1%&f) <#64de7c>and <#ecca18>%pokemon2% %shiny2% &f(%form2%&f) <#64de7c>have created an egg <#ecca18>%egg%<#64de7c>!";
    this.notcancreateEgg = "%prefix% <#ecca18>%pokemon1% %shiny1% &f(%form1%&f) <#d65549>and <#ecca18>%pokemon2% %shiny2% &f(%form2%&f) <#d65549>can't create an egg!";
    this.notdoubleditto = "%prefix% <#d65549>you can't use two dittos!";
    this.notditto = "%prefix% <#d65549>you can't use one ditto!";
    this.blacklisted = "%prefix% <#ecca18>%pokemon% <#d65549>is blacklisted!";
    this.notbreedable = "%prefix% <#ecca18>%pokemon% <#d65549>is not breedable!";
    this.blacklist = List.of("pokestop", "egg", "manaphy");
    this.whitelist = List.of("manaphy");
    this.nameEgg = "Egg";
    this.nameRandomEgg = "Random Egg";
    this.nameAbandonedEgg = "Abandoned Egg";
    this.notCompatible = "%prefix% <#d65549>%pokemon1% and %pokemon2% is not compatible!";
    this.maleSelectItem = new ItemModel(0, "minecraft:light_blue_wool", "Male", List.of(""), 0);
    this.femaleSelectItem = new ItemModel(0, "minecraft:pink_wool", "Female", List.of(""), 0);
    this.incenses = Incense.defaultIncenses();
    this.blacklistForm = List.of("halloween");

    this.eggForms = List.of(
      new EggData.EggForm("galarian",
        List.of("perrserker", "sirfetchd", "mrrime", "cursola", "runerigus", "obstagoon")),
      new EggData.EggForm("paldean", List.of("clodsire")),
      new EggData.EggForm("hisuian", List.of("overqwil", "sneasler"))
    );

    this.eggSpecialForms = List.of(
      new EggData.EggSpecialForm("region_bias=hisui",
        List.of(new PokemonData("decidueye", "hisuian")))
    );

    this.pokemonRareMechanics = List.of(
      new EggData.PokemonRareMecanic(List.of(
        new PokemonChance("nidoranf", 50),
        new PokemonChance("nidoranm", 50)
      )),
      new EggData.PokemonRareMecanic(List.of(
        new PokemonChance("illumise", 50),
        new PokemonChance("volbeat", 50)
      ))
    );
    pokemonsForDoubleDitto = new FilterPokemons();
    pokemonsForDoubleDitto.setLegendarys(false);

  }

  public String getPermissionplot(int i) {
    return "cobbleutils.breeding.plot." + i;
  }

  @Data
  public static class SuccessItems {
    private double percentageTransmitAH;
    private double percentageDestinyKnot;
    private double percentagePowerItem;
    private double percentageEverStone;

    public SuccessItems() {
      this.percentageTransmitAH = 70.0;
      this.percentageDestinyKnot = 100.0;
      this.percentagePowerItem = 100.0;
      this.percentageEverStone = 100.0;
    }

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
        numberIvsDestinyKnot = config.getNumberIvsDestinyKnot();
        tickstocheck = config.getTickstocheck();
        maxplots = config.getMaxplots();
        notcancreateEgg = config.getNotcancreateEgg();
        autoclaim = config.isAutoclaim();
        titleemptyplot = config.getTitleemptyplot();
        notdoubleditto = config.getNotdoubleditto();
        cooldowns = config.getCooldowns();
        notditto = config.getNotditto();
        spawnEggWorld = config.isSpawnEggWorld();
        blacklist = config.getBlacklist();
        obtainPokeBallFromMother = config.isObtainPokeBallFromMother();
        femaleSlots = config.getFemaleSlots();
        maleSlots = config.getMaleSlots();
        eggSlots = config.getEggSlots();
        blacklisted = config.getBlacklisted();
        percentagespawnegg = config.getPercentagespawnegg();
        multiplierShiny = config.getMultiplierShiny();
        eggcommand = config.getEggcommand();
        nameEgg = config.getNameEgg();
        obtainAspect = config.isObtainAspect();
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
        emptySlots = config.getEmptySlots();
        plotThereAreEggs = config.getPlotThereAreEggs();
        notbreedable = config.getNotbreedable();
        blacklistForm = config.getBlacklistForm();
        eggForms = config.getEggForms();
        eggSpecialForms = config.getEggSpecialForms();
        pokemonRareMechanics = config.getPokemonRareMechanics();
        maxIvsRandom = config.getMaxIvsRandom();
        haveMaxNumberIvsForRandom = config.isHaveMaxNumberIvsForRandom();
        successItems = config.getSuccessItems();
        defaultNumberPlots = config.getDefaultNumberPlots();
        plotItem = config.getPlotItem();
        plotSlots = config.getPlotSlots();
        permissionAutoClaim = config.getPermissionAutoClaim();
        pokemonsForDoubleDitto = config.getPokemonsForDoubleDitto();

        checker(this);

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

  private void checker(BreedConfig breedConfig) {
    if (breedConfig.getPokemonsForDoubleDitto() == null) breedConfig.setPokemonsForDoubleDitto(new FilterPokemons());
  }
}