package com.kingpixel.cobbleutils.util;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemObject;
import com.kingpixel.cobbleutils.Model.RewardsData;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing player rewards.
 *
 * @author Carlos Varas Alonso - 28/06/2024 10:16
 */
public class RewardsUtils {

  /**
   * Saves an item into the player's rewards inventory.
   *
   * @param player    ServerPlayerEntity to save the item for
   * @param itemStack Item to save
   *
   * @return
   */
  public static boolean saveRewardItemStack(ServerPlayerEntity player, ItemStack itemStack) {
    RewardsData rewardsData = CobbleUtils.rewardsManager.getRewardsData().computeIfAbsent(
      player.getUuid(),
      uuid -> {
        RewardsData newRewardsData = new RewardsData(player.getGameProfile().getName(), player.getUuid());
        newRewardsData.init();
        return newRewardsData;
      });
    saveItemToRewardsData(player, rewardsData, itemStack);

    rewardsData.writeInfo();
    return true;
  }

  /**
   * Saves a list of items into the player's rewards inventory.
   *
   * @param player     ServerPlayerEntity to save the items for
   * @param itemStacks List of items to save
   */
  public static void saveRewardItemStack(ServerPlayerEntity player, List<ItemStack> itemStacks) {
    RewardsData rewardsData = CobbleUtils.rewardsManager.getRewardsData().computeIfAbsent(
      player.getUuid(),
      uuid -> {
        RewardsData newRewardsData = new RewardsData(player.getGameProfile().getName(), player.getUuid());
        newRewardsData.init();
        return newRewardsData;
      });

    for (ItemStack itemStack : itemStacks) {
      saveItemToRewardsData(player, rewardsData, itemStack);
    }

    rewardsData.writeInfo();
  }

  /**
   * Helper method to save an item into the RewardsData.
   *
   * @param player
   * @param rewardsData RewardsData object to save the item to
   * @param itemStack   ItemStack to save
   */
  private static void saveItemToRewardsData(ServerPlayerEntity player, RewardsData rewardsData, ItemStack itemStack) {
    boolean added = player.giveItemStack(itemStack);
    if (added)
      return;

    if (!CobbleUtils.config.isRewards() || CobbleUtils.config.isDirectreward()) {
      CobbleUtils.server.execute(() -> player.dropItem(itemStack, true));
      return;
    }

    int remainingCount = itemStack.getCount();
    int maxStackSize = itemStack.getMaxCount();

    // Add to rewards data
    for (ItemObject item : rewardsData.getItems()) {
      if (remainingCount <= 0)
        break;

      try {
        ItemStack existingItemStack = ItemStack.fromNbt(NbtHelper.fromNbtProviderString(item.getItem()));
        if (existingItemStack.isOf(itemStack.getItem())) {
          int existingCount = existingItemStack.getCount();
          int spaceAvailable = maxStackSize - existingCount;

          if (spaceAvailable > 0) {
            int toAdd = Math.min(remainingCount, spaceAvailable);
            existingItemStack.setCount(existingCount + toAdd);
            item.setItem(existingItemStack.getNbt().toString());
            remainingCount -= toAdd;
          }
        }
      } catch (CommandSyntaxException e) {
        e.printStackTrace();
      }
    }

    while (remainingCount > 0) {
      int toAdd = Math.min(remainingCount, maxStackSize);
      ItemStack newItemStack = itemStack.copy();
      newItemStack.setCount(toAdd);
      rewardsData.getItems().add(ItemObject.fromString(newItemStack.getNbt().toString()));
      remainingCount -= toAdd;
    }
  }

  /**
   * Saves a Pokémon into the player's rewards inventory.
   *
   * @param player  ServerPlayerEntity to save the Pokémon for
   * @param pokemon Pokémon to save
   *
   * @return
   */
  public static boolean saveRewardPokemon(ServerPlayerEntity player, Pokemon pokemon) throws NoPokemonStoreException {
    if (pokemon == null)
      return false;
    if (Cobblemon.INSTANCE.getStorage().getParty(player.getUuid()).add(pokemon)) {
      return true;
    }
    RewardsData rewardsData = CobbleUtils.rewardsManager.getRewardsData().computeIfAbsent(
      player.getUuid(),
      uuid -> {
        RewardsData newRewardsData = new RewardsData(player.getGameProfile().getName(), player.getUuid());
        newRewardsData.init();
        return newRewardsData;
      });
    rewardsData.getPokemons().add(pokemon.saveToJSON(new JsonObject()));
    rewardsData.writeInfo();
    return true;
  }

  /**
   * Saves a list of Pokémon into the player's rewards inventory.
   *
   * @param player   ServerPlayerEntity to save the Pokémon for
   * @param pokemons List of Pokémon to save
   */
  public static void saveRewardPokemon(ServerPlayerEntity player, List<Pokemon> pokemons) {
    RewardsData rewardsData = CobbleUtils.rewardsManager.getRewardsData().computeIfAbsent(
      player.getUuid(),
      uuid -> {
        RewardsData newRewardsData = new RewardsData(player.getGameProfile().getName(), player.getUuid());
        newRewardsData.init();
        return newRewardsData;
      });
    for (Pokemon pokemon : pokemons) {
      if (pokemon == null)
        continue;
      rewardsData.getPokemons().add(pokemon.saveToJSON(new JsonObject()));
    }
    rewardsData.writeInfo();
  }

