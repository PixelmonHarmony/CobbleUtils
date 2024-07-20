package com.kingpixel.cobbleutils.Model.options;

import com.kingpixel.cobbleutils.Model.ItemModel;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 20/07/2024 11:19
 */
@Getter
@ToString
public class ImpactorItem {
  private final String message;
  private final ItemModel item;


  public ImpactorItem() {
    this.message = "&aYou have received &6%amount% &dTokens&a!";
    this.item = new ItemModel("minecraft:nether_star", "%amount% &dTokens", List.of(), 1);
  }

  public ImpactorItem(String message, ItemModel item) {
    this.message = message;
    this.item = item;
  }
}
