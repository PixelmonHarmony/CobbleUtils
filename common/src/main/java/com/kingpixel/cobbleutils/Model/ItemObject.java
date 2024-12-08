package com.kingpixel.cobbleutils.Model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * @author Carlos Varas Alonso - 29/06/2024 0:54
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ItemObject {
  private String item;

  public ItemObject(String item) {
    this.item = item;
  }

  // Crea un ItemObject a partir de un ItemStack, guardando el NBT como String
  public static String fromItemStack(ItemStack itemStack) {
    var jsonObject = new JsonObject();
    jsonObject.addProperty("itemId", Registries.ITEM.getId(itemStack.getItem()).toString());
    jsonObject.addProperty("amount", itemStack.getCount());
    if (itemStack.hasCustomName()) {
      jsonObject.addProperty("displayName", itemStack.getName().getString());
    }
    // Save NBT data
    if (itemStack.hasNbt()) {
      jsonObject.addProperty("nbt", itemStack.getNbt().toString());
    }
    if (itemStack.hasNbt() && itemStack.getNbt().contains("display") && itemStack.getNbt().getCompound("display").contains("Lore")) {
      var loreJsonArray = new JsonArray();
      var loreNbtList = itemStack.getNbt().getCompound("display").getList("Lore", 8);
      for (int i = 0; i < loreNbtList.size(); i++) {
        loreJsonArray.add(loreNbtList.getString(i));
      }
      jsonObject.add("lore", loreJsonArray);
    }
    return jsonObject.toString();
  }

  public ItemStack toItemStack() {
    return toItemStack(item);
  }

  // Crea un ItemStack a partir de un String
  public static ItemStack toItemStack(String itemString) {
    try {
      var jsonObject = JsonParser.parseString(itemString).getAsJsonObject();
      var itemId = jsonObject.get("itemId").getAsString();
      var amount = jsonObject.get("amount").getAsInt();
      var item = Registries.ITEM.get(new Identifier(itemId));
      var itemStack = new ItemStack(item, amount);

      if (jsonObject.has("displayName")) {
        itemStack.setCustomName(Text.literal(jsonObject.get("displayName").getAsString()));
      }
      if (jsonObject.has("nbt")) {
        itemStack.setNbt(NbtHelper.fromNbtProviderString(jsonObject.get("nbt").getAsString()));
      }
      if (jsonObject.has("lore")) {
        var loreJsonArray = jsonObject.getAsJsonArray("lore");
        var loreNbtList = new NbtList();
        for (var element : loreJsonArray) {
          loreNbtList.add(NbtString.of(element.getAsString()));
        }
        itemStack.getOrCreateSubNbt("display").put("Lore", loreNbtList);
      }
      return itemStack;
    } catch (Exception e) {
      e.printStackTrace();
      return ItemStack.EMPTY;
    }
  }

  public static ItemObject createItemObject(ItemStack newItemStack) {
    return new ItemObject(fromItemStack(newItemStack));
  }
}