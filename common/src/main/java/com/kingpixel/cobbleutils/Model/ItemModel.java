package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.button.ButtonAction;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.item.PokemonItem;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Carlos Varas Alonso - 28/06/2024 20:02
 */
@Getter
@Setter
@ToString
public class ItemModel {
  private Integer slot;
  private String item;
  private String displayname;
  private List<String> lore = new ArrayList<>();
  private long CustomModelData = 0;
  private String nbt;


  public ItemModel() {
  }

  public ItemModel(String item) {
    this.slot = 0;
    this.item = item;
    this.displayname = "";
    this.lore = new ArrayList<>();
    this.CustomModelData = 0;
    this.nbt = "";
  }

  public ItemModel(String item, String displayname, List<String> lore) {
    this.item = item;
    this.displayname = displayname;
    this.lore = lore;
  }

  public ItemModel(String item, String displayname, List<String> lore, int customModelData) {
    this.item = item;
    this.displayname = displayname;
    this.lore = lore;
    this.CustomModelData = customModelData;
  }

  public ItemModel(Integer slot, String item, String displayname, List<String> lore, int customModelData) {
    this.slot = slot;
    this.item = item;
    this.displayname = displayname;
    this.lore = lore;
    CustomModelData = customModelData;
  }

  public ItemModel(Integer slot, String item, String displayname, List<String> lore, int customModelData, String nbt) {
    this.slot = slot;
    this.item = item;
    this.displayname = displayname;
    this.lore = lore;
    CustomModelData = customModelData;
    this.nbt = nbt;
  }

  public ItemModel(String item, String displayname, List<String> lore, long customModelData) {
    this.item = item;
    this.displayname = displayname;
    this.lore = lore;
    this.CustomModelData = customModelData;
  }

  public ItemModel(Integer slot, String item, String displayname, List<String> lore, long customModelData) {
    this.slot = slot;
    this.item = item;
    this.displayname = displayname;
    this.lore = lore;
    CustomModelData = customModelData;
  }

  public ItemModel(Integer slot, String item, String displayname, List<String> lore, long customModelData, String nbt) {
    this.slot = slot;
    this.item = item;
    this.displayname = displayname;
    this.lore = lore;
    CustomModelData = customModelData;
    this.nbt = nbt;
  }

  public ItemModel(ItemModel itemMoney) {
    this.slot = itemMoney.getSlot();
    this.item = itemMoney.getItem();
    this.displayname = itemMoney.getDisplayname();
    this.lore = itemMoney.getLore();
    CustomModelData = itemMoney.getCustomModelData();
  }

  /**
   * Get the itemstack of the item
   *
   * @return The itemstack of the item
   */
  public ItemStack getItemStack() {
    return getItemStack(this);
  }

  /**
   * Get the itemstack of the item
   *
   * @param amount The amount of the item
   *
   * @return The itemstack of the item
   */
  public ItemStack getItemStack(int amount) {
    return getItemStack(this, amount);
  }

  /**
   * Get the itemstack of the item
   *
   * @param itemModel The item model to get the itemstack
   *
   * @return The itemstack of the item
   */
  public static ItemStack getItemStack(ItemModel itemModel) {
    return getItemStack(itemModel, 1);
  }

  /**
   * Get the itemstack of the item
   *
   * @param itemModel The item model to get the itemstack
   * @param amount    The amount of the item
   *
   * @return The itemstack of the item
   */
  public static ItemStack getItemStack(ItemModel itemModel, int amount) {
    if (itemModel.getItem().startsWith("pokemon:")) {
      return Utils.addThingsItemStack(PokemonItem.from(PokemonProperties.Companion.parse(itemModel.getItem().replace(
        "pokemon:", ""))), itemModel);
    } else if (itemModel.getItem().startsWith("command:")) {
      String command = itemModel.getItem().replace("command:", "");
      for (Map.Entry<String, ItemModel> entry : CobbleUtils.config.getItemsCommands().entrySet()) {
        if (command.startsWith(entry.getKey())) {
          return Utils.parseItemId(entry.getValue().getItem(), amount, entry.getValue().getCustomModelData());
        }
      }
      return Utils.parseItemId("minecraft:command_block", amount);
    } else if (itemModel.getItem().startsWith("head:")) {
      return Utils.getHead(itemModel.getItem().replace("head:", ""), amount);
    } else {
      return Utils.parseItemModel(itemModel, amount);
    }
  }

  /**
   * Get the button of the item
   *
   * @param itemModel The item model to get the button
   * @param action    The action of the button
   *
   * @return The button of the item
   */
  public static GooeyButton getButton(ItemModel itemModel, Consumer<ButtonAction> action) {
    return GooeyButton.builder()
      .display(getItemStack(itemModel))
      .title(AdventureTranslator.toNative(itemModel.getDisplayname()))
      .lore(Text.class, AdventureTranslator.toNativeL(itemModel.getLore()))
      .onClick(action)
      .build();
  }

  /**
   * Get the button of the item
   *
   * @param action The action of the button
   *
   * @return The button of the item
   */
  public GooeyButton getButton(Consumer<ButtonAction> action) {
    return getButton(this, action);
  }
}
