package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
public class Lang {
  // Messages
  private String titlemenushiny;
  private String titlemenushinyoperation;
  private String titlemenurewards;
  private String titlepc;
  private String titleparty;
  private String confirm;
  private String cancel;
  private String close;
  private String next;
  private String previous;
  private String empty;
  private String yes;
  private String no;
  private String none;
  private String symbolshiny;
  private String pokemonnameformat;
  private String messageReload;
  private String messagerandomitem;
  private String messagearebattle;
  private String messagefossiltime;
  private String messagefossilcomplete;
  private String messageNotHaveRewards;
  private String messageHaveRewards;
  private String messageThisPokemonIsShiny;
  private String messageNoPokemon;
  private String coloritem;
  private List<String> lorepokemon;
  private List<String> lorechance;
  // Time
  private String seconds;
  private String minutes;
  private String hours;
  private String days;
  private Map<String, String> gender;
  private Map<String, String> forms;
  private Map<String, String> types;
  private ItemModel itemPc;
  private ItemModel itemNoPokemon;
  private ItemModel itemPrevious;
  private ItemModel itemNext;
  private ItemModel itemClose;
  private ItemModel itemConfirm;
  private ItemModel itemCancel;
  private ItemModel itemCommand;

  /**
   * Constructor to generate a file if one doesn't exist.
   */
  public Lang() {
    confirm = "&aConfirm";
    cancel = "&cCancel";
    close = "&cClose";
    next = "&eNext";
    previous = "&ePrevious";
    empty = "&cEmpty";
    titlemenushiny = "&eShiny Menu";
    titlepc = "&bPC";
    titleparty = "&bParty";
    messageNotHaveRewards = "&cYou don't have rewards.";
    titlemenushinyoperation = "&eShiny Operation";
    titlemenurewards = "&eRewards Menu";
    messageReload = "&aReloaded.";
    yes = "&a✔";
    no = "&c✘";
    symbolshiny = " &e⭐";
    none = "&cNone";
    coloritem = "<gradient:#cc7435:#e3ab84>";
    pokemonnameformat = "&e%pokemon%%shiny% %gender% &f(&b%form%&f) &f(&b%level%&f)";
    messageHaveRewards = "&aYou have rewards %amount%.";
    // Messages
    messagerandomitem = "&aYou get a &e%type% &arandomitem &f%item% &6%amount%&a!";
    messagearebattle = "&aYou need to be in a battle to use this command.";
    messagefossiltime = "&aYou need to wait %time% &ato get a new fossil.";
    messagefossilcomplete = "&aYou have completed the fossil.";
    messageThisPokemonIsShiny = "&aThis Pokemon is shiny!";
    messageNoPokemon = "&cNo Pokemon";
    lorepokemon = List.of(
      "<#83dcde>Level: &f%level%",
      "<#14b2dd>Type: &f%types%",
      "<#4969dc>Gender: %gender%",
      "<#ab8fdb>Legendary: %legendary%",
      "<#ab8fdb>Ultra Beast: %ultrabeast%",
      "<#de896f>Tradeable: &f%tradeable%",
      "<#b0eb59>Breedable: &f%breedable%",
      "<#9be8c2>Nature: &f%nature% &f(&a↑%up%&f/&c↓%down%&f)",
      "<#6fa7de>Ability: &f%ability%",
      "<#83a7de>IVs: &e%ivs%&7/&e31",
      " <#ee8339>HP: &f%ivshp% <#e84b48>Atk: &f%ivsatk% <#5d79e1>Def: &f%ivsdef%",
      " <#40b5cd>Sp. Atk: &f%ivsspa% <#f59bc2>Sp. Def: &f%ivsspdef% <#69cd65>Spd: &f%ivsspeed%",
      "<#de8397>EVs: &e%evs%&7/&e510",
      " <#ee8339>HP: &f%evshp% <#e84b48>Atk: &f%evsatk% <#5d79e1>Def: &f%evsdef%",
      " <#40b5cd>Sp. Atk: &f%evsspa% <#f59bc2>Sp. Def: &f%evsspdef% <#69cd65>Spd: &f%evsspeed%",
      "<#e35146>Ball: &f%ball%",
      "<#ecca18>Size: &f%size%",
      "<#ecca18>Held Item: &f%item%",
      "<#ecca18>Form: &f%form%",
      "<#98eb59>Moves: &f%move1% - %move2% - %move3% - %move4%",
      "<#b0eb59>Owner: &f%owner%"
    );
    lorechance = List.of(
      "&7Chance: &e%chance%&f%"
    );
    // Time
    seconds = "&6%s% &aseconds";
    minutes = "&6%m% &aminutes";
    hours = "&6%h% &ahours";
    days = "&6%d% &adays";
    gender = Map.of("M", "&b♂", "F", "&d♀", "N", "&7⚲");
    forms = Map.of("hisui", "&cHisuian");
    types = new HashMap<>();
    types.put("normal", "<gradient:#939393:#C3C3C3>Normal&7");
    types.put("steel", "<gradient:#706F6F:#6F6F6F>Steel&7");
    types.put("poison", "<gradient:#B363CD:#D0A5DE>Poison&7");
    types.put("electric", "<gradient:#E9E13B:#EAE8BA>Electric&7");
    types.put("ice", "<gradient:#87CEEB:#00FFFF>Ice&7");
    types.put("fighting", "<gradient:#D77361:#F1C0B7>Fighting&7");
    types.put("dragon", "<gradient:#8B72CF:#B9A8E7>Dragon&7");
    types.put("water", "<gradient:#5498C5:#9BC6E3>Water&7");
    types.put("rock", "<gradient:#D0953C:#E5BD80>Rock&7");
    types.put("ghost", "<gradient:#4B0082:#9370DB>Ghost&7");
    types.put("bug", "<gradient:#A5CB60:#CBE0A5>Bug&7");
    types.put("grass", "<gradient:#A5CB60:#CBE0A5>Grass&7");
    types.put("flying", "<gradient:#C4E9ED:#E3F5F7>Flying&7");
    types.put("dark", "<gradient:#000000:#303030>Dark&7");
    types.put("fire", "<gradient:#E24D4D:#F69F9F>Fire&7");
    types.put("ground", "<gradient:#B8860B:#D2B48C>Ground&7");
    types.put("psychic", "<gradient:#D74DE2:#DE77E7>Psychic&7");
    types.put("fairy", "<gradient:#9C38A5:#C06EC7>Fairy&7");
    itemPc = new ItemModel("cobblemon:pc", "&bPC", List.of(""), 0);
    itemNoPokemon = new ItemModel("minecraft:barrier", "<gradient:#E05858:#F09E9E>No Pokemon", List.of(""), 0);
    itemPrevious = new ItemModel("minecraft:arrow", "<gradient:#E0A457:#E9C79B>Previous", List.of(""), 0);
    itemNext = new ItemModel("minecraft:arrow", "<gradient:#E0A457:#E9C79B>Next", List.of(""), 0);
    itemClose = new ItemModel("minecraft:barrier", "<gradient:#E05858:#F09E9E>Close", List.of(""), 0);
    itemCommand = new ItemModel("minecraft:command_block", "<gradient:#E0A457:#E9C79B>Command", List.of(""), 0);
    itemConfirm = new ItemModel("minecraft:emerald", "<gradient:#4B9F4B:#7DC97D>Confirm", List.of(""), 0);
    itemCancel = new ItemModel("minecraft:redstone", "<gradient:#E05858:#F09E9E>Cancel", List.of(""), 0);
  }

