package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.ButtonClick;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.TemplateType;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 02/08/2024 9:25
 */
@Getter
@ToString
@Data
public class Shop {
  private boolean active;
  private String id;
  private String title;
  private String currency;
  private short rows;
  private String soundopen;
  private String soundclose;
  private TemplateType templateType;
  //private ItemModel money;
  private Rectangle rectangle;
  private ItemModel display;
  private List<Product> products;
  private List<FillItems> fillItems;

  @Getter
  @ToString
  private class Rectangle {
    private int startRow;
    private int startColumn;
    private int length;
    private int width;

    public Rectangle() {
      this.startRow = 0;
      this.startColumn = 0;
      this.length = 5;
      this.width = 9;
    }

    public Rectangle(int startRow, int startColumn, int length, int width) {
      this.startRow = startRow;
      this.startColumn = startColumn;
      this.length = length;
      this.width = width;
    }
  }

  public Shop() {
    this.active = true;
    this.id = "default";
    this.title = "Default";
    this.rows = 6;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.currency = "dollars";
    this.templateType = TemplateType.CHEST;
    this.rectangle = new Rectangle();
   /* money = new ItemModel("cobblemon:relic_coin_sack");
    money.setSlot(47);
    money.setDisplayname("Balance");
    money.setLore(List.of(
      "You have: %balance% %currency%"
    ));*/
    this.display = new ItemModel("cobblemon:poke_ball");
    this.products = new ArrayList<>();
    this.products.add(new Product());
    this.fillItems = new ArrayList<>();
    this.fillItems.add(new FillItems());
  }

  public Shop(String id, String title, short rows, String currency, ItemModel display) {
    this.active = true;
    this.id = id;
    this.title = title;
    this.rows = rows;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.currency = currency;
    this.rectangle = new Rectangle();
    this.templateType = TemplateType.CHEST;
    this.display = display;
    /*money = new ItemModel("cobblemon:relic_coin_sack");
    money.setSlot(47);
    money.setDisplayname("Balance");
    money.setLore(List.of(
      "You have: %balance% %currency%"
    ));*/
    this.products = new ArrayList<>();
    this.products.add(new Product());
    this.fillItems = new ArrayList<>();
    this.fillItems.add(new FillItems());
  }


  @Getter
  @ToString
  @Data
  public static class Product {
    private ItemChance product;
    private BigDecimal buy;
    private BigDecimal sell;

    public Product() {
      this.product = new ItemChance();
      this.buy = BigDecimal.ZERO;
      this.sell = BigDecimal.ZERO;
    }

    public Product(ItemChance product, BigDecimal buy, BigDecimal sell) {
      this.product = product;
      this.buy = buy;
      this.sell = sell;
    }
  }

  @EqualsAndHashCode(callSuper = true)
  @Getter
  @ToString
  @Data
  public static class FillItems extends ItemModel {
    private List<Integer> slots;

    public FillItems() {
      super("minecraft:gray_stained_glass_pane");
      slots = new ArrayList<>();
    }
  }

