package com.kingpixel.cobbleutils.features.breeding.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.Breeding;
import com.kingpixel.cobbleutils.features.breeding.models.PlotBreeding;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 02/08/2024 14:29
 */
public class PlotBreedingUI {
  public static void open(ServerPlayerEntity player) {
    ChestTemplate template = ChestTemplate.builder(CobbleUtils.breedconfig.getRowmenuselectplot()).build();

    int size = CobbleUtils.breedconfig.getMaxplots();

    for (int i = 0; i < size; i++) {
      PlotBreeding plotBreeding = Breeding.managerPlotEggs.getEggs().get(player.getUuid()).get(i);
      List<String> lore = new ArrayList<>(CobbleUtils.breedconfig.getPlotItem().getLore());
      int amount = plotBreeding.getEggs().size();
      List<Pokemon> pokemons = new ArrayList<>();
      pokemons.add(plotBreeding.getMale() != null ? Pokemon.Companion.loadFromJSON(plotBreeding.getMale()) : null);
      pokemons.add(plotBreeding.getFemale() != null ? Pokemon.Companion.loadFromJSON(plotBreeding.getFemale()) : null);
      lore.replaceAll(s -> PokemonUtils.replace(s, pokemons)
        .replace("%cooldown%", PlayerUtils.getCooldown(new Date(plotBreeding.getCooldown())))
        .replace("%eggs%", String.valueOf(amount)));
      ItemStack itemStack;
      if (plotBreeding.getEggs().isEmpty()) {
        itemStack = CobbleUtils.breedconfig.getPlotItem().getItemStack();
      } else {
        itemStack = CobbleUtils.breedconfig.getPlotThereAreEggs().getItemStack(amount);
      }
      GooeyButton button = GooeyButton.builder()
        .display(itemStack)
        .title(AdventureTranslator.toNative(CobbleUtils.breedconfig.getPlotItem().getDisplayname()))
        .lore(Text.class, AdventureTranslator.toNativeL(lore))
        .onClick(action -> {
          plotBreeding.checking(player);
          PlotBreedingManagerUI.open(player, plotBreeding);
        })
        .build();
      template.set(CobbleUtils.breedconfig.getPlotSlots().get(i), button);
    }

    GooeyPage page = GooeyPage.builder()
      .template(template)
      .title(AdventureTranslator.toNative(CobbleUtils.breedconfig.getTitleselectplot()))
      .build();

    UIManager.openUIForcefully(player, page);
  }
}
