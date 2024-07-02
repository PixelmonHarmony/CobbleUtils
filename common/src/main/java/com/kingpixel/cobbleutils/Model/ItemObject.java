package com.kingpixel.cobbleutils.Model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 29/06/2024 0:54
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ItemObject {
  private UUID uuid;
  private String item;

  public ItemObject(UUID uuid, String item) {
    this.uuid = uuid;
    this.item = item;
  }

  public static ItemObject fromString(String string) {
    return new ItemObject(UUID.randomUUID(), string);
  }

}
