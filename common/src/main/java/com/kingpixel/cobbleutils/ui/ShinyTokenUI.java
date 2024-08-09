package com.kingpixel.cobbleutils.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.UIUtils;
import com.kingpixel.cobbleutils.util.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * @author Carlos Varas Alonso - 28/06/2024 20:09
 */
public class ShinyTokenUI {
  public static GooeyPage openmenu(Player player) {
    try {
      PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUUID());

      ChestTemplate templateBuilder = ChestTemplate.builder(4).build();
      
      for (int i = 0; i < partyStore.size(); i++) {
        GooeyButton slot;
        Pokemon pokemon = partyStore.get(i);
        slot = UIUtils.createButtonPokemon(pokemon, action -> {
          try {
            if (pokemon == null) return;
            if (CobbleUtils.config.isShinyTokenBlacklisted(pokemon)) return;
            if (!pokemon.getShiny()) {
              UIManager.openUIForcefully((ServerPlayer) player, confirmShiny(player, pokemon));
            }
          } catch (NoPokemonStoreException e) {
            e.printStackTrace();
          }
        });
        int row = i / 3;
        int col = i % 3 + 3;
        templateBuilder.set(row + 1, col, slot);
      }
      templateBuilder.set(0, 4,
        CobbleUtils.language.getItemPc().getButton(action -> UIManager.openUIForcefully(action.getPlayer(),
          ShinyTokenPcUI.getMenuShinyTokenPc(action.getPlayer()))));


      GooeyButton fill = GooeyButton.builder()
        .display(Utils.parseItemId(CobbleUtils.config.getFill()))
        .title("")
        .build();

      templateBuilder.fill(fill);


      GooeyPage page =
        GooeyPage.builder().template(templateBuilder).title(AdventureTranslator.toNative(CobbleUtils.language.getTitlemenushiny())).build();

      UIManager.openUIForcefully((ServerPlayer) player, page);
      return page;
    } catch (NoPokemonStoreException e) {
      player.sendSystemMessage(AdventureTranslator.toNative("An error occurred while trying to access your Pokémon party."));
      e.printStackTrace();
    }
    return null;
  }

  public static GooeyPage confirmShiny(Player player, Pokemon pokemon) throws NoPokemonStoreException {
    GooeyButton confirm = GooeyButton.builder()
      .display(new ItemStack(Items.GREEN_STAINED_GLASS_PANE))
      .title(AdventureTranslator.toNative(CobbleUtils.language.getConfirm()))
      .onClick((action) -> {
        pokemon.setShiny(true);
        player.getItemInHand(action.getPlayer().getUsedItemHand()).shrink(1);
        UIManager.closeUI((ServerPlayer) player);
      })
      .build();

    GooeyButton buttonPokemon = UIUtils.createButtonPokemon(pokemon, (action) -> {
    });

    GooeyButton cancel = GooeyButton.builder()
      .display(new ItemStack(Items.RED_STAINED_GLASS_PANE))
      .title(AdventureTranslator.toNative(CobbleUtils.language.getCancel()))
      .onClick((action) -> {
        openmenu(player);
      })
      .build();

    GooeyPage page = GooeyPage.builder()
      .template(new ChestTemplate.Builder(3)
        .set(1, 2, confirm)
        .set(1, 6, cancel)
        .set(1, 4, buttonPokemon)
        .fill(GooeyButton.builder().display(new ItemStack(Items.GRAY_STAINED_GLASS_PANE).setHoverName(AdventureTranslator.toNative(""))).build()).build())
      .title(AdventureTranslator.toNative(CobbleUtils.language.getTitlemenushinyoperation())).build();

    return page;
  }

  public static boolean haveShinyToken(Player player) {
    return player.getInventory().contains(CobbleUtils.config.getShinytoken().getItemStack());
  }
}
