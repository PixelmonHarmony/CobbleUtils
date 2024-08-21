package com.kingpixel.cobbleutils.Model.shops;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemChance;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.SoundUtil;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
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
  private String soundopen;
  private String soundclose;
  private int rows;
  private ItemModel fill;
  private List<Shop> shops;

  public ShopMenu() {
    this.title = "Test";
    this.rows = 6;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.fill = new ItemModel("minecraft:gray_stained_glass_pane");
    this.shops = defaultShops();
  }

  public ShopMenu(String title) {
    this.title = title;
    this.rows = 6;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.fill = new ItemModel("minecraft:gray_stained_glass_pane");
    this.shops = List.of(new Shop());
  }

  public ShopMenu(String title, List<Shop> shops) {
    this.title = title;
    this.rows = 6;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.fill = new ItemModel("minecraft:gray_stained_glass_pane");
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
    ShopMenu shopMenu = new ShopMenu();
    shopMenu.getShops().forEach(shop -> {
      Utils.writeFileAsync(path, shop.getId() + ".json", Utils.newGson().toJson(shop)).join();
    });
  }


  public void open(ServerPlayerEntity player) {
    try {


      ChestTemplate template = ChestTemplate
        .builder(this.rows)
        .build();

      shops.forEach(shop -> {
        ItemModel itemModelShop = shop.getDisplay();
        GooeyButton button = GooeyButton.builder()
          .display(itemModelShop.getItemStack())
          .title(AdventureTranslator.toNative(itemModelShop.getDisplayname()))
          .lore(Text.class, AdventureTranslator.toNativeL(itemModelShop.getLore()))
          .onClick(action -> {
            if (shop.isActive()) {
              shop.open(player, this);
            } else {
              player.sendMessage(Text.literal("Shop is not active"));
              SoundUtil.playSound(CobbleUtils.shopLang.getSoundError(), player);
            }
          })
          .build();

        template.set(itemModelShop.getSlot(), button);
      });

      template.fill(GooeyButton.of(fill.getItemStack()));

      GooeyPage page = GooeyPage
        .builder()
        .template(template)
        .title(AdventureTranslator.toNative(this.title))
        .onClose(pageAction -> {
          SoundUtil.playSound(getSoundclose(), player);
        })
        .build();

      UIManager.openUIForcefully(player, page);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void open(ServerPlayerEntity player, String shop) {
    Shop shop1 = shops.stream().filter(s -> s.getId().equals(shop)).findFirst().orElse(null);
    if (shop1 != null) {
      shop1.open(player, this);
    } else {
      player.sendMessage(Text.literal("Shop not found"));
    }
  }

  private List<Shop> defaultShops() {
    shops = new ArrayList<>();
    // PokeBalls
    shops.add(pokeBalls());
    return shops;
  }

  // PokeBalls
  private Shop pokeBalls() {
    Shop shop = new Shop("pokeballs", "<#de504b>Pokeballs", (short) 6, "impactor:dollars", new ItemModel("cobblemon" +
      ":poke_ball"));
    List<Shop.Product> products = new ArrayList<>();
    products.add(new Shop.Product(new ItemChance("cobblemon:poke_ball", 1), BigDecimal.valueOf(100), BigDecimal.ZERO));
    shop.setProducts(products);
    return shop;
  }
}
