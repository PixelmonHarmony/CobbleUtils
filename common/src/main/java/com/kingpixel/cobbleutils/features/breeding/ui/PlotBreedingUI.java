package com.kingpixel.cobbleutils.features.breeding.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.Breeding;
import com.kingpixel.cobbleutils.features.breeding.models.PlotBreeding;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 02/08/2024 14:29
 */
public class PlotBreedingUI {
  public static void open(ServerPlayer player) {
    ChestTemplate template = ChestTemplate.builder(6).build();

    int size = CobbleUtils.breedconfig.getMaxplots();

    for (int i = 0; i < size; i++) {
      PlotBreeding plotBreeding = Breeding.managerPlotEggs.getEggs().get(player.getUUID()).get(i);
      List<String> lore = new ArrayList<>(CobbleUtils.breedconfig.getPlotItem().getLore());
      /*List<Pokemon> pokemons = new ArrayList<>();
      pokemons.add(Pokemon.Companion.loadFromJSON(plotBreeding.getMale()));
      pokemons.add(Pokemon.Companion.loadFromJSON(plotBreeding.getFemale()));
      lore.replaceAll(s -> PokemonUtils.replace(s, pokemons)
        .replace("%cooldown%", PlayerUtils.getCooldown(plotBreeding.getCooldown())));*/
      GooeyButton button = GooeyButton.builder()
        .display(CobbleUtils.breedconfig.getPlotItem().getItemStack())
        .title(AdventureTranslator.toNative(CobbleUtils.breedconfig.getPlotItem().getDisplayname()))
        .lore(Component.class, AdventureTranslator.toNativeL(lore))
        .onClick(action -> PlotBreedingManagerUI.open(player, plotBreeding))
        .build();
      template.set(i, button);
    }

    GooeyPage page = GooeyPage.builder()
      .template(template)
      .title("Breeding Manager")
      .build();

    UIManager.openUIForcefully(player, page);
  }
}
