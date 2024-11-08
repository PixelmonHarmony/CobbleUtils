package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
  private static Map<String, List<ItemMod>> modItems = new HashMap<>();

  @Getter
  @Setter
  @ToString
  public static class ItemMod {
    private String itemId;
    private ItemStack itemStack;

    public ItemMod(String itemId, ItemStack itemStack) {
      this.itemId = itemId;
      this.itemStack = itemStack;
    }
  }

  public ItemChance() {
    this("minecraft:dirt", 100);
  }

  public ItemChance(String item, int chance) {
    this.item = item;
    this.chance = chance;
  }

  public static void addModItem(String modid, String itemId, ItemStack itemStack) {
    if (!modItems.containsKey(modid)) {
      modItems.put(modid, new ArrayList<>());
    }
    ItemMod itemMod = new ItemMod(itemId, itemStack);
    boolean exists = modItems.get(modid).stream().anyMatch(i -> i.getItemId().equals(itemId));
    if (!exists) {
      modItems.get(modid).add(itemMod);
    }
  }

  /**
   * Determines the type of item chance.
   *
   * @return The type of item chance.
   */
  public ItemChanceType getType() {
    if (item.startsWith("pokemon:")) return ItemChanceType.POKEMON;
    if (item.startsWith("command:")) return ItemChanceType.COMMAND;
    if (item.startsWith("money:")) return ItemChanceType.MONEY;
    if (item.startsWith("mod:")) return ItemChanceType.MOD;
    return ItemChanceType.ITEM;
  }

  public enum ItemChanceType {
    POKEMON, COMMAND, MONEY, ITEM, MOD
  }

  /**
   * Gets default item chances.
   *
   * @return The default item chances.
   */
  public static List<ItemChance> defaultItemChances() {
    List<ItemChance> itemChances = new ArrayList<>();
    itemChances.add(new ItemChance("minecraft:dirt", 100));
    itemChances.add(new ItemChance("item:8:minecraft:dirt", 100));
    itemChances.add(new ItemChance("item:8:minecraft:dirt#{CustomModelData:1}", 100));
    itemChances.add(new ItemChance("pokemon:zorua hisuian", 100));
    itemChances.add(new ItemChance("command:lp user %player% permission set a", 100));
    itemChances.add(new ItemChance("command:lp user %player% permission set a#lp user %player% permission set b", 100));
    itemChances.add(new ItemChance("money:100", 100));
    itemChances.add(new ItemChance("money:tokens:100", 100));
    itemChances.add(new ItemChance("mod:cobblehunt:radar", 100));
    return itemChances;
  }

  /**
   * Gets the ItemStack of the item with a specific amount.
   *
   * @param amount The amount of the item.
   *
   * @return The ItemStack of the item.
   */
  public ItemStack getItemStack(int amount) {
    return getRewardItemStack(item, amount);
  }

  public ItemStack getItemStack() {
    return getItemStack(1);
  }

  public static boolean giveReward(ServerPlayerEntity player, ItemChance itemChance) throws NoPokemonStoreException {
    return giveReward(player, itemChance, 1);
  }

  /**
   * Gives a reward to the player based on the item type and amount.
   *
   * @param player The player to give the reward to.
   * @param amount The amount of the item to give.
   *
   * @return True if the reward was given successfully, false otherwise.
   */
  public static boolean giveReward(ServerPlayerEntity player, ItemChance itemChance, int amount) {
    try {
      String item = itemChance.getItem();
      ItemStack itemStack;

      if (item.startsWith("pokemon:")) {
        Pokemon pokemon = getRewardPokemon(item);
        return RewardsUtils.saveRewardPokemon(player, pokemon);
      } else if (item.startsWith("command:")) {
        String command = item.replace("command:", "");
        String[] parts = command.split("#");
        if (parts.length > 1) {
          for (String part : parts) {
            RewardsUtils.saveRewardCommand(player, part);
          }
          return true;
        } else {
          return RewardsUtils.saveRewardCommand(player, command);
        }
      } else if (item.startsWith("money:")) {
        return handleMoneyReward(player, item);
      } else if (item.startsWith("item:")) {
        itemStack = parseItemStack(item, parseAmount(item.split(":")[1]) * amount);
        return RewardsUtils.saveRewardItemStack(player, itemStack);
      } else if (item.startsWith("mod:")) {
        itemStack = getModItem(itemChance);
        return RewardsUtils.saveRewardItemStack(player, itemStack);
      } else {
        itemStack = Utils.parseItemId(item, amount);
        return RewardsUtils.saveRewardItemStack(player, itemStack);
      }
    } catch (Exception e) {
      CobbleUtils.LOGGER.error("Error giving reward: " + e.getMessage());
      return false;
    }
  }

  private static boolean handleMoneyReward(ServerPlayerEntity player, String item) {
    int money;
    String currency = "";
    switch (item.split(":").length)
    {
      case 2:
        money = Integer.parseInt(item.split(":")[1]);
        currency = "dollars";
        break;
      case 3:
        money = Integer.parseInt(item.split(":")[2]);
        currency = item.split(":")[1];
        break;
      default:
        money = Integer.parseInt(item.replace("money:", ""));
        break;
    }

    CobbleUtils.LOGGER.info("Money: " + money + " Currency: " + currency + " Player: " + player.getGameProfile().getName() + " Split: " + item.split(":").length);

    if (!CobbleUtils.language.getMessageReceiveMoney().isEmpty()){
      player.sendMessage(AdventureTranslator.toNativeWithOutPrefix(
        CobbleUtils.language.getMessageReceiveMoney()
          .replace("%amount%", String.valueOf(money))));
    }

    return EconomyUtil.addMoney(player, currency, BigDecimal.valueOf(money));
  }

  private static ItemStack parseItemStack(String item, int amount) {
    ItemStack itemStack;
    String[] split = item.split("#", 2);
    String[] itemSplit = split[0].split(":");
    String iditem = itemSplit[2] + ":" + itemSplit[3];
    itemStack = Utils.parseItemId(iditem, Integer.parseInt(itemSplit[1]));
    itemStack.setCount(amount);
    if (split.length > 1) {
      try {
        itemStack.setNbt(NbtHelper.fromNbtProviderString(split[1]));
      } catch (PatternSyntaxException | CommandSyntaxException | ArrayIndexOutOfBoundsException ignored) {
      }
    }
    return itemStack;
  }

  private static Pokemon getRewardPokemon(String item) {
    String p = item.replace("pokemon:", "");
    if (p.isEmpty()) {
      return ArraysPokemons.getRandomPokemon();
    } else {
      return PokemonProperties.Companion.parse(p).create();
    }
  }

  /**
   * Gets the title of the item based on its type.
   *
   * @return The title of the item.
   */
  public String getTitle() {
    return getTitle(this);
  }

  private static ItemStack getModItem(ItemChance itemChance) {
    String[] parts = itemChance.getItem().split(":");
    String modid = parts[1];
    String itemId = parts[2];
    ItemStack itemStack =
      modItems.getOrDefault(modid, new ArrayList<>())
        .stream()
        .filter(i -> i.getItemId().equalsIgnoreCase(itemId))
        .findFirst()
        .orElse(new ItemMod("", Items.DIRT.getDefaultStack())).getItemStack().copy();
    return itemStack;
  }

  /**
   * Gets the lore of the item based on its type.
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
      .lore(Text.class, AdventureTranslator.toNativeL(lore))
      .build();
  }

  /**
   * Gets the ItemStack of the reward based on its type.
   *
   * @param item   The item to get the ItemStack of.
   * @param amount The amount of the item.
   *
   * @return The ItemStack of the reward.
   */
  private static ItemStack getRewardItemStack(String item, int amount) {
    if (item.startsWith("pokemon:")) {
      return PokemonItem.from(getRewardPokemon(item));
    } else if (item.startsWith("command:")) {
      return parseCommandItem(item);
    } else if (item.startsWith("money:")) {
      return parseMoneyItem(item);
    } else if (item.startsWith("item:")) {
      return parseItemStack(item, parseAmount(item.split(":")[1]) * amount);
    } else if (item.startsWith("mod:")) {
      return getModItem(new ItemChance(item, 100));
    } else {
      return Utils.parseItemId(item, amount);
    }
  }

  private static ItemStack parseCommandItem(String item) {
    String command = item.replace("command:", "");
    for (Map.Entry<String, ItemModel> entry : CobbleUtils.config.getItemsCommands().entrySet()) {
      if (command.startsWith(entry.getKey())) {
        return entry.getValue().getItemStack();
      }
    }
    return Utils.parseItemId("minecraft:command_block", 1);
  }

  private static ItemStack parseMoneyItem(String item) {
    String[] parts = item.split(":");
    String currency = "money";
    int amount = 1;

    switch (parts.length) {
      case 2:
        amount = Integer.parseInt(parts[1]);
        break;
      case 3:
        currency = parts[1];
        amount = Integer.parseInt(parts[2]);
        break;
    }

    ItemModel impactorItem = new ItemModel(CobbleUtils.language.getItemsEconomy().getOrDefault(currency,
      new ItemModel(CobbleUtils.language.getItemMoney())));
    impactorItem.setDisplayname(impactorItem.getDisplayname().replace("%amount%", String.valueOf(amount)));

    return impactorItem.getItemStack();
  }

  private static int parseAmount(String amountStr) {
    try {
      return Integer.parseInt(amountStr);
    } catch (NumberFormatException e) {
      return 1;
    }
  }


  private static String getTitle(ItemChance itemChance) {
    String item = itemChance.getItem();
    if (item.startsWith("pokemon:")) {
      return getPokemonTitle(item);
    } else if (item.startsWith("command:")) {
      return getCommandTitle(item);
    } else if (item.startsWith("money:")) {
      return getMoneyTitle(item);
    } else if (item.startsWith("item:")) {
      return getItemTitle(item);
    } else if (item.startsWith("mod:")) {
      return ItemUtils.getTranslatedName(getModItem(itemChance));
    } else {
      return ItemUtils.getTranslatedName(Utils.parseItemId(item));
    }
  }

  private static String getPokemonTitle(String item) {
    Pokemon pokemon = getRewardPokemon(item);
    return PokemonUtils.replace(CobbleUtils.language.getPokemonnameformat(), pokemon);
  }

  private static String getCommandTitle(String item) {
    String command = item.replace("command:", "");
    for (Map.Entry<String, ItemModel> entry : CobbleUtils.config.getItemsCommands().entrySet()) {
      if (command.startsWith(entry.getKey())) {
        return entry.getValue().getDisplayname();
      }
    }
    return command;
  }

  private static String getMoneyTitle(String item) {
    int money;
    if (item.split(":").length < 3) {
      money = Integer.parseInt(item.replace("money:", ""));
      ItemModel itemModel = new ItemModel(CobbleUtils.language.getItemMoney());
      return itemModel.getDisplayname().replace("%amount%", String.valueOf(money));
    } else {
      money = Integer.parseInt(item.split(":")[2]);
      String currency = item.split(":")[1];
      ItemModel impactorItem = new ItemModel(CobbleUtils.language.getItemsEconomy().getOrDefault(currency,
        new ItemModel(CobbleUtils.language.getItemMoney())));
      return impactorItem.getDisplayname().replace("%amount%", String.valueOf(money));
    }
  }

  private static String getItemTitle(String item) {
    ItemStack itemStack = parseItemStack(item, 1);
    return ItemUtils.getTranslatedName(itemStack);
  }

  private static List<String> getLore(ItemChance itemChance) {
    return CobbleUtils.language.getLorechance();
  }

  /**
   * Gets a random reward from a list of item chances and gives it to the player.
   *
   * @param itemChances The list of item chances to choose from.
   * @param player      The player to give the reward to.
   *
   * @throws IllegalArgumentException If the list of item chances is empty.
   */
  public static void getRandomReward(List<ItemChance> itemChances, ServerPlayerEntity player) {
    if (itemChances == null || itemChances.isEmpty()) {
      throw new IllegalArgumentException("The list of item chances cannot be empty");
    }

    int totalChance = itemChances.stream().mapToInt(ItemChance::getChance).sum();
    int randomChance = Utils.RANDOM.nextInt(totalChance);
    int cumulativeChance = 0;

    for (ItemChance itemChance : itemChances) {
      cumulativeChance += itemChance.getChance();
      if (randomChance < cumulativeChance) {
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
   * @param itemChances     The list of item chances to choose from.
   * @param player          The player to give the reward to.
   * @param numberOfRewards The number of rewards to give.
   *
   * @throws IllegalArgumentException If the list of item chances is empty or the
   *                                  number of rewards is less than or equal to
   *                                  zero.
   */
  public static void getRandomRewards(List<ItemChance> itemChances, ServerPlayerEntity player, int numberOfRewards) {
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
   * Gives all rewards from a list of item chances to the player.
   *
   * @param itemChances The list of item chances to choose from.
   * @param player      The player to give the rewards to.
   */
  public static void getAllRewards(List<ItemChance> itemChances, ServerPlayerEntity player) {
    for (ItemChance itemChance : itemChances) {
      try {
        giveReward(player, itemChance);
      } catch (NoPokemonStoreException e) {
        e.printStackTrace();
      }
    }
  }
}
