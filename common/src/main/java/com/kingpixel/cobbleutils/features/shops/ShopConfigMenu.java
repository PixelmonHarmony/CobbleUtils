package com.kingpixel.cobbleutils.features.shops;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.config.ShopConfig;
import com.kingpixel.cobbleutils.features.shops.models.Product;
import com.kingpixel.cobbleutils.features.shops.models.types.*;
import com.kingpixel.cobbleutils.util.*;
import lombok.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Carlos Varas Alonso - 06/08/2024 11:10
 */
@Getter
@Setter
@EqualsAndHashCode
@Data
@ToString
public class ShopConfigMenu {
  private String logg;
  private String title;
  private String soundopen;
  private String soundclose;
  private short rowsBuySellMenu;
  private short slotViewProduct;
  private int rows;
  private ItemModel fill;
  private List<Shop.FillItems> fillItems;
  private static Map<ShopMod, List<Shop>> shops = new ConcurrentHashMap<>();

  public static void addShops(String modId, String pathShops, List<Shop> shopList) {
    ShopMod shopMod = new ShopMod(modId, pathShops);
    shops.put(shopMod, shopList);
    shopList.forEach(ShopSell::addProduct);
  }

  @Getter
  @Setter
  @EqualsAndHashCode
  @Data
  @ToString
  public static class ShopMod {
    private String mod_id;
    private String path;

    public ShopMod(String modId, String path) {
      this.mod_id = modId;
      this.path = path;
    }
  }

  public ShopConfigMenu() {
    this.logg = "config/cobbleutils/shop/transactions";
    this.title = "Test";
    this.rowsBuySellMenu = 6;
    this.slotViewProduct = 22;
    this.rows = 6;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.fill = new ItemModel("minecraft:gray_stained_glass_pane");
    this.fillItems = new ArrayList<>();
    this.fillItems.add(new Shop.FillItems());
  }

  public ShopConfigMenu(String title) {
    this.logg = "config/cobbleutils/shop/transactions";
    this.title = title;
    this.rows = 6;
    this.rowsBuySellMenu = 6;
    this.slotViewProduct = 22;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.fill = new ItemModel("minecraft:gray_stained_glass_pane");
    this.fillItems = new ArrayList<>();
  }

  public ShopConfigMenu(String title, List<Shop> shops) {
    this.logg = "config/cobbleutils/shop/transactions";
    this.title = title;
    this.rows = 6;
    this.rowsBuySellMenu = 6;
    this.slotViewProduct = 22;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.fill = new ItemModel("minecraft:gray_stained_glass_pane");
    this.fillItems = new ArrayList<>();
  }


  public static List<Shop> getShopsMod(String mod_id) {
    return shops.entrySet().stream()
      .filter(entry -> entry.getKey().getMod_id().equals(mod_id))
      .findFirst()
      .map(Map.Entry::getValue)
      .orElse(new ArrayList<>());
  }