  /**
   * Saves a command into the player's rewards inventory.
   *
   * @param player  ServerPlayerEntity to save the command for
   * @param command Command to save
   *
   * @return
   */
  public static boolean saveRewardCommand(ServerPlayerEntity player, String command) {
    RewardsData rewardsData = CobbleUtils.rewardsManager.getRewardsData().computeIfAbsent(
      player.getUuid(),
      uuid -> {
        RewardsData newRewardsData = new RewardsData(player.getGameProfile().getName(), player.getUuid());
        newRewardsData.init();
        return newRewardsData;
      });
    rewardsData.getCommands().add(command.replace("%player%", player.getGameProfile().getName()));
    if (!CobbleUtils.config.isRewards() || CobbleUtils.config.isDirectreward()) {
      giveCommandRewards(rewardsData.getCommands());
    }
    rewardsData.writeInfo();
    return true;
  }

  /**
   * Saves a list of commands into the player's rewards inventory.
   *
   * @param player   ServerPlayerEntity to save the commands for
   * @param commands List of commands to save
   */
  public static void saveRewardCommand(ServerPlayerEntity player, List<String> commands) {
    RewardsData rewardsData = CobbleUtils.rewardsManager.getRewardsData().computeIfAbsent(
      player.getUuid(),
      uuid -> {
        RewardsData newRewardsData = new RewardsData(player.getGameProfile().getName(), player.getUuid());
        newRewardsData.init();
        return newRewardsData;
      });
    commands.replaceAll(command -> command.replace("%player%", player.getGameProfile().getName()));
    rewardsData.getCommands().addAll(commands);
    if (!CobbleUtils.config.isRewards() || CobbleUtils.config.isDirectreward()) {
      giveCommandRewards(rewardsData.getCommands());
    }
    rewardsData.writeInfo();
  }

  private static void giveCommandRewards(List<String> commands) {
    if (CobbleUtils.config.isDirectreward() || !CobbleUtils.config.isRewards()) {
      commands.forEach(c -> {
        try {
          CobbleUtilities.executeCommand(c);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      commands.clear();
    }
  }

  /**
   * Claims rewards for the player.
   *
   * @param player ServerPlayerEntity to claim rewards
   */
  public static void claimRewards(ServerPlayerEntity player) {
    RewardsData rewardsData = CobbleUtils.rewardsManager.getRewardsData().get(player.getUuid());
    if (rewardsData == null) {
      rewardsData = CobbleUtils.rewardsManager.getRewardsData().computeIfAbsent(
        player.getUuid(),
        uuid -> new RewardsData(player.getGameProfile().getName(), player.getUuid()));
      rewardsData.init();
    }
    if (rewardsData.getCommands().isEmpty() && rewardsData.getItems().isEmpty()
      && rewardsData.getPokemons().isEmpty()) {
      return;
    }
    PlayerPartyStore playerPartyStore = null;
    try {
      playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUuid());
    } catch (NoPokemonStoreException e) {
      throw new RuntimeException(e);
    }

    if (!rewardsData.getPokemons().isEmpty()) {
      PlayerPartyStore finalPlayerPartyStore = playerPartyStore;
      List<JsonObject> pokemonsToRemove = new ArrayList<>();
      rewardsData.getPokemons().forEach(pokemon -> {
        try {
          Pokemon pokemon1 = Pokemon.Companion.loadFromJSON(pokemon);
          if (finalPlayerPartyStore.add(pokemon1)) {
            pokemonsToRemove.add(pokemon);
          }
        } catch (Exception e) {
          CobbleUtils.LOGGER.info(e.getMessage());
          e.printStackTrace();
        }
      });
      rewardsData.getPokemons().removeAll(pokemonsToRemove);
    }

    if (!rewardsData.getItems().isEmpty()) {
      List<ItemObject> itemsToRemove = new ArrayList<>();
      rewardsData.getItems().forEach(item -> {
        ItemStack itemStack = CobbleUtilities.getItem(item.getItem());
        try {
          if (player.getInventory().getEmptySlot() == -1)
            return;
          boolean success = player.getInventory().insertStack(itemStack);
          if (success) {
            itemsToRemove.add(item);
          }
        } catch (Exception e) {
          CobbleUtils.LOGGER.info(e.getMessage());
          e.printStackTrace();
        }
      });
      rewardsData.getItems().removeAll(itemsToRemove);
    }

    if (!rewardsData.getCommands().isEmpty()) {
      List<String> commandsToRemove = new ArrayList<>();
      rewardsData.getCommands().forEach(command -> {
        try {
          if (CobbleUtilities.executeCommand(command)) {
            commandsToRemove.add(command);
          }
        } catch (Exception e) {
          CobbleUtils.LOGGER.info(e.getMessage());
          e.printStackTrace();
        }
      });
      rewardsData.getCommands().removeAll(commandsToRemove);
    }

    rewardsData.writeInfo();
  }

  /**
   * Checks if the player has pending rewards.
   *
   * @param player ServerPlayerEntity to check
   *
   * @return True if the player has pending rewards, false otherwise
   */
  public static boolean hasRewards(ServerPlayerEntity player) {
    if (player == null)
      return false;

    RewardsData rewardsData = CobbleUtils.rewardsManager.getRewardsData().computeIfAbsent(
      player.getUuid(),
      uuid -> {
        RewardsData newRewardsData = new RewardsData(player.getGameProfile().getName(), player.getUuid());
        newRewardsData.init();
        return newRewardsData;
      });

    return !rewardsData.getCommands().isEmpty() ||
      !rewardsData.getItems().isEmpty() ||
      !rewardsData.getPokemons().isEmpty();
  }

  public static void removeRewards(ServerPlayerEntity player) {
    RewardsData rewardsData = CobbleUtils.rewardsManager.getRewardsData().get(player.getUuid());
    if (rewardsData == null)
      return;
    rewardsData.getItems().clear();
    rewardsData.getPokemons().clear();
    rewardsData.getCommands().clear();
    rewardsData.writeInfo();
  }
}
