package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.template.TemplateType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 02/08/2024 9:25
 */
@Getter
@Setter
@Data
public class Shop {
  private String title;
  private String rows;
  private String currency;
  private TemplateType templateType;
  private List<SellItem> products;
  private List<FillItems> fillItems;

  @EqualsAndHashCode(callSuper = true)
  @Getter
  @Setter
  @Data
  public static class SellItem extends ItemModel {
    private ItemChance product;
    private int page;
    private int buy;
    private int sell;

    public SellItem() {
      super();
      product = new ItemChance();
      page = 0;
      buy = 0;
      sell = 0;
    }
  }

  public static class FillItems extends ItemModel {
    private List<Integer> slots;

    public FillItems() {
      super();
      slots = new ArrayList<>();
    }
  }

}
