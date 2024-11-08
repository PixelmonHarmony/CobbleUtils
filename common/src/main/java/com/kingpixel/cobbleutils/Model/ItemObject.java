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

  // Crea un ItemObject a partir de un String (sin convertirlo a ItemStack)
  public static ItemObject fromString(String string) {
    return new ItemObject(UUID.randomUUID(), string);
  }

  // Crea un ItemObject a partir de un ItemStack, guardando el NBT como String
  public static ItemObject fromItemStack(ItemStack itemStack) {
    return new ItemObject(UUID.randomUUID(), NbtHelper.toFormattedString(itemStack.getNbt(), true));
  }

  // Convierte un ItemStack desde un String (deserializa el NBT)
  public static ItemStack fromItemString(String itemString) {
    try {
      return ItemStack.fromNbt(NbtHelper.fromNbtProviderString(itemString));
    } catch (Exception e) {
      e.printStackTrace();
      return ItemStack.EMPTY; // Regresa un ItemStack vacío en caso de error
    }
  }
}
