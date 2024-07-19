package com.kingpixel.cobbleutils.util;

import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * @author Carlos Varas Alonso - 10/06/2024 21:09
 */
public class CobbleUtilities {

  public static String getNameItem(String id) {
    ItemStack itemStack = Utils.parseItemId(id);
    Item item = itemStack.getItem();
    return item.getName(itemStack).getString();
  }

  /**
   * Check if a player is in a battle
   *
   * @param player The player to check
   *
   * @return If the player is in a battle
   */
  public static boolean isBattle(Player player) {
    PokemonBattle battle;
    try {
      battle =
        Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer((ServerPlayer) player);
    } catch (Exception e) {
      player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.language.getMessagearebattle()));
      return false;
    }
    assert battle != null;
    boolean pvn = battle.isPvN();
    boolean pvp = battle.isPvP();
    boolean pvw = battle.isPvW();
    return pvn || pvp || pvw;
  }

  /**
   * Give a random item to a player
   *
   * @param player The player to give the item
   * @param type   The type of item to give
   * @param amount The amount of items to give
   */
  public static void giveRandomItem(Player player, String type, int amount) {
    String item = CobbleUtils.poolItems.getRandomItem(type);
    if (item == null) {
      player.sendSystemMessage(AdventureTranslator.toNative("Invalid type."));
      return;
    }
    ItemStack itemStack = Utils.parseItemId(item, amount);
    CobbleUtils.server.execute(() -> {
      if (!player.getInventory().add(itemStack)) {
        player.drop(itemStack, true);
      }
    });
    String message = CobbleUtils.language.getMessagerandomitem()
      .replace("%item%", getNameItem(item))
      .replace("%amount%", String.valueOf(amount))
      .replace("%type%", type);

    player.sendSystemMessage(AdventureTranslator.toNative(message));
  }

  /**
   * Give a random pokemon to a player
   *
   * @param player The player to give the pokemon
   * @param type   The type of pokemon to give
   *
   * @throws NoPokemonStoreException If the pokemon store is empty
   */
  public static void giveRandomPokemon(Player player, String type) throws NoPokemonStoreException {
    String pokemonName = CobbleUtils.poolPokemons.getRandomPokemon(type);
    if (pokemonName == null) {
      player.sendSystemMessage(AdventureTranslator.toNative("Invalid type."));
      return;
    }
    Pokemon pokemon = Utils.createPokemonParse(pokemonName);
    Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).add(pokemon);
  }

  /**
   * Convert seconds to time
   *
   * @param totalSeconds The total seconds
   *
   * @return The time in a string
   */
  public static String convertSecondsToTime(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    if (minutes > 0) {
      return CobbleUtils.language.getMinutes().replace("%m%", String.valueOf(minutes)) + CobbleUtils.language.getSeconds().replace("%s%", String.valueOf(seconds));
    } else {
      return CobbleUtils.language.getSeconds().replace("%s%", String.valueOf(seconds));
    }
  }

  public static GooeyButton fillItem() {
    return GooeyButton.builder().display(Utils.parseItemId(CobbleUtils.config.getFill())).build();
  }

  /**
   * Give a random amount of money to a player
   *
   * @param player The player to give the money
   * @param type   The type of money to give
   *
   * @return If the money was given successfully
   */
  public static boolean giveRandomMoney(Player player, String type) {
    int money = CobbleUtils.poolMoney.getRandomMoney(type);
    if (money == 0) {
      player.sendSystemMessage(AdventureTranslator.toNative("Invalid type."));
      return false;
    }
    String comando = CobbleUtils.config.getEcocommand().replace("%amount%", String.valueOf(money))
      .replace("%player%", player.getGameProfile().getName());
    return executeCommand(comando);
  }


  /**
   * Execute a command
   *
   * @param command The command to execute
   *
   * @return If the command was executed successfully
   */
  public static boolean executeCommand(String command) {
    CommandDispatcher<CommandSourceStack> disparador = CobbleUtils.server.getCommands().getDispatcher();
    try {
      CommandSourceStack serverSource = CobbleUtils.server.createCommandSourceStack();
      ParseResults<CommandSourceStack> parse = disparador.parse(command, serverSource);
      disparador.execute(parse);
      return true;
    } catch (CommandSyntaxException e) {
      System.err.println("Error to execute command: " + command);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Get an ItemStack from a string with NBT
   *
   * @param item The item with NBT
   *
   * @return The ItemStack
   */
  public static ItemStack getItem(String item) {
    try {
      return ItemStack.of(TagParser.parseTag(item));
    } catch (CommandSyntaxException e) {
      CobbleUtils.LOGGER.fatal("Failed to parse item for NBT: " + item);
      CobbleUtils.LOGGER.fatal("Stacktrace: ");
      e.printStackTrace();
    }
    return null;
  }
}
