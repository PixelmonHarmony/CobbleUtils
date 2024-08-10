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
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 28/06/2024 20:09
 */
public class ShinyTokenUI {
  public static GooeyPage openmenu(ServerPlayerEntity player) {
    try {
      PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUuid());

      ChestTemplate templateBuilder = ChestTemplate.builder(4).build();

      for (int i = 0; i < partyStore.size(); i++) {
        GooeyButton slot;
        Pokemon pokemon = partyStore.get(i);
        slot = UIUtils.createButtonPokemon(pokemon, action -> {
          try {
            if (pokemon == null)
              return;
            if (CobbleUtils.config.isShinyTokenBlacklisted(pokemon))
              return;
            if (!pokemon.getShiny()) {
              UIManager.openUIForcefully((ServerPlayerEntity) player, confirmShiny(player, pokemon));
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

      GooeyPage page = GooeyPage.builder().template(templateBuilder)
        .title(AdventureTranslator.toNative(CobbleUtils.language.getTitlemenushiny())).build();

      UIManager.openUIForcefully(player, page);
      return page;
    } catch (NoPokemonStoreException e) {
      player.sendMessage(
        AdventureTranslator.toNative("An error occurred while trying to access your Pokémon party."));
      e.printStackTrace();
    }
    return null;
  }

  public static GooeyPage confirmShiny(ServerPlayerEntity player, Pokemon pokemon) throws NoPokemonStoreException {
    GooeyButton confirm = UIUtils.getConfirmButton(action -> {
      pokemon.setShiny(true);
      player.getMainHandStack().decrement(1);
      UIManager.closeUI(player);
    });

    GooeyButton buttonPokemon = UIUtils.createButtonPokemon(pokemon, (action) -> {
    });

    GooeyButton cancel = UIUtils.getCancelButton(action -> openmenu(player));

    return GooeyPage.builder()
      .template(new ChestTemplate.Builder(3)
        .set(1, 2, confirm)
        .set(1, 6, cancel)
        .set(1, 4, buttonPokemon)
        .fill(GooeyButton.builder().display(Utils.parseItemId(CobbleUtils.config.getFill())).title("").build())
        .build())
      .title(AdventureTranslator.toNative(CobbleUtils.language.getTitlemenushinyoperation())).build();
  }

  public static boolean haveShinyToken(ServerPlayerEntity player) {
    return player.getInventory().contains(CobbleUtils.config.getShinytoken().getItemStack());
  }
}