  /**
   * Method to initialize the config.
   */
  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_LANG, CobbleUtils.config.getLang() + ".json",
      el -> {
        Gson gson = Utils.newGson();
        Lang lang = gson.fromJson(el, Lang.class);
        confirm = lang.getConfirm();
        cancel = lang.getCancel();
        close = lang.getClose();
        next = lang.getNext();
        previous = lang.getPrevious();
        empty = lang.getEmpty();
        titlemenushiny = lang.getTitlemenushiny();
        titlemenushinyoperation = lang.getTitlemenushinyoperation();
        titlemenurewards = lang.getTitlemenurewards();
        titleparty = lang.getTitleparty();
        if (titlemenurewards == null) titlemenurewards = "&eRewards Menu";
        yes = lang.getYes();
        no = lang.getNo();
        titlepc = lang.getTitlepc();
        messageReload = lang.getMessageReload();

        symbolshiny = lang.getSymbolshiny();
        pokemonnameformat = lang.getPokemonnameformat();
        lorepokemon = lang.getLorepokemon();
        messageThisPokemonIsShiny = lang.getMessageThisPokemonIsShiny();
        none = lang.getNone();
        messageNotHaveRewards = lang.getMessageNotHaveRewards();

        // Messages
        messagerandomitem = lang.getMessagerandomitem();
        messagearebattle = lang.getMessagearebattle();
        messagefossiltime = lang.getMessagefossiltime();
        messagefossilcomplete = lang.getMessagefossilcomplete();
        messageHaveRewards = lang.getMessageHaveRewards();
        coloritem = lang.getColoritem();
        // Time
        seconds = lang.getSeconds();
        minutes = lang.getMinutes();
        hours = lang.getHours();
        days = lang.getDays();
        gender = lang.getGender();
        forms = lang.getForms();
        itemPc = lang.getItemPc();
        lorechance = lang.getLorechance();
        types = lang.getTypes();

        itemCommand = lang.getItemCommand();
        itemClose = lang.getItemClose();
        itemNext = lang.getItemNext();
        itemPrevious = lang.getItemPrevious();
        itemNoPokemon = lang.getItemNoPokemon();
        itemConfirm = lang.getItemConfirm();
        itemCancel = lang.getItemCancel();


        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_LANG, CobbleUtils.config.getLang() +
            ".json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write lang.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No lang.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_LANG, CobbleUtils.config.getLang() +
          ".json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write lang.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }
  }

}
