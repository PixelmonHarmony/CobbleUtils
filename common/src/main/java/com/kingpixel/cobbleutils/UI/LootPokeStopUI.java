package com.kingpixel.cobbleutils.UI;

import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.Pokestop.PokeStopModel;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.CobbleUtilities;
import com.kingpixel.cobbleutils.util.Utils;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 15/06/2024 16:48
 */
public class LootPokeStopUI {

  public static GooeyPage open(String type) {
    ChestTemplate template = ChestTemplate.builder(6).build();
    List<Button> buttons = new ArrayList<>();


    LinkedPageButton previus = LinkedPageButton.builder()
      .display(Utils.parseItemId("minecraft:arrow"))
      .linkType(LinkType.Previous)
      .build();

    LinkedPageButton next = LinkedPageButton.builder()
      .display(Utils.parseItemId("minecraft:arrow"))
      .linkType(LinkType.Next)
      .build();

    GooeyButton close = GooeyButton.builder()
      .display(Utils.parseItemId("minecraft:arrow"))
      .build();

    PokeStopModel pokeStopModel = CobbleUtils.pokestops.getPokestop(type);
    if (pokeStopModel == null) {
      CobbleUtils.server.sendSystemMessage(AdventureTranslator.toNative("Invalid type."));
      return null;
    }
    GooeyButton info = GooeyButton.builder()
      .display(Utils.parseItemId("minecraft:book"))
      .title(AdventureTranslator.toNative(CobbleUtils.language.getInfolootpokestop().replace("%amount%",
        String.valueOf(pokeStopModel.getAmountrewards()))))
      .build();
    // Iterar a través del loot y crear botones para cada item
    pokeStopModel.getLoot().forEach(loot -> {
      // Calcular el porcentaje de chance en base al peso del loot
      double percentage = ((double) loot.getChance() / pokeStopModel.getTotalWeight()) * 100.0;

      // Crear el botón con el item y el lore que incluye el porcentaje de chance
      buttons.add(GooeyButton.builder()
        .display(Utils.parseItemId(loot.getItem()))
        .title(AdventureTranslator.toNative("&7" + CobbleUtilities.getNameItem(loot.getItem())))
        .lore(Component.class, AdventureTranslator.toNativeL(
          List.of(CobbleUtils.language.getLorelootpokestop().replace("%chance%", String.format("%.2f", percentage)))))
        .build());
    });

    PlaceholderButton placeholder = new PlaceholderButton();
    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder();

    template.fill(CobbleUtilities.fillItem());
    template.set(4, info);
    template.rectangle(1, 1, 4, 7, placeholder);
    String title = CobbleUtils.language.getTitlelootpokestop().replace("%type%", type);

    linkedPageBuilder.template(template).title(title);

    return PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
  }

}
