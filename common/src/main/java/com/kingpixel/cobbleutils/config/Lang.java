package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
public class Lang {
  // Messages
  private String titlemenushiny;
  private String titlemenushinyoperation;
  private String titlemenurewards;
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
  private String messagerandomitem;
  private String messagearebattle;
  private String messagefossiltime;
  private String messagefossilcomplete;
  private String messageNotHaveRewards;
  private String messageHaveRewards;
  private String messageThisPokemonIsShiny;
  private String coloritem;
  private List<String> lorepokemon;
  // Time
  private String seconds;
  private String minutes;
  private String hours;
  private String days;
  private Map<String, String> gender;
  private Map<String, String> forms;
  private ItemModel itemPc;

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
    messageNotHaveRewards = "&cYou don't have rewards.";
    titlemenushinyoperation = "&eShiny Operation";
    titlemenurewards = "&eRewards Menu";
    yes = "&a✔";
    no = "&c✖";
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
    lorepokemon = List.of(
      "&7Level: &f%level%",
      "&6Gender: %gender%",
      "&5Legendary: %legendary%",
      "&5Ultra Beast: %ultrabeast%",
      "&eNature: &f%nature% &f(&a🢙%up%&f/&c🢛%down%&f)",
      "&eBreedable: &f%breedable%",
      "&eTradeable: &f%tradeable%",
      "&6Ability: &f%ability%",
      "&dIVs: &e%ivs%",
      " &cHP: &f%ivshp% &9Atk: &f%ivsatk% &7Def: &f%ivsdef%",
      " &bSpAtk: &f%ivsspa% &eSpDef: &f%ivsspdef% &bSpd: &f%ivsspeed%",
      "&3EVs: &e%evs%",
      " &cHP: &f%evshp% &9Atk: &f%evsatk% &7Def: &f%evsdef%",
      " &bSpAtk: &f%evsspa% &eSpDef: &f%evsspdef% &bSpd: &f%evsspeed%",
      "&6Size: &f%size%",
      "&6Ball: &f%ball%",
      "&6Held Item: &f%item%",
      "&6Form: &f%form%",
      "&2Moves: ",
      " &7- &f%move1%",
      " &7- &f%move2%",
      " &7- &f%move3%",
      " &7- &f%move4%",
      "&aOwner: &f%owner%"
    );
    // Time
    seconds = "&6%s% &aseconds";
    minutes = "&6%m% &aminutes";
    hours = "&6%h% &ahours";
    days = "&6%d% &adays";
    gender = Map.of("M", "&b♂", "F", "&d♀", "N", "&7⚲");
    forms = Map.of("hisui", "&cHisuian");
    itemPc = new ItemModel("cobblemon:pc", "&bPC", List.of(""));
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
        yes = lang.getYes();
        no = lang.getNo();
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
