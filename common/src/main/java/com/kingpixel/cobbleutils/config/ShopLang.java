package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public class ShopLang {
  public static String PATH_SHOP_LANG = ShopConfig.PATH_SHOP + "/lang/";
  private String prefix;
  private String soundBuy;
  private String soundSell;
  private String soundError;
  private String soundOpen;
  private String soundClose;
  private String soundAdd;
  private String soundRemove;
  private String titleBuy;
  private String titleSell;
  private String messageSellSuccess;
  private String messageSellErrorInvalidQuantity;
  private String messageBuySuccess;
  private String messageSellError;
  private List<String> loreProduct;
  private ItemModel balance;
  private ItemModel add1;
  private ItemModel add8;
  private ItemModel add10;
  private ItemModel add16;
  private ItemModel add64;
  private ItemModel remove1;
  private ItemModel remove8;
  private ItemModel remove10;
  private ItemModel remove16;
  private ItemModel remove64;
  private ItemModel confirm;
  private ItemModel cancel;
  private ItemModel buyStacks;

  /**
   * Constructor to generate a file if one doesn't exist.
   */
  public ShopLang() {
    this.prefix = "&7[&6Shop&7] &8»";
    this.soundBuy = "minecraft:entity.experience_orb.pickup";
    this.soundSell = "minecraft:entity.experience_orb.pickup";
    this.soundError = "minecraft:block.note_block.bass";
    this.soundOpen = "cobblemon:pc.on";
    this.soundClose = "cobblemon:pc.off";
    this.soundAdd = "minecraft:entity.experience_orb.pickup";
    this.soundRemove = "minecraft:entity.experience_orb.pickup";
    this.titleBuy = "&6Buy %product%";
    this.titleSell = "&cSell %product%";
    this.messageSellSuccess = "&7You sold %amount% %product% for %sell% %symbol%";
    this.messageBuySuccess = "&7You bought %amount% %product% for %buy% %symbol%";
    this.messageSellError = "&7You don't have enough %currency% to sell %amount% %product%";
    this.messageSellErrorInvalidQuantity = "&7You can't sell %amount% %product% because it's not a multiple of %packageSize%";
    this.loreProduct = List.of(
      "",
      "&7Amount: %amount%x%amountproduct%",
      "&7Buy: &a%buy% %symbol%",
      "&7Sell: &c%sell% %symbol%",
      "",
      "&7Left click to buy",
      "&7Right click to sell",
      "",
      "&7Balance: &e%balance% %currency%",
      "");
    this.balance = new ItemModel(47, "cobblemon:relic_coin_sack", "&6Balance", List.of(
      "&7You have: &e%balance% %currency%"
    ), 0);
    this.add1 = new ItemModel(23, "minecraft:lime_stained_glass_pane", "&aAdd +1", List.of(""), 0);
    this.add8 = new ItemModel(24, "minecraft:lime_stained_glass_pane", "&aAdd +8", List.of(""), 0);
    this.add10 = new ItemModel(24, "minecraft:lime_stained_glass_pane", "&aAdd +10", List.of(""), 0);
    this.add16 = new ItemModel(25, "minecraft:lime_stained_glass_pane", "&aAdd +16", List.of(""), 0);
    this.add64 = new ItemModel(25, "minecraft:lime_stained_glass_pane", "&aAdd +64", List.of(""), 0);
    this.remove1 = new ItemModel(21, "minecraft:red_stained_glass_pane", "&cRemove -1", List.of(""), 0);
    this.remove8 = new ItemModel(20, "minecraft:red_stained_glass_pane", "&cRemove -8", List.of(""), 0);
    this.remove10 = new ItemModel(20, "minecraft:red_stained_glass_pane", "&cRemove -10", List.of(""), 0);
    this.remove16 = new ItemModel(19, "minecraft:red_stained_glass_pane", "&cRemove -16", List.of(""), 0);
    this.remove64 = new ItemModel(19, "minecraft:red_stained_glass_pane", "&cRemove -64", List.of(""), 0);
    this.confirm = new ItemModel(39, "minecraft:lime_stained_glass_pane", "&aConfirm", List.of(""), 0);
    this.buyStacks = new ItemModel(40, "minecraft:lime_stained_glass_pane", "&aBuy Stacks", List.of(""), 0);
    this.cancel = new ItemModel(41, "minecraft:red_stained_glass_pane", "&cCancel", List.of(""), 0);
  }


  /**
   * Method to initialize the config.
   */
  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(PATH_SHOP_LANG, CobbleUtils.config.getLang() +
        ".json",
      el -> {
        Gson gson = Utils.newGson();
        ShopLang lang = gson.fromJson(el, ShopLang.class);
        this.prefix = lang.getPrefix();
        this.soundBuy = lang.getSoundBuy();
        this.soundSell = lang.getSoundSell();
        this.soundError = lang.getSoundError();
        this.soundOpen = lang.getSoundOpen();
        this.soundClose = lang.getSoundClose();
        this.soundAdd = lang.getSoundAdd();
        this.soundRemove = lang.getSoundRemove();
        this.titleBuy = lang.getTitleBuy();
        this.titleSell = lang.getTitleSell();
        this.loreProduct = lang.getLoreProduct();
        this.balance = lang.getBalance();
        this.add1 = lang.getAdd1();
        this.add10 = lang.getAdd10();
        this.add64 = lang.getAdd64();
        this.remove1 = lang.getRemove1();
        this.remove10 = lang.getRemove10();
        this.remove64 = lang.getRemove64();
        this.confirm = lang.getConfirm();
        this.cancel = lang.getCancel();
        this.buyStacks = lang.getBuyStacks();


        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(PATH_SHOP_LANG, CobbleUtils.config.getLang() +
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
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(PATH_SHOP_LANG, CobbleUtils.config.getLang() +
          ".json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write lang.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }
  }

}
