package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;

@Getter
public class Lang {
  // Titles
  private String titlelootpokestop;
  private String infolootpokestop;
  // Messages
  private String messagerandomitem;
  private String messagearebattle;
  private String messagefossiltime;
  private String messagefossilcomplete;
  private String messagepokestopcooldown;
  // Lores
  private String lorelootpokestop;
  // Time
  private String seconds;
  private String minutes;
  private String hours;
  private String days;

  /**
   * Constructor to generate a file if one doesn't exist.
   */
  public Lang() {
    // Titles
    titlelootpokestop = "&7Loot Pokestop: &a%type%";
    infolootpokestop = "&7Amount rewards: &6%amount%";
    // Messages
    messagerandomitem = "&aYou get a &e%type% &arandomitem &f%item% &6%amount%&a!";
    messagearebattle = "&aYou need to be in a battle to use this command.";
    messagefossiltime = "&aYou need to wait %time% &ato get a new fossil.";
    messagefossilcomplete = "&aYou have completed the fossil.";
    messagepokestopcooldown = "&aCooldown of the pokestop: %cooldown%";
    // Lores
    lorelootpokestop = "&7Chance: &&6%chance%&f%";
    // Time
    seconds = "&6%s% &aseconds";
    minutes = "&6%m% &aminutes";
    hours = "&6%h% &ahours";
    days = "&6%d% &adays";
  }

  /**
   * Method to initialize the config.
   */
  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_LANG, CobbleUtils.config.getLang() + ".json",
      el -> {
        Gson gson = Utils.newGson();
        Lang lang = gson.fromJson(el, Lang.class);
        // Titles
        titlelootpokestop = lang.getTitlelootpokestop();
        infolootpokestop = lang.getInfolootpokestop();
        // Messages
        messagerandomitem = lang.getMessagerandomitem();
        messagearebattle = lang.getMessagearebattle();
        messagefossiltime = lang.getMessagefossiltime();
        messagefossilcomplete = lang.getMessagefossilcomplete();
        messagepokestopcooldown = lang.getMessagepokestopcooldown();
        // Lores
        lorelootpokestop = lang.getLorelootpokestop();
        // Time
        seconds = lang.getSeconds();
        minutes = lang.getMinutes();
        hours = lang.getHours();
        days = lang.getDays();
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
