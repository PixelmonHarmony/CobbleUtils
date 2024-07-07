package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.*;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an item chance model with methods to handle rewards.
 */
@Getter
@ToString
public class ItemChance {
  private String item;
  private int chance;

  public ItemChance() {
    this.item = "minecraft:dirt";
    this.chance = 100;
  }

  public ItemChance(String item, int chance) {
    this.item = item;
    this.chance = chance;
  }

  /**
   * Get the ItemStack of the item.
   *
   * @return The ItemStack of the item.
   */
  public ItemStack getItemStack() {
    return getItemStack(1);
  }

  /**
   * Get the ItemStack of the item with a specific amount.
   *
   * @param amount The amount of the item.
   *
   * @return The ItemStack of the item.
   */
  public ItemStack getItemStack(int amount) {
    return getRewardItemStack(item, amount);
  }

  /**
   * Handles giving the reward to the player based on the item type.
   *
   * @param player    The player to give the reward to.
   * @param itemModel The ItemChance model specifying the reward.
   *
   * @throws NoPokemonStoreException If there's an issue with storing Pokémon.
   */
  public static boolean giveReward(Player player, ItemChance itemModel) throws NoPokemonStoreException {
    String item = itemModel.getItem();
    if (item.startsWith("pokemon:")) {
      Pokemon pokemon = getRewardPokemon(item);
      return RewardsUtils.saveRewardPokemon(player, pokemon);
    } else if (item.startsWith("command:")) {
      String command = item.replace("command:", "");
      return RewardsUtils.saveRewardCommand(player, command);
    } else {
      return RewardsUtils.saveRewardItemStack(player, Utils.parseItemId(item));
    }
  }

  /**
   * Handles giving the reward to the player based on the item type and amount.
   *
   * @param player    The player to give the reward to.
   * @param itemModel The ItemChance model specifying the reward.
   * @param amount    The amount of the item to give.
   *
   * @throws NoPokemonStoreException If there's an issue with storing Pokémon.
   */
  public static boolean giveReward(Player player, ItemChance itemModel, int amount) throws NoPokemonStoreException {
    String item = itemModel.getItem();
    if (item.startsWith("pokemon:")) {
      Pokemon pokemon = getRewardPokemon(item);
      return RewardsUtils.saveRewardPokemon(player, pokemon);
    } else if (item.startsWith("command:")) {
      String command = item.replace("command:", "");
      return RewardsUtils.saveRewardCommand(player, command);
    } else {
      return RewardsUtils.saveRewardItemStack(player, Utils.parseItemId(item, amount));
    }
  }

  /**
   * Retrieves the title of the item based on its type.
   *
   * @return The title of the item.
   */
  public String getTitle() {
    return getTitle(this);
  }

  /**
   * Retrieves the lore of the item based on its type.
   *
   * @return The lore of the item.
   */
  public List<String> getLore() {
    return getLore(this);
  }

  /**
   * Constructs a GooeyButton instance representing the item button.
   *
   * @param percentage The percentage to display in the lore.
   *
   * @return The constructed GooeyButton instance.
   */
  public GooeyButton getButton(String percentage) {
    String title = getTitle();
    List<String> lore = new ArrayList<>(getLore());
    lore.replaceAll(s -> s.replace("%chance%", percentage));
    return GooeyButton.builder()
      .display(getItemStack())
      .title(AdventureTranslator.toNative(title))
      .lore(Component.class, AdventureTranslator.toNativeL(lore))
      .build();
  }

  // Private methods for internal use

  private static ItemStack getRewardItemStack(String item, int amount) {
    if (item.startsWith("pokemon:")) {
      return PokemonItem.from(PokemonProperties.Companion.parse(item.replace("pokemon:", "")));
    } else if (item.startsWith("command:")) {
      String command = item.replace("command:", "");
      for (Map.Entry<String, ItemModel> entry : CobbleUtils.config.getItemsCommands().entrySet()) {
        if (command.startsWith(entry.getKey())) {
          return Utils.parseItemId(entry.getValue().getItem(), amount);
        }
      }
      return Utils.parseItemId("minecraft:command_block", amount);
    } else {
      return Utils.parseItemId(item, amount);
    }
  }

  private static Pokemon getRewardPokemon(String item) {
    String p = item.replace("pokemon:", "");
    return PokemonProperties.Companion.parse(p).create();
  }

  private static String getTitle(ItemChance itemChance) {
    String item = itemChance.getItem();
    if (item.startsWith("pokemon:")) {
      String p = item.replace("pokemon:", "");
      Pokemon pokemon = PokemonProperties.Companion.parse(p).create();
      return PokemonUtils.replace(CobbleUtils.language.getPokemonnameformat(), pokemon);
    } else if (item.startsWith("command:")) {
      String command = item.replace("command:", "");
      for (Map.Entry<String, ItemModel> entry : CobbleUtils.config.getItemsCommands().entrySet()) {
        if (command.startsWith(entry.getKey())) {
          return entry.getValue().getDisplayname();
        }
      }
      return command; // Return the original command if no match found
    } else {
      return ItemUtils.getNameItem(item);
    }
  }

  private static List<String> getLore(ItemChance itemChance) {
    return CobbleUtils.language.getLorechance();
  }
}
