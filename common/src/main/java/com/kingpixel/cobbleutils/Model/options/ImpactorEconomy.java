package com.kingpixel.cobbleutils.Model.options;

import lombok.Getter;
import lombok.ToString;

import java.util.Map;

/**
 * @author Carlos Varas Alonso - 20/07/2024 11:13
 */
@Getter
@ToString
public class ImpactorEconomy {
  private String ecocommand;
  private Map<String, ImpactorItem> itemsCommands;

  public ImpactorEconomy() {
    this.ecocommand = "eco deposit %amount% %currency% %player%";
    this.itemsCommands = Map.of("tokens", new ImpactorItem());
  }
}
