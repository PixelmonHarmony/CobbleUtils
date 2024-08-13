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
import net.minecraft.server.network.ServerPlayerEntity;

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
  private String id;
  private String title;
  private short slot;
  private short rows;
  private String currency;
  private TemplateType templateType;
  private ItemModel display;
  private List<Product> products;
  private List<FillItems> fillItems;

  public Shop() {
    active = true;
    id = "default";
    title = "Default";
    rows = 6;
    currency = "dollars";
    templateType = TemplateType.CHEST;
    products = new ArrayList<>();
    products.add(new Product());
    fillItems = new ArrayList<>();
    fillItems.add(new FillItems());
  }

  @Getter
  @ToString
  @Data
  public static class Product {
    private ItemChance product;
    private short slot;
    private short page;
    private long buy;
    private long sell;

    public Product() {
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
    private short page;
    private List<Integer> slots;

    public FillItems() {
      super("minecraft:gray_stained_glass_pane");
      page = 0;
      slots = new ArrayList<>();
    }
  }

  public void open(ServerPlayerEntity player) {
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
