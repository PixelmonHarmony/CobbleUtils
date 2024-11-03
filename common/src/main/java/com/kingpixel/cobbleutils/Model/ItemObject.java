package com.kingpixel.cobbleutils.Model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;

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

  public static ItemObject fromItemStack(ItemStack itemStack) {
    return new ItemObject(UUID.randomUUID(), NbtHelper.toFormattedString(itemStack.getNbt(), true));
  }

}
