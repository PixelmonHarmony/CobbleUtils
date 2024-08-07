package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.TemplateType;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 02/08/2024 9:25
 */
@Getter
@ToString
@Data
public class Shop {
  private boolean active;
  private String title;
  private int slot;
  private int rows;
  private String currency;
  private TemplateType templateType;
  private ItemModel display;
  private List<SellItem> products;
  private List<FillItems> fillItems;

  public Shop() {
    active = true;
    title = "Shop";
    rows = 6;
    currency = "tokens";
    templateType = TemplateType.CHEST;
    products = new ArrayList<>();
    fillItems = new ArrayList<>();
  }

  @Getter
  @ToString
  @Data
  public static class SellItem {
    private ItemChance product;
    private int slot;
    private int page;
    private int buy;
    private int sell;

    public SellItem() {
      product = new ItemChance();
      page = 0;
      buy = 0;
      sell = 0;
    }
  }

  @EqualsAndHashCode(callSuper = true)
  @Getter
  @ToString
  @Data
  public static class FillItems extends ItemModel {
    private int page;
    private List<Integer> slots;

    public FillItems() {
      super();
      page = 0;
      slots = new ArrayList<>();
    }
  }

  public void open(ServerPlayer player) {
    ChestTemplate chestTemplate = ChestTemplate
      .builder(this.rows)
      .build();

    GooeyPage page = GooeyPage
      .builder()
      .template(chestTemplate)
      .title(AdventureTranslator.toNative(this.title))
      .build();

    UIManager.openUIForcefully(player, page);
  }
}
