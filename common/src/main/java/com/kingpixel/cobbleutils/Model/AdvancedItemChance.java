package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Carlos Varas Alonso - 21/11/2024 3:15
 */
@Getter
public class AdvancedItemChance {
  private String title;
  private int amountReward;
  private boolean giveAll;
  private Map<String, List<ItemChance>> lootTable;

  public AdvancedItemChance() {
    this.title = "";
    this.amountReward = 3;
    this.giveAll = false;
    this.lootTable = Map.of(
      "", ItemChance.defaultItemChances(),
      "group.vip", List.of(
        new ItemChance()
      )
    );
  }

  public void giveRewards(ServerPlayerEntity player) {
    List<ItemChance> itemChances = getList(player);
    if (giveAll) {
      ItemChance.getAllRewards(itemChances, player);
    } else {
      ItemChance.getRandomRewards(itemChances, player, amountReward);
    }
  }

  public void openMenu(ServerPlayerEntity player) {
    ChestTemplate template = ChestTemplate.builder(6)
      .build();

    List<Button> buttons = getButtons(player);

    template.rectangle(0, 0, 5, 9, new PlaceholderButton());

    ItemModel itemPrevious = CobbleUtils.language.getItemPrevious();
    template.set(45, LinkedPageButton.builder()
      .display(itemPrevious.getItemStack())
      .linkType(LinkType.Previous)
      .build());

    ItemModel itemClose = CobbleUtils.language.getItemClose();
    template.set(49, itemClose.getButton(action -> {
      UIManager.closeUI(player);
    }));

    ItemModel itemNext = CobbleUtils.language.getItemNext();
    template.set(53, LinkedPageButton.builder()
      .display(itemNext.getItemStack())
      .linkType(LinkType.Next)
      .build());


    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder()
      .title(AdventureTranslator.toNative(title == null || title.isEmpty() ? CobbleUtils.language.getTitleLoot() : title));

    UIManager.openUIForcefully(player, PaginationHelper.createPagesFromPlaceholders(template, buttons,
      linkedPageBuilder));
  }


  public List<Button> getButtons(ServerPlayerEntity player) {
    List<Button> buttons = new ArrayList<>();
    double totalWeight = getList(null).stream().mapToDouble(ItemChance::getChance).sum();
    lootTable.forEach((key, value) -> {
      value.forEach(itemChance -> {
        boolean hasPermission = PermissionApi.hasPermission(player, key, 2);
        double chance = hasPermission ? itemChance.getChance() : 0;
        buttons.add(getButton(itemChance, key, chance, hasPermission, totalWeight));
      });
    });
    return buttons;
  }

  private GooeyButton getButton(ItemChance itemChance, String permission, double chance, boolean havePermission,
                                double totalWeight) {
    // Calcula el porcentaje basado en el peso total
    double percentage = totalWeight > 0 ? (chance / totalWeight) * 100 : 0;

    // Prepara el lore para mostrar el porcentaje calculado
    List<String> lore = new ArrayList<>(CobbleUtils.language.getLorechance());
    lore.replaceAll(s -> s.replace("%chance%", String.format("%.2f", percentage) + "%"));
    if (!havePermission) {
      lore.add(CobbleUtils.language.getMessagePermissionRewards()
        .replace("%permission%", permission));
    }

    return GooeyButton.builder()
      .display(itemChance.getItemStack())
      .lore(Text.class, AdventureTranslator.toNativeL(lore))
      .build();
  }


  private List<ItemChance> getList(ServerPlayerEntity player) {
    List<ItemChance> itemChances = new ArrayList<>();
    lootTable.forEach((key, value) -> {
      if (player != null && PermissionApi.hasPermission(player, key, 2)) {
        itemChances.addAll(value);
      } else {
        itemChances.addAll(value);
      }
    });
    return itemChances;
  }

}


