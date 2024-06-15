package com.kingpixel.cobbleutils.util;

import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
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
    return getNameItemStack(itemStack);
  }

  public static String getNameItemStack(ItemStack itemStack) {
    Item item = itemStack.getItem();
    return item.getName(itemStack).getString();
  }

  public static void unimplemented(Player player) {
    player.sendSystemMessage(AdventureTranslator.toNative("Unimplemented feature."));
  }

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

  public static void giveRandomPokemon(Player player, String type) throws NoPokemonStoreException {
    String pokemonName = CobbleUtils.poolPokemons.getRandomPokemon(type);
    if (pokemonName == null) {
      player.sendSystemMessage(AdventureTranslator.toNative("Invalid type."));
      return;
    }
    Pokemon pokemon = Utils.createPokemonParse(pokemonName);
    Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).add(pokemon);
  }

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
}
