package com.kingpixel.cobbleutils.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 28/06/2024 20:02
 */
@Getter
@Setter
@ToString
public class ItemModel {
  private String item;
  private String displayname;
  private List<String> lore = new ArrayList<>();

  public ItemModel(String item, String displayname, List<String> lore) {
    this.item = item;
    this.displayname = displayname;
    this.lore = lore;
  }
}