  public static List<Shop> getShops(String path) {
    List<Shop> shopList = new ArrayList<>();
    File folder = Utils.getAbsolutePath(path);

    if (folder.exists() && folder.isDirectory()) {
      File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

      if (files != null) {
        Gson gson = Utils.newGson();
        for (File file : files) {
          try (FileReader reader = new FileReader(file)) {
            Shop shop = gson.fromJson(reader, Shop.class);
            shop.setId(file.getName().replace(".json", ""));
            if (shop.getShopType() == null) shop.setShopType(new ShopTypePermanent());

            shopList.add(shop);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }

    saveShops(path);
    return shopList;
  }

  public static void open(ServerPlayerEntity player, ShopConfig shopConfig, String mod_id, boolean byCommand) {
    try {
      ChestTemplate template = ChestTemplate
        .builder(shopConfig.getShop().getRows())
        .build();

      getShopsMod(mod_id).forEach(shop -> {
        ItemModel itemModelShop = shop.getDisplay();
        if (itemModelShop.getSlot() < 0) return;

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
            ShopTypeDynamicWeekly shopTypeDynamicWeekly = ((ShopTypeDynamicWeekly) shop.getShopType()).updateShop(shop);
            lore.replaceAll(
              s -> s
                .replace("%cooldown%", PlayerUtils.getCooldown(shopTypeDynamicWeekly.getCooldown(shop)))
                .replace("%amountProducts%", String.valueOf(shopTypeDynamicWeekly.getAmountProducts()))
                .replace("%days%", shopTypeDynamicWeekly.getDayOfWeek().toString())
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
                isDay(player, shopConfig, mod_id, shop, shopTypeWeekly);
              } else if (shop.getShopType().getTypeShop() == ShopType.TypeShop.DYNAMIC) {
                ((ShopTypeDynamic) shop.getShopType()).updateShop(shop);
                shop.open(player, shopConfig, mod_id, byCommand);
              } else if (shop.getShopType().getTypeShop() == ShopType.TypeShop.DYNAMIC_WEEKLY) {
                ShopTypeDynamicWeekly shopTypeDynamicWeekly = ((ShopTypeDynamicWeekly) shop.getShopType()).updateShop(shop);
                isDay(player, shopConfig, mod_id, shop, shopTypeDynamicWeekly);
              } else {
                shop.open(player, shopConfig, mod_id, byCommand);
              }
            } else {
              player.sendMessage(Text.literal("Shop is not active"));
              SoundUtil.playSound(CobbleUtils.shopLang.getSoundError(), player);
            }
          })
          .build();

        template.set(itemModelShop.getSlot(), button);
      });

      template.fill(GooeyButton.of(shopConfig.getShop().fill.getItemStack()));

      if (shopConfig.getShop().fillItems != null && !shopConfig.getShop().fillItems.isEmpty()) {
        shopConfig.getShop().fillItems.forEach(fill -> {
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

      GooeyButton close = UIUtils.getCloseButton(action -> {
        UIManager.closeUI(action.getPlayer());
      });

      template.set(shopConfig.getShop().getRows() * 9 - 5, close);

      GooeyPage page = GooeyPage
        .builder()
        .template(template)
        .title(AdventureTranslator.toNative(shopConfig.getShop().title))
        .onClose(pageAction -> {
          SoundUtil.playSound(shopConfig.getShop().getSoundclose(), player);
        })
        .build();

      UIManager.openUIForcefully(player, page);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private static void isDay(ServerPlayerEntity player, ShopConfig shopConfig, String mod_id, Shop shop, ShopType shopType) {
    List<DayOfWeek> dayOfWeek = new ArrayList<>();
    if (shopType.getTypeShop() == ShopType.TypeShop.WEEKLY) {
      dayOfWeek = ((ShopTypeWeekly) shopType).getDayOfWeek();
    } else if (shopType.getTypeShop() == ShopType.TypeShop.DYNAMIC_WEEKLY) {
      dayOfWeek = ((ShopTypeDynamicWeekly) shopType).getDayOfWeek();
    }

    if (dayOfWeek.contains(LocalDate.now().getDayOfWeek())) {
      shop.open(player, shopConfig, mod_id, false);
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

  public static Shop getShop(String shopId) {
    return shops.values().stream()
      .flatMap(List::stream)
      .filter(shop -> shop.getId().equals(shopId))
      .findFirst()
      .orElse(null);
  }

  public void open(ServerPlayerEntity player, String shopId, ShopConfig shopConfig, String mod_id, boolean b) {
    Shop shop = getShop(shopId);

    if (shop != null) {
      shop.open(player, shopConfig, mod_id, b);
    } else {
      player.sendMessage(Text.literal("Shop not found"));
    }
  }

  public List<Product> getAllProducts() {
    List<Product> products = new ArrayList<>();
    shops.values().forEach(shopList -> products.addAll(shopList.stream()
      .flatMap(shop -> shop.getProducts().stream())
      .toList()));
    return products;
  }

  public Product getProductById(String productId) {
    return shops.values().stream()
      .flatMap(List::stream)
      .map(Shop::getProducts)
      .flatMap(List::stream)
      .filter(product -> product.getProduct().equals(productId))
      .findFirst()
      .orElse(null);
  }

  public static void saveShops(String path) {
    Gson gson = Utils.newGson();

    for (Map.Entry<ShopMod, List<Shop>> entry : shops.entrySet()) {// Itera sobre cada ShopMod y su lista de Shops
      ShopMod shopMod = entry.getKey();
      if (!shopMod.path.equalsIgnoreCase(path)) continue;
      List<Shop> shopList = entry.getValue();

      for (Shop shop : shopList) {  // Itera sobre cada Shop en la lista de Shops
        String json = gson.toJson(shop);  // Convierte el objeto Shop a JSON
        String fileName = shop.getId() + ".json";  // Usa el ID de la tienda como nombre de archivo

        try {
          // Escribe el JSON en el archivo correspondiente
          Utils.writeFileAsync(shopMod.path, fileName, json).join();
        } catch (Exception e) {
          e.printStackTrace();  // Maneja cualquier excepción que ocurra durante la escritura
        }
      }
    }
  }
}
