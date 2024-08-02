package com.kingpixel.cobbleutils.features.breeding.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.ButtonAction;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.Breeding;
import com.kingpixel.cobbleutils.features.breeding.models.PlotBreeding;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Carlos Varas Alonso - 02/08/2024 14:29
 */
public class PlotBreedingManagerUI {
  public static void open(ServerPlayer player, PlotBreeding plotBreeding) {
    ChestTemplate template = ChestTemplate.builder(6).build();

    Pokemon pokemonmale = plotBreeding.obtainMale();
    Pokemon pokemonfemale = plotBreeding.obtainFemale();

    GooeyButton male = createButton(pokemonmale, action -> {
      if (pokemonmale == null) {
        Pokemon selected = PokemonProperties.Companion.parse("ditto").create();
        Cobblemon.INSTANCE.getStorage().getParty(player).remove(selected);
        plotBreeding.setMale(selected.saveToJSON(new JsonObject()));
        Breeding.managerPlotEggs.writeInfo(player);
        return;
      }
      Cobblemon.INSTANCE.getStorage().getParty(player).add(pokemonmale);
      plotBreeding.setMale(null);
      Breeding.managerPlotEggs.writeInfo(player);
      open(player, plotBreeding);
    });

    GooeyButton female = createButton(pokemonfemale, action -> {
      if (pokemonfemale == null) {
        Pokemon selected = PokemonProperties.Companion.parse("ditto").create();
        Cobblemon.INSTANCE.getStorage().getParty(player).remove(selected);
        plotBreeding.setFemale(selected.saveToJSON(new JsonObject()));
        Breeding.managerPlotEggs.writeInfo(player);
        return;
      }
      Cobblemon.INSTANCE.getStorage().getParty(player).add(pokemonfemale);
      plotBreeding.setFemale(null);
      Breeding.managerPlotEggs.writeInfo(player);
      open(player, plotBreeding);
    });

    template.set(0, female);
    template.set(1, male);


    GooeyPage page = GooeyPage.builder()
      .template(template)
      .title("Breeding Manager")
      .build();

    UIManager.openUIForcefully(player, page);
  }

  private static GooeyButton createButton(Pokemon pokemon, Consumer<ButtonAction> action) {
    return GooeyButton.builder()
      .display((pokemon != null ? PokemonItem.from(pokemon) : Items.BARRIER.getDefaultInstance()))
      .title(AdventureTranslator.toNative((pokemon != null ? PokemonUtils.getTranslatedName(pokemon) : CobbleUtils.language.getEmpty())))
      .lore(Component.class, AdventureTranslator.toNativeL(List.of("")))
      .onClick(action)
      .build();
  }
}
