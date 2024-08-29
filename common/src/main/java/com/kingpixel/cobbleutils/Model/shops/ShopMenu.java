package com.kingpixel.cobbleutils.Model.shops;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.Model.shops.types.*;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PlayerUtils;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
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
  private String logg;
  private String title;
  private String soundopen;
  private String soundclose;
  private int rows;
  private ItemModel fill;
  private List<Shop.FillItems> fillItems;
  private List<Shop> shops;

  public ShopMenu() {
    this.logg = "config/cobbleutils/shop/transactions";
    this.title = "Test";
    this.rows = 6;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.fill = new ItemModel("minecraft:gray_stained_glass_pane");
    this.fillItems = new ArrayList<>();
    this.fillItems.add(new Shop.FillItems());
    this.shops = defaultShops();
  }

  public ShopMenu(String title) {
    this.logg = "config/cobbleutils/shop/transactions";
    this.title = title;
    this.rows = 6;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.fill = new ItemModel("minecraft:gray_stained_glass_pane");
    this.fillItems = new ArrayList<>();
    this.shops = List.of(new Shop());
  }

  public ShopMenu(String title, List<Shop> shops) {
    this.logg = "config/cobbleutils/shop/transactions";
    this.title = title;
    this.rows = 6;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.fill = new ItemModel("minecraft:gray_stained_glass_pane");
    this.fillItems = new ArrayList<>();
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
            if (shop.getShopType() == null) {
              shop.setShopType(new ShopTypePermanent());
            }
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
    shopMenu.getShops().forEach(shop -> Utils.writeFileAsync(path, shop.getId() + ".json", Utils.newGson().toJson(shop)).join());
  }


  public void open(ServerPlayerEntity player) {
    try {
      ChestTemplate template = ChestTemplate
        .builder(this.rows)
        .build();

      shops.forEach(shop -> {
        ItemModel itemModelShop = shop.getDisplay();
        List<String> lore = new ArrayList<>(itemModelShop.getLore());
        switch (shop.getShopType().getTypeShop()) {
          case DYNAMIC:
            ShopTypeDynamic shopTypeDynamic = ((ShopTypeDynamic) shop.getShopType()).updateShop(shop);
            lore.replaceAll(
              s -> s
                .replace("%cooldown%", PlayerUtils.getCooldown(shopTypeDynamic.getCooldown(shop)))
                .replace("%amountProducts%", String.valueOf(shopTypeDynamic.getAmountProducts()))
            );
            break;
          case WEEKLY:
            ShopTypeWeekly shopTypeWeekly = (ShopTypeWeekly) shop.getShopType();
            lore.replaceAll(
              s -> s
                .replace("%days%", shopTypeWeekly.getDayOfWeek().toString())
            );
            break;
          case DYNAMIC_WEEKLY:
            ShopTypeDynamic shopTypeDynamicWeekly = ((ShopTypeDynamic) shop.getShopType()).updateShop(shop);
            ShopTypeWeekly shopTypeWeekly1 = (ShopTypeWeekly) shop.getShopType();
            lore.replaceAll(
              s -> s
                .replace("%cooldown%", PlayerUtils.getCooldown(shopTypeDynamicWeekly.getCooldown(shop)))
                .replace("%amountProducts%", String.valueOf(shopTypeDynamicWeekly.getAmountProducts()))
                .replace("%days%", shopTypeWeekly1.getDayOfWeek().toString())
            );
            break;
          default:
            break;
        }
        GooeyButton button = GooeyButton.builder()
          .display(itemModelShop.getItemStack())
          .title(AdventureTranslator.toNative(itemModelShop.getDisplayname()))
          .lore(Text.class, AdventureTranslator.toNativeL(lore))
          .onClick(action -> {
            if (shop.isActive()) {
              if (shop.getShopType() == null) shop.setShopType(new ShopTypePermanent());
              if (shop.getShopType().getTypeShop() == ShopType.TypeShop.WEEKLY) {
                ShopTypeWeekly shopTypeWeekly = (ShopTypeWeekly) shop.getShopType();
                isDay(player, shop, shopTypeWeekly);
              } else if (shop.getShopType().getTypeShop() == ShopType.TypeShop.DYNAMIC) {
                ((ShopTypeDynamic) shop.getShopType()).updateShop(shop);
                shop.open(player, this);
              } else if (shop.getShopType().getTypeShop() == ShopType.TypeShop.DYNAMIC_WEEKLY) {
                ShopTypeDynamicWeekly shopTypeDynamicWeekly = ((ShopTypeDynamicWeekly) shop.getShopType()).updateShop(shop);
                isDay(player, shop, shopTypeDynamicWeekly);
              } else {
                shop.open(player, this);
              }
            } else {
              player.sendMessage(Text.literal("Shop is not active"));
              SoundUtil.playSound(CobbleUtils.shopLang.getSoundError(), player);
            }
          })
          .build();

        template.set(itemModelShop.getSlot(), button);
      });

      template.fill(GooeyButton.of(fill.getItemStack()));

      if (fillItems != null && !fillItems.isEmpty()) {
        fillItems.forEach(fill -> {
          GooeyButton button = GooeyButton.builder()
            .display(fill.getItemStack())
            .title(AdventureTranslator.toNative(fill.getDisplayname()))
            .lore(Text.class, AdventureTranslator.toNativeL(fill.getLore()))
            .onClick(action -> {
              SoundUtil.playSound(CobbleUtils.shopLang.getSoundError(), player);
            })
            .build();

          fill.getSlots().forEach(slot -> template.set(slot, button));
        });
      }

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

  private void isDay(ServerPlayerEntity player, Shop shop, ShopType shopType) {

    List<DayOfWeek> dayOfWeek = new ArrayList<>();
    if (shopType.getTypeShop() == ShopType.TypeShop.WEEKLY) {
      dayOfWeek = ((ShopTypeWeekly) shopType).getDayOfWeek();
    } else if (shopType.getTypeShop() == ShopType.TypeShop.DYNAMIC_WEEKLY) {
      dayOfWeek = ((ShopTypeDynamicWeekly) shopType).getDayOfWeek();
    }

    if (dayOfWeek.contains(LocalDate.now().getDayOfWeek())) {
      shop.open(player, this);
    } else {
      String message = CobbleUtils.shopLang.getMessageShopWeekly()
        .replace("%prefix%", CobbleUtils.shopLang.getPrefix())
        .replace("%shop%", shop.getId())
        .replace("%days%", dayOfWeek.toString());
      if (shopType instanceof ShopTypeDynamicWeekly shopTypeDynamicWeekly) {
        message = message
          .replace("%cooldown%", PlayerUtils.getCooldown(shopTypeDynamicWeekly.getCooldown(shop)))
          .replace("%amountProducts%", String.valueOf(shopTypeDynamicWeekly.getAmountProducts()));
      }
      player.sendMessage(
        AdventureTranslator.toNative(
          message
        )
      );
      SoundUtil.playSound(CobbleUtils.shopLang.getSoundError(), player);
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
    shops.add(dynamic());
    shops.add(weekly());
    shops.add(dynamicWeekly());
    shops.add(permanent());
    shops.add(shopdefault());
    shops.add(pokeBalls());
    return shops;
  }

  private Shop dynamic() {
    Shop shop = new Shop("dynamic", "<#de504b>Dynamic", (short) 6, "impactor:dollars",
      new ItemModel("minecraft:sculk_sensor"), new ShopTypeDynamic(1));
    List<Shop.Product> products = new ArrayList<>();
    shop.getDisplay().setSlot(0);
    shop.getDisplay().setDisplayname("<#de504b>Dynamic");
    shop.getDisplay().setLore(List.of("&b%cooldown%", "&b%amountProducts%"));
    products.add(new Shop.Product("minecraft:stone", BigDecimal.valueOf(100), BigDecimal.ZERO));
    products.add(new Shop.Product("minecraft:dirt", BigDecimal.valueOf(100), BigDecimal.ZERO));
    products.add(new Shop.Product("minecraft:gravel", BigDecimal.valueOf(100), BigDecimal.ZERO));
    shop.setProducts(products);
    return shop;
  }

  private Shop weekly() {
    Shop shop = new Shop("weekly", "<#de504b>Weekly", (short) 6, "impactor:dollars",
      new ItemModel("minecraft:clock"), new ShopTypeWeekly());
    shop.getDisplay().setSlot(1);
    shop.getDisplay().setDisplayname("<#de504b>Weekly");
    shop.getDisplay().setLore(List.of("&b%days%"));
    List<Shop.Product> products = new ArrayList<>();
    products.add(new Shop.Product("minecraft:stone", BigDecimal.valueOf(100), BigDecimal.ZERO));
    shop.setProducts(products);
    return shop;
  }

  private Shop dynamicWeekly() {
    Shop shop = new Shop("dynamic_weekly", "<#de504b>Dynamic Weekly", (short) 6, "impactor:dollars",
      new ItemModel("minecraft:compass"), new ShopTypeDynamicWeekly());
    shop.getDisplay().setSlot(2);
    shop.getDisplay().setDisplayname("<#de504b>Dynamic Weekly");
    shop.getDisplay().setLore(List.of("&b%cooldown%", "&b%amountProducts%", "&b%days%"));
    List<Shop.Product> products = new ArrayList<>();
    products.add(new Shop.Product("minecraft:stone", BigDecimal.valueOf(100), BigDecimal.ZERO));
    products.add(new Shop.Product("minecraft:dirt", BigDecimal.valueOf(100), BigDecimal.ZERO));
    products.add(new Shop.Product("minecraft:gravel", BigDecimal.valueOf(100), BigDecimal.ZERO));
    shop.setProducts(products);
    return shop;

  }

  private Shop permanent() {
    Shop shop = new Shop("permanent", "<#de504b>Permanent", (short) 6, "impactor:dollars",
      new ItemModel("minecraft:beacon"), new ShopTypePermanent());
    shop.getDisplay().setSlot(3);
    shop.getDisplay().setDisplayname("<#de504b>Permanent");
    List<Shop.Product> products = new ArrayList<>();
    products.add(new Shop.Product("minecraft:stone", BigDecimal.valueOf(100), BigDecimal.ZERO));
    shop.setProducts(products);
    return shop;
  }

  private Shop shopdefault() {
    Shop shop = new Shop("Default", "<#de504b>Default", (short) 6, "impactor:dollars", new ItemModel("cobblemon:poke_ball"));
    shop.getDisplay().setSlot(4);
    shop.getDisplay().setDisplayname("<#de504b>Default");
    List<Shop.Product> products = new ArrayList<>();
    products.add(new Shop.Product("minecraft:stone", BigDecimal.valueOf(100), BigDecimal.ZERO, "cobblemon.command.pc"));
    shop.setProducts(products);
    return shop;
  }


  // PokeBalls
  private Shop pokeBalls() {
    Shop shop = new Shop("pokeballs", "<#de504b>Pokeballs", (short) 6, "impactor:dollars", new ItemModel("cobblemon" +
      ":poke_ball"));
    shop.getDisplay().setSlot(5);
    shop.getDisplay().setDisplayname("<#de504b>Pokeballs");
    List<Shop.Product> products = new ArrayList<>();
    products.add(new Shop.Product("minecraft:stone", BigDecimal.valueOf(100), BigDecimal.ZERO));
    shop.setProducts(products);
    return shop;
  }

  public List<Shop.Product> getAllProducts() {
    List<Shop.Product> products = new ArrayList<>();
    shops.forEach(shop -> products.addAll(shop.getProducts()));
    return products;
  }

  public Shop.Product getProductById(String productId) {
    return shops.stream()
      .map(Shop::getProducts)
      .flatMap(List::stream)
      .filter(product -> product.getProduct().equals(productId))
      .findFirst()
      .orElse(null);
  }
}