  public void open(ServerPlayerEntity player, ShopMenu shopMenu) {
    try {
      short rows = this.rows;
      if (rows >= 6) {
        rows = 6;
      } else if (rows <= 1) {
        rows = 1;
      }

      ChestTemplate template = ChestTemplate
        .builder(rows)
        .build();

      List<Button> buttons = new ArrayList<>();

      String symbol = EconomyUtil.getSymbol(getCurrency());

      products.forEach(product -> {
        BigDecimal buy = product.getBuy();
        BigDecimal sell = product.getSell();


        List<String> lore = new ArrayList<>(CobbleUtils.shopLang.getLoreProduct());
        if (buy.compareTo(BigDecimal.ZERO) <= 0) {
          lore.removeIf(s -> s.contains("%buy%"));
        }
        if (sell.compareTo(BigDecimal.ZERO) <= 0) {
          lore.removeIf(s -> s.contains("%sell%"));
        }
        lore.replaceAll(s -> s.replace("%buy%", String.valueOf(buy))
          .replace("%sell%", String.valueOf(sell))
          .replace("%currency%", getCurrency())
          .replace("%symbol%", symbol)
          .replace("%amount%", String.valueOf(product.getProduct().getItemStack().getCount()))
        );

        TypeError typeError = getTypeError(product);

        if (typeError != TypeError.NONE) {
          lore = new ArrayList<>();
          lore.add("&cError in this product");
          lore.add("&cContact the server administrator");
          lore.add(product.getProduct().toString());
          lore.add("&cError: " + typeError.name());
        }

        ItemStack itemStack;
        if (typeError == TypeError.NONE) {
          itemStack = product.getProduct().getItemStack();
        } else {
          itemStack = Utils.parseItemId("minecraft:barrier");
        }

        GooeyButton button = GooeyButton.builder()
          .display(itemStack)
          .lore(Text.class, AdventureTranslator.toNativeL(lore))
          .onClick(action -> {
            if (typeError == TypeError.NONE) {
              if (action.getClickType() == ButtonClick.LEFT_CLICK || action.getClickType() == ButtonClick.SHIFT_LEFT_CLICK) {
                if (buy.compareTo(BigDecimal.ZERO) > 0) {
                  SoundUtil.playSound(getSoundopen(), player);
                  openBuySellMenu(player, product, TypeMenu.BUY, 1);
                }
              } else if (action.getClickType() == ButtonClick.RIGHT_CLICK || action.getClickType() == ButtonClick.SHIFT_RIGHT_CLICK) {
                if (sell.compareTo(BigDecimal.ZERO) > 0) {
                  SoundUtil.playSound(getSoundopen(), player);
                  openBuySellMenu(player, product, TypeMenu.SELL, 1);
                }
              }
            } else {
              sendError(player, typeError);
            }
          })
          .build();

        buttons.add(button);
      });


      LinkedPageButton next = UIUtils.getNextButton(action -> {
        SoundUtil.playSound(getSoundopen(), player);
      });

      LinkedPageButton previous = UIUtils.getPreviousButton(action -> {
        SoundUtil.playSound(getSoundopen(), player);
      });

      GooeyButton close = UIUtils.getCloseButton(action -> {
        SoundUtil.playSound(getSoundclose(), player);
        shopMenu.open(player);
      });

      if (!getFillItems().isEmpty()) {
        getFillItems().forEach(fillItem -> {
          ItemStack itemStack = fillItem.getItemStack();
          fillItem.getSlots().forEach(fillItemSlot -> {
            template.set(fillItemSlot, GooeyButton.of(itemStack));
          });
        });
      } else {
        template.fill(GooeyButton.of(Utils.parseItemId(CobbleUtils.config.getFill())));
      }


      template.rectangle(getRectangle().getStartRow(),
        getRectangle().getStartColumn(),
        getRectangle().getLength(),
        getRectangle().getWidth(),
        new PlaceholderButton());

      // Balance
      ItemModel balance = CobbleUtils.shopLang.getBalance();
      List<String> lorebalance = new ArrayList<>(balance.getLore());
      lorebalance.replaceAll(s -> s.replace("%balance%", EconomyUtil.getBalance(player, getCurrency()).toPlainString())
        .replace("%currency%", getCurrency())
        .replace("%symbol%", symbol));

      template.set((this.rows * 9) - 7, GooeyButton.builder()
        .display(balance.getItemStack())
        .title(AdventureTranslator.toNative(balance.getDisplayname()))
        .lore(Text.class, AdventureTranslator.toNativeL(lorebalance))
        .build());

      // Display
      template.set((this.rows * 9) - 1, next);
      template.set((this.rows * 9) - 5, close);
      template.set((this.rows * 9) - 9, previous);

      LinkedPage.Builder linkedPageBuilder = LinkedPage
        .builder()
        .title(AdventureTranslator.toNative(this.title))
        .onOpen(pageAction -> {
          SoundUtil.playSound(getSoundopen(), player);
        })
        .onClose(pageAction -> {
          SoundUtil.playSound(getSoundclose(), player);
        });

      GooeyPage page = PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
      UIManager.openUIForcefully(player, page);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private enum TypeMenu {
    BUY, SELL
  }

  private enum TypeError {
    BOTH, ZERO, NONE
  }

  private TypeError getTypeError(Product product) {
    if (product.getBuy().compareTo(BigDecimal.ZERO) <= 0 && product.getSell().compareTo(BigDecimal.ZERO) <= 0) {
      return TypeError.ZERO;
    } else if (product.getSell().compareTo(product.getBuy()) > 0) {
      return TypeError.BOTH;
    } else {
      return TypeError.NONE;
    }
  }

  private void sendError(ServerPlayerEntity player, TypeError typeError) {
    if (Objects.requireNonNull(typeError) == TypeError.BOTH) {
      player.sendMessage(Text.literal("Buy price is higher than sell price contact the server administrator"));
    } else if (Objects.requireNonNull(typeError) == TypeError.ZERO) {
      player.sendMessage(Text.literal("Buy and sell price is zero contact the server administrator"));
    }
  }

  private void openBuySellMenu(ServerPlayerEntity player, Product product, TypeMenu typeMenu, int amount) {
    ChestTemplate template = ChestTemplate
      .builder(CobbleUtils.shopConfig.getRowsBuySellMenu())
      .build();

    int maxStack = product.getProduct().getItemStack(amount).getMaxCount();
    BigDecimal price = calculatePrice(product, typeMenu, amount);
    String title = generateTitle(product, typeMenu);
    String symbol = EconomyUtil.getSymbol(getCurrency());

    // Botones de cantidad
    if (maxStack != 1) {
      createAmountButton(template, player, product, typeMenu, CobbleUtils.shopLang.getAdd1(),
        CobbleUtils.shopLang.getRemove1(), 1, amount);

      if (maxStack == 16) {
        createAmountButton(template, player, product, typeMenu, CobbleUtils.shopLang.getAdd8(),
          CobbleUtils.shopLang.getRemove8(), 8, amount);
        createAmountButton(template, player, product, typeMenu, CobbleUtils.shopLang.getAdd16(),
          CobbleUtils.shopLang.getRemove16(), 16, amount);
      }
      if (maxStack == 64) {
        createAmountButton(template, player, product, typeMenu, CobbleUtils.shopLang.getAdd10(),
          CobbleUtils.shopLang.getRemove10(), 10, amount);
        createAmountButton(template, player, product, typeMenu, CobbleUtils.shopLang.getAdd64(),
          CobbleUtils.shopLang.getRemove64(), 64, amount);
      }
    }
    // Botón de ver producto
    createViewProductButton(template, product, typeMenu, amount, price, symbol);

    // Botón de confirmar
    ItemModel confirm = CobbleUtils.shopLang.getConfirm();
    template.set(confirm.getSlot(), confirm.getButton(action -> {
      // Lógica de confirmación aquí
    }));

    // Botón de comprar pilas completas
    createMaxStackButton(template, player, product, typeMenu, maxStack);

    // Botón de cancelar
    createCancelButton(template, player);

    // Relleno y botón de cerrar
    template.fill(GooeyButton.of(Utils.parseItemId(CobbleUtils.config.getFill())));
    createCloseButton(template, player);

    GooeyPage page = GooeyPage.builder()
      .title(AdventureTranslator.toNative(title))
      .template(template)
      .onOpen(pageAction -> {
        SoundUtil.playSound(getSoundopen(), player);
      })
      .onClose(pageAction -> {
        SoundUtil.playSound(getSoundclose(), player);
      })
      .build();

    UIManager.openUIForcefully(player, page);
  }

  private BigDecimal calculatePrice(Product product, TypeMenu typeMenu, int amount) {
    return (typeMenu == TypeMenu.BUY) ?
      product.getBuy().multiply(BigDecimal.valueOf(amount)) :
      product.getSell().multiply(BigDecimal.valueOf(amount));
  }

  private String generateTitle(Product product, TypeMenu typeMenu) {
    return (typeMenu == TypeMenu.BUY) ?
      CobbleUtils.shopLang.getTitleBuy().replace("%product%", ItemUtils.getTranslatedName(product.getProduct().getItemStack())) :
      CobbleUtils.shopLang.getTitleSell().replace("%product%", ItemUtils.getTranslatedName(product.getProduct().getItemStack()));
  }

  private void createAmountButton(ChestTemplate template, ServerPlayerEntity player,
                                  Product product, TypeMenu typeMenu,
                                  ItemModel addModel, ItemModel removeModel,
                                  int increment, int amount) {

    template.set(addModel.getSlot(), GooeyButton.builder()
      .display(addModel.getItemStack(increment))
      .title(AdventureTranslator.toNative(addModel.getDisplayname()))
      .lore(Text.class, AdventureTranslator.toNativeL(addModel.getLore()))
      .onClick(action -> openBuySellMenu(player, product, typeMenu, Math.min(amount + increment, product.getProduct().getItemStack(amount).getMaxCount())))
      .build());

    template.set(removeModel.getSlot(), GooeyButton.builder()
      .display(removeModel.getItemStack(increment))
      .title(AdventureTranslator.toNative(removeModel.getDisplayname()))
      .lore(Text.class, AdventureTranslator.toNativeL(removeModel.getLore()))
      .onClick(action -> openBuySellMenu(player, product, typeMenu, Math.max(amount - increment, 1)))
      .build());
  }

  private void createViewProductButton(ChestTemplate template, Product product, TypeMenu typeMenu, int amount, BigDecimal price, String symbol) {
    List<String> loreProduct = new ArrayList<>(CobbleUtils.shopLang.getLoreProduct());
    loreProduct.replaceAll(s -> s.replace("%buy%", String.valueOf(price))
      .replace("%sell%", String.valueOf(price))
      .replace("%currency%", getCurrency())
      .replace("%symbol%", symbol)
      .replace("%amount%", String.valueOf(product.getProduct().getItemStack().getCount())));

    if (typeMenu == TypeMenu.SELL) {
      loreProduct.removeIf(s -> s.contains("%buy%"));
    } else {
      loreProduct.removeIf(s -> s.contains("%sell%"));
    }

    ItemStack viewProduct = product.getProduct().getItemStack(amount);
    template.set(CobbleUtils.shopConfig.getSlotViewProduct(), GooeyButton.builder()
      .display(viewProduct)
      .title(AdventureTranslator.toNative(ItemUtils.getTranslatedName(viewProduct)))
      .lore(Text.class, AdventureTranslator.toNativeL(loreProduct))
      .build());
  }

  private void createMaxStackButton(ChestTemplate template, ServerPlayerEntity player, Product product, TypeMenu typeMenu, int maxStack) {
    ItemModel buyStacks = CobbleUtils.shopLang.getBuyStacks();
    template.set(buyStacks.getSlot(), GooeyButton.builder()
      .display(product.getProduct().getItemStack(maxStack))
      .title(AdventureTranslator.toNative(buyStacks.getDisplayname()))
      .lore(Text.class, AdventureTranslator.toNativeL(buyStacks.getLore()))
      .onClick(action -> {
        openStackMenu(player, product, typeMenu, maxStack);
      })
      .build());
  }

  private void createCancelButton(ChestTemplate template, ServerPlayerEntity player) {
    ItemModel cancel = CobbleUtils.shopLang.getCancel();
    template.set(cancel.getSlot(), cancel.getButton(action -> {
      open(player, CobbleUtils.shopConfig.getShop());
    }));
  }

  private void createCloseButton(ChestTemplate template, ServerPlayerEntity player) {
    template.set((getRows() * 9) - 5, UIUtils.getCloseButton(action -> {
      open(player, CobbleUtils.shopConfig.getShop());
    }));
  }

  private void openStackMenu(ServerPlayerEntity player, Product product, TypeMenu typeMenu, int maxStack) {
  }
}
