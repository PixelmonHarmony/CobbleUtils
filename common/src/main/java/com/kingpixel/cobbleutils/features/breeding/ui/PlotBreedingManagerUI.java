package com.kingpixel.cobbleutils.features.breeding.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.ButtonAction;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.features.breeding.Breeding;
import com.kingpixel.cobbleutils.features.breeding.models.PlotBreeding;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import com.kingpixel.cobbleutils.util.UIUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * @author Carlos Varas Alonso - 02/08/2024 14:29
 */
public class PlotBreedingManagerUI {
  public static void open(ServerPlayerEntity player, PlotBreeding plotBreeding) {
    int row = CobbleUtils.breedconfig.getRowmenuplot();
    ChestTemplate template = ChestTemplate.builder(row).build();

    Pokemon pokemonmale = plotBreeding.obtainMale();
    Pokemon pokemonfemale = plotBreeding.obtainFemale();

    GooeyButton male = createButton(pokemonmale, action -> {
      if (pokemonmale == null) {
        PlotSelectPokemonUI.selectPokemon(player, plotBreeding, Gender.MALE);
        return;
      }
      Cobblemon.INSTANCE.getStorage().getParty(player).add(pokemonmale);
      plotBreeding.setMale(null);
      Breeding.managerPlotEggs.writeInfo(player);
      open(player, plotBreeding);
    }, Gender.MALE);

    GooeyButton female = createButton(pokemonfemale, action -> {
      if (pokemonfemale == null) {
        PlotSelectPokemonUI.selectPokemon(player, plotBreeding, Gender.FEMALE);
        return;
      }
      Cobblemon.INSTANCE.getStorage().getParty(player).add(pokemonfemale);
      plotBreeding.setFemale(null);
      Breeding.managerPlotEggs.writeInfo(player);
      open(player, plotBreeding);
    }, Gender.FEMALE);

    Pokemon pokemonegg = null;

    if (!plotBreeding.getEggs().isEmpty()) {
      pokemonegg = Pokemon.Companion.loadFromJSON(plotBreeding.getEggs().get(0));
    } else {
      pokemonegg = PokemonProperties.Companion.parse("egg").create();
    }

    GooeyButton egg = GooeyButton.builder()
      .display(PokemonItem.from(pokemonegg,
        plotBreeding.getEggs().size()))
      .title("Eggs")
      .onClick(action -> {
        if (!plotBreeding.getEggs().isEmpty()) {
          plotBreeding.getEggs().forEach(pokemon -> {
            try {
              RewardsUtils.saveRewardPokemon(action.getPlayer(), Pokemon.Companion.loadFromJSON(pokemon));
            } catch (NoPokemonStoreException e) {
              throw new RuntimeException(e);
            }
          });
          plotBreeding.getEggs().clear();
          Breeding.managerPlotEggs.writeInfo(player);
          open(player, plotBreeding);
        }
      })
      .build();

    template.set(10, male);
    template.set(13, egg);
    template.set(16, female);
    template.set((row * 9) - 5, UIUtils.getCloseButton(action -> PlotBreedingUI.open(player)));

    GooeyPage page = GooeyPage.builder()
      .template(template)
      .title(AdventureTranslator.toNative(CobbleUtils.breedconfig.getTitleplot()))
      .build();

    UIManager.openUIForcefully(player, page);
  }

  private static GooeyButton createButton(Pokemon pokemon, Consumer<ButtonAction> action, Gender gender) {
    ItemModel itemModel = new ItemModel();
    if (gender == Gender.MALE) {
      itemModel = CobbleUtils.breedconfig.getMaleSelectItem();
    } else if (gender == Gender.FEMALE) {
      itemModel = CobbleUtils.breedconfig.getFemaleSelectItem();
    }
    return GooeyButton.builder()
      .display((pokemon != null ? PokemonItem.from(pokemon) : itemModel.getItemStack()))
      .title(AdventureTranslator
        .toNative((pokemon != null ? PokemonUtils.replace(pokemon) :
          CobbleUtils.language.getGender().getOrDefault(gender.getShowdownName(), gender.name()))))
      .lore(Text.class,
        AdventureTranslator.toNativeL((pokemon != null ? PokemonUtils.replaceLore(pokemon) : itemModel.getLore())))
      .onClick(action)
      .build();
  }
}
