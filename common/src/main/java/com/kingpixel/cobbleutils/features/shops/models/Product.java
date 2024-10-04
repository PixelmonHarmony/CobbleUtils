package com.kingpixel.cobbleutils.features.shops.models;

import com.kingpixel.cobbleutils.Model.ItemChance;
import lombok.*;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 16/09/2024 18:43
 */
@Getter
@Setter
@EqualsAndHashCode
@Data
@ToString
public class Product {
  // Optional
  private String permission;
  private String color;
  private String display;
  private String displayname;
  private List<String> lore;
  private Long CustomModelData;
  private Integer discount;
  // Always have date
  private String product;
  private BigDecimal buy;
  private BigDecimal sell;

  public Product() {
    this.display = null;
    this.color = null;
    this.displayname = null;
    this.lore = null;
    this.CustomModelData = null;
    this.permission = null;
    this.discount = null;
    this.product = "minecraft:stone";
    this.buy = BigDecimal.valueOf(999999999);
    this.sell = BigDecimal.valueOf(25);
  }

  public Product(boolean optional) {
    if (optional) {
      this.display = "minecraft:dirt";
      this.color = "<#e7af76>";
      this.displayname = "Custom Dirt";
      this.lore = List.of("This is a custom dirt", "You can use it to build");
      this.CustomModelData = 1L;
      this.permission = "cobbleutils.dirt";
      this.discount = 10;
    } else {
      this.display = null;
      this.color = null;
      this.displayname = null;
      this.lore = null;
      this.CustomModelData = null;
      this.permission = null;
      this.discount = null;
    }
    this.product = "minecraft:stone";
    this.buy = BigDecimal.valueOf(999999999);
    this.sell = BigDecimal.valueOf(25);
  }

  public Product(String product, BigDecimal buy, BigDecimal sell) {
    this.display = null;
    this.color = null;
    this.displayname = null;
    this.lore = null;
    this.CustomModelData = null;
    this.permission = null;
    this.discount = null;
    this.product = product;
    this.buy = buy;
    this.sell = sell;
  }

  public Product(String product, BigDecimal buy, BigDecimal sell, String permission) {
    this.display = null;
    this.color = null;
    this.displayname = null;
    this.lore = null;
    this.CustomModelData = null;
    this.discount = null;
    this.permission = permission;
    this.product = product;
    this.buy = buy;
    this.sell = sell;
  }

  public Product(String product, BigDecimal buy, BigDecimal sell, String permission, String display, String displayname, List<String> lore, Long CustomModelData) {
    this.display = display;
    this.displayname = displayname;
    this.color = null;
    this.discount = null;
    this.lore = lore;
    this.CustomModelData = CustomModelData;
    this.permission = permission;
    this.product = product;
    this.buy = buy;
    this.sell = sell;
  }

  public ItemChance getItemchance() {
    return new ItemChance(product, 100);
  }

  public ItemStack getItemStack(int amount) {
    ItemStack itemStack = getItemchance().getItemStack();
    if (getDisplay() != null && !getDisplay().isEmpty()) {
      itemStack = new ItemChance(getDisplay(), 100).getItemStack();
    }
    itemStack.setCount(amount);
    if (getDisplayname() != null && !getDisplayname().isEmpty()) {
      itemStack.setCustomName(Text.literal(getDisplayname()));
    }
    if (getLore() != null && !getLore().isEmpty()) {

    }
    if (getCustomModelData() != null) {
      itemStack.getOrCreateNbt().putLong("CustomModelData", getCustomModelData());
    }
    return itemStack;
  }

  public ItemStack getItemStack() {
    return getItemStack(1);
  }
}
