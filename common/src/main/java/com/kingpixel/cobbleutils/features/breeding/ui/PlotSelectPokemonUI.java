package com.kingpixel.cobbleutils.features.breeding.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.Breeding;
import com.kingpixel.cobbleutils.features.breeding.models.PlotBreeding;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.UIUtils;
import com.kingpixel.cobbleutils.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.BREEDABLE_TAG;

/**
 * @author Carlos Varas Alonso - 02/08/2024 18:54
 */
public class PlotSelectPokemonUI {

  public static void selectPokemon(ServerPlayer player, PlotBreeding plotBreeding, Gender gender) {
    int row = CobbleUtils.breedconfig.getRowmenuselectpokemon();
    ChestTemplate template = ChestTemplate.builder(row).build();

    List<Pokemon> pokemons = new ArrayList<>();
    try {
      Cobblemon.INSTANCE.getStorage().getParty(player).forEach(pokemon -> {
        if (pokemon.getPersistentData().getBoolean(BREEDABLE_TAG)) return;
        if (isBlacklist(player, pokemon, plotBreeding, gender)) return;
        if (pokemon.getGender() == gender || pokemon.getGender() == Gender.GENDERLESS) {
          pokemons.add(pokemon);
        }
      });
      Cobblemon.INSTANCE.getStorage().getPC(player.getUUID()).forEach(pokemon -> {
        if (pokemon.getPersistentData().getBoolean(BREEDABLE_TAG)) return;
        if (isBlacklist(player, pokemon, plotBreeding, gender)) return;
        if (pokemon.getGender() == gender || pokemon.getGender() == Gender.GENDERLESS) {

          pokemons.add(pokemon);
        }
      });
    } catch (NoPokemonStoreException e) {
      throw new RuntimeException(e);
    }

    List<Button> buttons = new ArrayList<>();
    int size = pokemons.size();

    for (int i = 0; i < size; i++) {
      Pokemon pokemon = pokemons.get(i);
      GooeyButton button = GooeyButton.builder()
        .display(PokemonItem.from(pokemon))
        .title(AdventureTranslator.toNative(PokemonUtils.replace(pokemon)))
        .lore(Component.class, AdventureTranslator.toNativeL(PokemonUtils.replaceLore(pokemon)))
        .onClick(action -> {
          if (pokemon.isUltraBeast() || pokemon.isLegendary()) return;
          try {
            Cobblemon.INSTANCE.getStorage().getPC(player.getUUID()).remove(pokemon);
            Cobblemon.INSTANCE.getStorage().getParty(player).remove(pokemon);
          } catch (NoPokemonStoreException e) {
            throw new RuntimeException(e);
          }
          plotBreeding.add(pokemon, gender);
          Breeding.managerPlotEggs.writeInfo(player);
          PlotBreedingManagerUI.open(player, plotBreeding);
        })
        .build();
      buttons.add(button);
    }
    LinkedPageButton back = UIUtils.getPreviousButton(action -> {

    });

    LinkedPageButton next = UIUtils.getNextButton(action -> {

    });

    GooeyButton close = UIUtils.getCloseButton(action -> {
      PlotBreedingManagerUI.open(player, plotBreeding);
    });

    template.set(row - 1, 0, back);
    template.set(row - 1, 4, close);
    template.set(row - 1, 8, next);
    template.fill(GooeyButton.builder()
      .display(Utils.parseItemId(CobbleUtils.config.getFill()))
      .title("")
      .build());
    template.rectangle(0, 0, row - 1, 9, new PlaceholderButton());
    template.fillFromList(buttons);

    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder().title(AdventureTranslator.toNative("Select a Pokemon"));
    GooeyPage page = PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
    UIManager.openUIForcefully(player, page);
  }

  private static boolean isBlacklist(ServerPlayer player, Pokemon pokemon, PlotBreeding plotBreeding, Gender gender) {
    if (pokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) return true;
    if (CobbleUtils.breedconfig.getBlacklist().contains(pokemon.getSpecies().showdownId())) {
      /*player.sendSystemMessage(
        AdventureBreeding.adventure(
          PokemonUtils.replace(
            CobbleUtils.breedconfig.getBlacklisted()
            , pokemon
          )
        )
      );*/
      return true;
    }
    if (!CobbleUtils.breedconfig.isDoubleditto()) {
      /*player.sendSystemMessage(
        AdventureBreeding.adventure(
          PokemonUtils.replace(
            CobbleUtils.breedconfig.getNotdoubleditto()
            , pokemon
          )
        )
      );*/
      return true;
    }
    Pokemon isDitto = plotBreeding.obtainOtherGender(gender);
    if (isDitto != null && isDitto.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      return !CobbleUtils.breedconfig.isDitto();
    }
    return false;
  }
}
