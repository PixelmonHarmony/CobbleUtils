package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.google.gson.Gson;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 06/08/2024 11:10
 */
@Getter
@Setter
@ToString
@Data
public class ShopMenu {
  private String title;
  private int rows;
  private ItemModel filler;
  private List<Shop> shops;

  public ShopMenu() {
    this.title = "Test";
    this.rows = 6;
    this.filler = new ItemModel();
    this.shops = List.of(new Shop());
  }

  public ShopMenu(String title) {
    this.title = title;
    this.rows = 6;
    this.filler = new ItemModel();
    this.shops = List.of(new Shop());
  }

  public ShopMenu(String title, List<Shop> shops) {
    this.title = title;
    this.rows = 6;
    this.filler = new ItemModel();
    this.shops = shops;
  }

  public static List<Shop> getShops(String path) {
    List<Shop> shops = new ArrayList<>();
    File folder = Utils.getAbsolutePath(path);

    if (folder.exists() && folder.isDirectory()) {
      File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

      if (files != null) {
        Gson gson = Utils.newGson();

        for (File file : files) {
          try (FileReader reader = new FileReader(file)) {
            Shop shop = gson.fromJson(reader, Shop.class);
            shops.add(shop);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }

    return shops;
  }

  public static void createDefaultShop(String path) {
    Shop shop = new Shop();
    Utils.writeFileAsync(path, "default.json", Utils.newGson().toJson(shop)).join();
  }


  public void open(ServerPlayerEntity player) {
    ChestTemplate chestTemplate = ChestTemplate
      .builder(this.rows)
      .build();

    shops.forEach(shop -> {
      ItemModel itemModelShop = shop.getDisplay();
      GooeyButton button = GooeyButton.builder()
        .display(itemModelShop.getItemStack())
        .title(AdventureTranslator.toNative(shop.getTitle()))
        .lore(Text.class, AdventureTranslator.toNativeL(itemModelShop.getLore()))
        .onClick(action -> {
          if (shop.isActive()) {
            shop.open(player);
          } else {
            player.sendMessage(Text.literal("Shop is not active"));
          }
        })
        .build();

      chestTemplate.set(shop.getSlot(), button);
    });

    GooeyPage page = GooeyPage
      .builder()
      .template(chestTemplate)
      .title(AdventureTranslator.toNative(this.title))
      .build();

    UIManager.openUIForcefully(player, page);
  }

}
