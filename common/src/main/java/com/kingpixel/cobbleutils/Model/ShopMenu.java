package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 06/08/2024 11:10
 */
@Getter
@Setter
@ToString
@Data
public class ShopMenu {
  private String title;
  private int rows;
  private ItemModel filler;
  private List<Shop> shops;

  public ShopMenu() {
    this.title = "Test";
    this.rows = 6;
    this.filler = new ItemModel();
    this.shops = List.of(new Shop());
  }

  public ShopMenu(String title) {
    this.title = title;
    this.rows = 6;
    this.filler = new ItemModel();
    this.shops = List.of(new Shop());
  }

  public ShopMenu(String title, List<Shop> shops) {
    this.title = title;
    this.rows = 6;
    this.filler = new ItemModel();
    this.shops = shops;
  }

  public void open(ServerPlayerEntity player) {
    ChestTemplate chestTemplate = ChestTemplate
      .builder(this.rows)
      .build();

    shops.forEach(shop -> {
      ItemModel itemModelShop = shop.getDisplay();
      GooeyButton button = GooeyButton.builder()
        .display(itemModelShop.getItemStack())
        .title(AdventureTranslator.toNative(shop.getTitle()))
        .lore(Text.class, AdventureTranslator.toNativeL(itemModelShop.getLore()))
        .onClick(action -> {
          if (shop.isActive()) {
            shop.open(player);
          } else {
            player.sendMessage(Text.literal("Shop is not active"));
          }
        })
        .build();

      chestTemplate.set(shop.getSlot(), button);
    });

    GooeyPage page = GooeyPage
      .builder()
      .template(chestTemplate)
      .title(AdventureTranslator.toNative(this.title))
      .build();

    UIManager.openUIForcefully(player, page);
  }

}
