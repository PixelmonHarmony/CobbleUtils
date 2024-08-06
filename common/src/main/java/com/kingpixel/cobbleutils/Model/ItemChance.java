package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.options.ImpactorItem;
import com.kingpixel.cobbleutils.util.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/**
 * Represents an item chance model with methods to handle rewards.
 */
@Getter
@ToString
public class ItemChance {
  private final String item;
  private final int chance;

  public ItemChance() {
    this.item = "minecraft:dirt";
    this.chance = 100;
  }

  public ItemChance(String item, int chance) {
    this.item = item;
    this.chance = chance;
  }

  /**
   * Get the default item chances.
   * <p>
   * Give a default List<ItemChance> for example configurations.
   *
   * @return The default item chances.
   */
  public static List<ItemChance> defaultItemChances() {
    List<ItemChance> itemChances = new ArrayList<>();
    itemChances.add(new ItemChance("minecraft:dirt", 100));
    itemChances.add(new ItemChance("item:8:minecraft:dirt", 100));
    itemChances.add(new ItemChance("item:8:minecraft:dirt#{CustomModelData:1}", 100));
    itemChances.add(new ItemChance("pokemon:zorua hisuian", 100));
    itemChances.add(new ItemChance("command:give %player% minecraft:dirt", 100));
    itemChances.add(new ItemChance("money:100", 100));
    itemChances.add(new ItemChance("money:tokens:100", 100));
    return itemChances;
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
   * @param player     The player to give the reward to.
   * @param itemChance The ItemChance model specifying the reward.
   *
   * @throws NoPokemonStoreException If there's an issue with storing Pokémon.
   */
  public static boolean giveReward(Player player, ItemChance itemChance) throws NoPokemonStoreException {
    return giveReward(player, itemChance, 1);
  }

  /**
   * Handles giving the reward to the player based on the item type and amount.
   *
   * @param player     The player to give the reward to.
   * @param itemChance The ItemChance model specifying the reward.
   * @param amount     The amount of the item to give.
   *
   * @throws NoPokemonStoreException If there's an issue with storing Pokémon.
   */
  public static boolean giveReward(Player player, ItemChance itemChance, int amount) throws NoPokemonStoreException {
    try {
      String item = itemChance.getItem();
      CobbleUtils.LOGGER.info("ItemChance: " + item);
      if (item.startsWith("pokemon:")) {
        Pokemon pokemon = getRewardPokemon(item);
        return RewardsUtils.saveRewardPokemon(player, pokemon);
      } else if (item.startsWith("command:")) {
        String command = item.replace("command:", "");
        return RewardsUtils.saveRewardCommand(player, command);
      } else if (item.startsWith("money:")) {
        int money;
        CobbleUtils.LOGGER.info("Logintud de money:" + item.split(":").length);
        if (item.split(":").length < 3) {
          money = Integer.parseInt(item.replace("money:", ""));
          player.sendSystemMessage(AdventureTranslator.toNativeWithOutPrefix(
            CobbleUtils.language.getMessageReceiveMoney()
              .replace("%amount%", String.valueOf(money))
          ));
          return RewardsUtils.saveRewardCommand(player, CobbleUtils.config.getEcocommand()
            .replace("%player%", player.getGameProfile().getName())
            .replace("%amount%", String.valueOf(money)));
        } else {
          money = Integer.parseInt(item.split(":")[2]);
          String currency = item.split(":")[1];
          String command = CobbleUtils.config.getImpactorEconomy().getEcocommand();
          ImpactorItem impactorItem = CobbleUtils.config.getImpactorEconomy().getItemsCommands().get(currency);
          player.sendSystemMessage(AdventureTranslator.toNativeWithOutPrefix(
            impactorItem.getMessage()
              .replace("%amount%", String.valueOf(money))
          ));

          command = command
            .replace("%player%", player.getGameProfile().getName())
            .replace("%amount%", String.valueOf(money))
            .replace("%currency%", currency);

          if (CobbleUtils.config.isDebug()) {
            CobbleUtils.LOGGER.info("Command: " + command);
          }

          return RewardsUtils.saveRewardCommand(player, command);
        }
      } else if (item.startsWith("item:")) {
        ItemStack itemStack;
        String[] split = item.split("#", 2);
        String[] itemSplit = split[0].split(":");
        String iditem = itemSplit[2] + ":" + itemSplit[3];
        itemStack = Utils.parseItemId(iditem, Integer.parseInt(itemSplit[1]));
        if (split.length > 1) {
          try {
            itemStack.setTag(TagParser.parseTag(split[1]));
          } catch (PatternSyntaxException | CommandSyntaxException | ArrayIndexOutOfBoundsException ignored) {
          }
        }

        return RewardsUtils.saveRewardItemStack(player, itemStack);
      } else {
        return RewardsUtils.saveRewardItemStack(player, Utils.parseItemId(item, amount));
      }
    } catch (Exception e) {
      CobbleUtils.LOGGER.error("Error al dar la recompensa: " + e.getMessage());
      e.printStackTrace();
      return false;
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


  /**
   * Get the ItemStack of the reward based on its type.
   *
   * @param item   The item to get the ItemStack of.
   * @param amount The amount of the item.
   *
   * @return The ItemStack of the reward.
   */
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
    } else if (item.startsWith("money:")) {
      if (item.split(":").length < 3) {
        ItemModel itemModel = new ItemModel(CobbleUtils.language.getItemMoney());
        return itemModel.getItemStack();
      } else {
        String currency = item.split(":")[1];
        ImpactorItem impactorItem = CobbleUtils.config.getImpactorEconomy().getItemsCommands().get(currency);
        return impactorItem.getItem().getItemStack();
      }
    } else if (item.startsWith("item:")) {
      ItemStack itemStack;
      String[] split = item.split("#", 2);
      String[] itemSplit = split[0].split(":");
      String iditem = itemSplit[2] + ":" + itemSplit[3];
      itemStack = Utils.parseItemId(iditem, Integer.parseInt(itemSplit[1]));
      if (split.length > 1) {
        try {
          itemStack.setTag(TagParser.parseTag(split[1]));
        } catch (PatternSyntaxException | CommandSyntaxException | ArrayIndexOutOfBoundsException ignored) {
        }
      }
      return itemStack;
    } else {
      return Utils.parseItemId(item, amount);
    }
  }

  /**
   * Get the Pokémon reward based on the item.
   *
   * @param item The item to get the Pokémon reward of.
   *
   * @return The Pokémon reward.
   */
  private static Pokemon getRewardPokemon(String item) {
    String p = item.replace("pokemon:", "");
    return PokemonProperties.Companion.parse(p).create();
  }

  /**
   * Get the title of the item based on its type.
   *
   * @param itemChance The ItemChance model specifying the reward.
   *
   * @return The title of the item.
   */
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
      return command;
    } else if (item.startsWith("money:")) {
      int money;
      String title;
      if (item.split(":").length < 3) {
        money = Integer.parseInt(item.replace("money:", ""));
        ItemModel itemModel = new ItemModel(CobbleUtils.language.getItemMoney());
        title = itemModel.getDisplayname()
          .replace("%amount%", String.valueOf(money));
      } else {
        money = Integer.parseInt(item.split(":")[2]);
        String currency = item.split(":")[1];
        ImpactorItem impactorItem = CobbleUtils.config.getImpactorEconomy().getItemsCommands().get(currency);
        return impactorItem.getItem().getDisplayname()
          .replace("%amount%", String.valueOf(money));

      }
      return title;
    } else if (item.startsWith("item:")) {
      ItemStack itemStack;
      String[] split = item.split("#", 2);
      String[] itemSplit = split[0].split(":");
      String iditem = itemSplit[2] + ":" + itemSplit[3];
      itemStack = Utils.parseItemId(iditem, Integer.parseInt(itemSplit[1]));
      if (split.length > 1) {
        try {
          itemStack.setTag(TagParser.parseTag(split[1]));
          if (itemStack.getTag().contains("display")) {
            return ItemUtils.getNameItem(itemStack);
          } else {
            return ItemUtils.getTranslatedName(itemStack);
          }
        } catch (PatternSyntaxException | CommandSyntaxException | ArrayIndexOutOfBoundsException ignored) {
        }
      }
      return ItemUtils.getTranslatedName(itemStack);
    } else {
      return ItemUtils.getTranslatedName(Utils.parseItemId(item));
    }
  }

  /**
   * Get the lore of the item based on its type.
   *
   * @param itemChance The ItemChance model specifying the reward.
   *
   * @return The lore of the item.
   */
  private static List<String> getLore(ItemChance itemChance) {
    return CobbleUtils.language.getLorechance();
  }

  /**
   * Get a random reward from a list of item chances and give it to the player.
   *
   * @param itemChances     The list of item chances to choose from.
   * @param player          The player to give the reward to.
   * @param numberOfRewards The number of rewards to give.
   *
   * @throws IllegalArgumentException If the list of item chances is empty or the number of rewards is less than or equal to zero.
   */
  public static void getRandomRewards(List<ItemChance> itemChances, ServerPlayer player, int numberOfRewards) {
    if (itemChances == null || itemChances.isEmpty()) {
      throw new IllegalArgumentException("The list of item chances cannot be empty");
    }
    if (numberOfRewards <= 0) {
      throw new IllegalArgumentException("The number of rewards must be greater than zero");
    }

    for (int i = 0; i < numberOfRewards; i++) {
      getRandomReward(itemChances, player);
    }
  }

  /**
   * Get a random reward from a list of item chances and give it to the player.
   *
   * @param itemChances The list of item chances to choose from.
   * @param player      The player to give the reward to.
   *
   * @throws IllegalArgumentException If the list of item chances is empty.
   */
  public static void getRandomReward(List<ItemChance> itemChances, ServerPlayer player) {
    if (itemChances == null || itemChances.isEmpty()) {
      throw new IllegalArgumentException("The list of item chances cannot be empty");
    }

    int total = itemChances.stream().mapToInt(ItemChance::getChance).sum();
    int random = Utils.RANDOM.nextInt(total);
    int current = 0;

    for (ItemChance itemChance : itemChances) {
      current += itemChance.getChance();
      if (random < current) {
        try {
          giveReward(player, itemChance);
        } catch (NoPokemonStoreException e) {
          e.printStackTrace();
        }
        break;
      }
    }
  }

  /**
   * Get a random reward from a list of item chances and give it to the player.
   *
   * @param itemChances The list of item chances to choose from.
   * @param player      The player to give the reward to.
   *
   * @return The ItemChance model specifying the reward.
   */
  public static void getAllRewards(List<ItemChance> itemChances, ServerPlayer player) {
    for (ItemChance itemChance : itemChances) {
      try {
        giveReward(player, itemChance);
      } catch (NoPokemonStoreException e) {
        e.printStackTrace();
      }
    }
  }
}
