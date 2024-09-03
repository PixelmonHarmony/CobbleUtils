package com.kingpixel.cobbleutils.Model.shops;

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
import com.kingpixel.cobbleutils.Model.ItemChance;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.Model.shops.types.ShopType;
import com.kingpixel.cobbleutils.Model.shops.types.ShopTypeDynamic;
import com.kingpixel.cobbleutils.Model.shops.types.ShopTypeDynamicWeekly;
import com.kingpixel.cobbleutils.Model.shops.types.ShopTypePermanent;
import com.kingpixel.cobbleutils.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
  private ShopType shopType;
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
    this.shopType = new ShopTypePermanent();
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
    this.shopType = new ShopTypePermanent();
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

  public Shop(String id, String title, short rows, String currency, ItemModel display, ShopType shopType) {
    this.active = true;
    this.id = id;
    this.title = title;
    this.rows = rows;
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.currency = currency;
    this.rectangle = new Rectangle();
    this.templateType = TemplateType.CHEST;
    this.shopType = shopType;
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
    private String permission;
    private String display;
    private String displayname;
    private List<String> lore;
    private Long CustomModelData;
    private String product;
    private BigDecimal buy;
    private BigDecimal sell;

    public Product() {
      this.display = null;
      this.displayname = null;
      this.lore = null;
      this.CustomModelData = null;
      this.permission = null;
      this.product = "minecraft:stone";
      this.buy = BigDecimal.ZERO;
      this.sell = BigDecimal.ZERO;
    }

    public Product(String product, BigDecimal buy, BigDecimal sell) {
      this.display = null;
      this.displayname = null;
      this.lore = null;
      this.CustomModelData = null;
      this.permission = null;
      this.product = product;
      this.buy = buy;
      this.sell = sell;
    }

    public Product(String product, BigDecimal buy, BigDecimal sell, String permission) {
      this.display = null;
      this.displayname = null;
      this.lore = null;
      this.CustomModelData = null;
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
      List<Product> products;

      if (shopType.getTypeShop() == ShopType.TypeShop.DYNAMIC) {
        products = ((ShopTypeDynamic) shopType).updateShop(this).getProducts(this);
      } else if (shopType.getTypeShop() == ShopType.TypeShop.DYNAMIC_WEEKLY) {
        products = ((ShopTypeDynamicWeekly) shopType).updateShop(this).getProducts(this);
      } else {
        products = this.products;
      }

      if (products == null) {
        if (CobbleUtils.config.isDebug()) {
          CobbleUtils.LOGGER.info("Products null");
        }
        products = new ArrayList<>();
      }

      products.forEach(product -> {
        BigDecimal buy = product.getBuy().setScale(EconomyUtil.getDecimals(getCurrency()), RoundingMode.HALF_UP);
        BigDecimal sell = product.getSell().setScale(EconomyUtil.getDecimals(getCurrency()), RoundingMode.HALF_UP);


        List<String> lore = new ArrayList<>(CobbleUtils.shopLang.getLoreProduct());
        if (buy.compareTo(BigDecimal.ZERO) <= 0) {
          lore.removeIf(s -> s.contains("%buy%"));
        }
        if (sell.compareTo(BigDecimal.ZERO) <= 0) {
          lore.removeIf(s -> s.contains("%sell%"));
        }
        lore.replaceAll(s -> s.replace("%buy%", EconomyUtil.formatCurrency(buy, currency, player.getUuid()))
          .replace("%sell%", EconomyUtil.formatCurrency(sell, currency, player.getUuid()))
          .replace("%currency%", getCurrency())
          .replace("%symbol%", symbol)
          .replace("%amount%", "1")
          .replace("%amountproduct%", String.valueOf(product.getItemchance().getItemStack().getCount()))
          .replace("%total%", String.valueOf(product.getItemchance().getItemStack().getCount()))
          .replace("%balance%", EconomyUtil.getBalance(player, getCurrency(), EconomyUtil.getDecimals(getCurrency())))
        );
        TypeError typeError = getTypeError(product, player);

        if (typeError == TypeError.PERMISSION) {
          lore = new ArrayList<>(lore);
          lore.add(CobbleUtils.shopLang.getNotPermission()
            .replace("%product%", ItemUtils.getTranslatedName(product.getItemchance().getItemStack())));
        } else if (typeError != TypeError.NONE) {
          lore = new ArrayList<>();
          lore.add("&cError in this product");
          lore.add("&cContact the server administrator");
          lore.add(product.getItemchance().toString());
          lore.add("&cError: " + typeError.name());
        }

        ItemStack itemStack;
        if (typeError == TypeError.NONE) {
          itemStack = getViewItemStack(product, 1);
        } else {
          if (CobbleUtils.shopLang.isChangeItemError()) {
            itemStack = product.getItemchance().getItemStack();
          } else {
            itemStack = Utils.parseItemId("minecraft:barrier");
          }
        }
        int defaultamount;
        if (product.getItemchance().getType() == ItemChance.ItemChanceType.MONEY ||
          product.getItemchance().getType() == ItemChance.ItemChanceType.COMMAND) {
          defaultamount = 1;
        } else {
          defaultamount = 0;
        }

        GooeyButton button = GooeyButton.builder()
          .display(itemStack)
          .lore(Text.class, AdventureTranslator.toNativeL(lore))
          .onClick(action -> {
            if (typeError == TypeError.NONE) {
              if (action.getClickType() == ButtonClick.LEFT_CLICK || action.getClickType() == ButtonClick.SHIFT_LEFT_CLICK) {
                if (buy.compareTo(BigDecimal.ZERO) > 0) {
                  SoundUtil.playSound(getSoundopen(), player);
                  openBuySellMenu(shopMenu, player, product, TypeMenu.BUY, defaultamount);
                }
              } else if (action.getClickType() == ButtonClick.RIGHT_CLICK || action.getClickType() == ButtonClick.SHIFT_RIGHT_CLICK) {
                if (sell.compareTo(BigDecimal.ZERO) > 0) {
                  SoundUtil.playSound(getSoundopen(), player);
                  openBuySellMenu(shopMenu, player, product, TypeMenu.SELL, defaultamount);
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
        SoundUtil.playSound(getSoundopen(), action.getPlayer());
      });

      LinkedPageButton previous = UIUtils.getPreviousButton(action -> {
        SoundUtil.playSound(getSoundopen(), action.getPlayer());
      });

      GooeyButton close = UIUtils.getCloseButton(action -> {
        shopMenu.open(action.getPlayer());
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

      lorebalance.replaceAll(s -> s
        .replace("%balance%", EconomyUtil.getBalance(player, getCurrency(), EconomyUtil.getDecimals(getCurrency())))
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
        .onOpen(pageAction -> SoundUtil.playSound(getSoundopen(), pageAction.getPlayer()))
        .onClose(pageAction -> {
          SoundUtil.playSound(getSoundclose(), pageAction.getPlayer());
        });

      GooeyPage page = PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
      UIManager.openUIForcefully(player, page);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private enum TypeMenu {
    BUY, SELL, STACK
  }

  private enum TypeError {
    INVALID_PRICE, ZERO, PERMISSION, NONE
  }

  private TypeError getTypeError(Product product, ServerPlayerEntity player) {
    if (product.getBuy().compareTo(BigDecimal.ZERO) <= 0 && product.getSell().compareTo(BigDecimal.ZERO) <= 0) {
      return TypeError.ZERO;
    } else if (!LuckPermsUtil.checkPermission(player, product.getPermission())) {
      return TypeError.PERMISSION;
    } else if (product.getBuy().compareTo(BigDecimal.ZERO) > 0 && product.getSell().compareTo(product.getBuy()) > 0) {
      return TypeError.INVALID_PRICE;
    } else {
      return TypeError.NONE;
    }
  }


  private void sendError(ServerPlayerEntity player, TypeError typeError) {
    if (Objects.requireNonNull(typeError) == TypeError.INVALID_PRICE) {
      player.sendMessage(Text.literal("Buy price is higher than sell price contact the server administrator"));
    } else if (Objects.requireNonNull(typeError) == TypeError.ZERO) {
      player.sendMessage(Text.literal("Buy and sell price is zero contact the server administrator"));
    } else if (Objects.requireNonNull(typeError) == TypeError.PERMISSION) {
      player.sendMessage(Text.literal("You don't have permission to buy or sell this product"));
    }
  }

  private void openBuySellMenu(ShopMenu shopMenu, ServerPlayerEntity player, Product product, TypeMenu typeMenu, int amount) {
    ChestTemplate template = ChestTemplate
      .builder(CobbleUtils.shopConfig.getRowsBuySellMenu())
      .build();

    int maxStack = product.getItemchance().getItemStack().getMaxCount();
    BigDecimal price = calculatePrice(product, typeMenu, (amount == 0 ? 1 : amount)).setScale(EconomyUtil.getDecimals(getCurrency()), RoundingMode.HALF_UP);
    String title = generateTitle(product, typeMenu);
    String symbol = EconomyUtil.getSymbol(getCurrency());

    // Botones de cantidad
    if (maxStack != 1) {
      createAmountButton(shopMenu, template, player, product, typeMenu, CobbleUtils.shopLang.getAdd1(),
        CobbleUtils.shopLang.getRemove1(), 1, amount);

      if (maxStack == 16) {
        createAmountButton(shopMenu, template, player, product, typeMenu, CobbleUtils.shopLang.getAdd8(),
          CobbleUtils.shopLang.getRemove8(), 8, amount);
        createAmountButton(shopMenu, template, player, product, typeMenu, CobbleUtils.shopLang.getAdd16(),
          CobbleUtils.shopLang.getRemove16(), 16, amount);
      }
      if (maxStack == 64) {
        createAmountButton(shopMenu, template, player, product, typeMenu, CobbleUtils.shopLang.getAdd10(),
          CobbleUtils.shopLang.getRemove10(), 10, amount);
        createAmountButton(shopMenu, template, player, product, typeMenu, CobbleUtils.shopLang.getAdd64(),
          CobbleUtils.shopLang.getRemove64(), 64, amount);
      }
    }

    // Botón de ver producto
    createViewProductButton(shopMenu, player, template, product, typeMenu, amount, price, symbol);

    // Botón de confirmar
    ItemModel confirm = CobbleUtils.shopLang.getConfirm();
    template.set(confirm.getSlot(), confirm.getButton(action -> {

      int finalamount = amount;
      if (finalamount == 0) {
        finalamount = 1;
      }
      BigDecimal finalprice = calculatePrice(product, typeMenu, finalamount).setScale(EconomyUtil.getDecimals(getCurrency()),
        RoundingMode.HALF_UP);
      if (typeMenu == TypeMenu.BUY) {
        if (buyProduct(player, product, finalamount, finalprice)) {
          ShopTransactions.addTransaction(player.getUuid(), this, ShopTransactions.ShopAction.BUY, product,
            BigDecimal.valueOf(finalamount), finalprice);
          open(player, CobbleUtils.shopConfig.getShop());
          ShopTransactions.updateTransaction(player.getUuid(), shopMenu);
        }
      }
      if (typeMenu == TypeMenu.SELL) {
        if (sellProduct(player, product, finalamount)) {
          ShopTransactions.addTransaction(player.getUuid(), this, ShopTransactions.ShopAction.SELL, product,
            BigDecimal.valueOf(finalamount), finalprice);
          open(player, CobbleUtils.shopConfig.getShop());
          ShopTransactions.updateTransaction(player.getUuid(), shopMenu);
        }
      }

    }));

    // Botón de comprar pilas completas
    //createMaxStackButton(template, player, product, typeMenu, maxStack);

    // Botón de cancelar
    createCancelButton(template, player);

    // Relleno y botón de cerrar
    template.fill(GooeyButton.of(Utils.parseItemId(CobbleUtils.config.getFill())));

    createCloseButton(template, player);

    GooeyPage page = GooeyPage.builder()
      .title(AdventureTranslator.toNative(title))
      .template(template)
      .build();

    page.subscribe(amount, () -> {
      createViewProductButton(shopMenu, player, template, product, typeMenu, amount, price, symbol);
    });

    UIManager.openUIPassively(player, page, 10, TimeUnit.MILLISECONDS);
  }

  private boolean buyProduct(ServerPlayerEntity player, Product product, int amount, BigDecimal price) {
    if (price.compareTo(BigDecimal.ZERO) <= 0) return false;
    if (EconomyUtil.hasEnough(player, getCurrency(), price)) {
      SoundUtil.playSound(CobbleUtils.shopLang.getSoundBuy(), player);

      ItemChance itemChance = product.getItemchance();
      ItemStack productStack = itemChance.getItemStack();
      int itemsPerPackage = productStack.getCount(); // Número de ítems en cada paquete
      int maxStackSize = productStack.getMaxCount(); // Capacidad máxima de un ItemStack

      // Total de ítems a entregar
      int totalItems = amount * itemsPerPackage;

      // Calcula cuántos paquetes completos se necesitan
      int fullStacks = totalItems / maxStackSize;
      int remainder = totalItems % maxStackSize;

      // Entregar los paquetes completos
      for (int i = 0; i < fullStacks; i++) {
        ItemChance.giveReward(player, itemChance, maxStackSize / itemsPerPackage);
      }

      // Entregar los paquetes parciales si es necesario
      if (remainder > 0) {
        int remainderPackages = (int) Math.ceil((double) remainder / itemsPerPackage);
        ItemChance.giveReward(player, itemChance, remainderPackages);
      }

      // Si llegamos aquí, la compra se realizó con éxito
      return true;
    }
    return false;
  }


  private boolean sellProduct(ServerPlayerEntity player, Product product, int amount) {
    int packageSize = product.getItemchance().getItemStack().getCount();
    int digits = EconomyUtil.getDecimals(getCurrency());
    BigDecimal unitPrice =
      product.getSell().setScale(digits, RoundingMode.HALF_UP).divide(BigDecimal.valueOf(packageSize), digits, RoundingMode.HALF_UP);
    BigDecimal totalPrice =
      unitPrice.setScale(digits, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(amount));

    // Verifica si el jugador tiene la cantidad requerida del producto en su inventario
    int amountItemInv = player.getInventory().main.stream()
      .filter(itemInv -> !itemInv.isEmpty() && ItemStack.canCombine(itemInv, product.getItemchance().getItemStack(amount)))
      .mapToInt(ItemStack::getCount)
      .sum();

    if (amountItemInv >= amount) {
      int remaining = amount;

      // Remueve los ítems del inventario del jugador
      for (ItemStack itemStack : player.getInventory().main) {
        if (!itemStack.isEmpty() && ItemStack.canCombine(itemStack, product.getItemchance().getItemStack(amount))) {
          int count = itemStack.getCount();

          if (count >= remaining) {
            itemStack.decrement(remaining);
            break;
          } else {
            remaining -= count;
            itemStack.setCount(0);
          }
        }
      }

      // Añade el precio calculado a la cuenta del jugador
      EconomyUtil.addMoney(player, getCurrency(), totalPrice);
      SoundUtil.playSound(CobbleUtils.shopLang.getSoundSell(), player);
      // Envía un mensaje de éxito al jugador
      player.sendMessage(
        AdventureTranslator.toNative(
          CobbleUtils.shopLang.getMessageSellSuccess()
            .replace("%prefix%", CobbleUtils.shopLang.getPrefix())
            .replace("%amount%", String.valueOf(amount))
            .replace("%amountproduct%", String.valueOf(product.getItemchance().getItemStack().getCount()))
            .replace("%total%", String.valueOf(amount * product.getItemchance().getItemStack().getCount()))
            .replace("%product%", ItemUtils.getTranslatedName(product.getItemchance().getItemStack()))
            .replace("%price%", EconomyUtil.formatCurrency(totalPrice, currency, player.getUuid()))
            .replace("%unitprice%", EconomyUtil.formatCurrency(unitPrice, currency, player.getUuid()))
            .replace("%sell%", EconomyUtil.formatCurrency(totalPrice, currency, player.getUuid()))
            .replace("%currency%", getCurrency())
            .replace("%symbol%", EconomyUtil.getSymbol(getCurrency()))
            .replace("%balance%", EconomyUtil.getBalance(player, getCurrency(), EconomyUtil.getDecimals(getCurrency())))
        )
      );
      return true;
    } else {
      // Envía un mensaje de error si el jugador no tiene la cantidad suficiente
      SoundUtil.playSound(CobbleUtils.shopLang.getSoundError(), player);
      player.sendMessage(
        AdventureTranslator.toNative(
          CobbleUtils.shopLang.getMessageSellError()
            .replace("%prefix%", CobbleUtils.shopLang.getPrefix())
            .replace("%amount%", String.valueOf(amount))
            .replace("%amountproduct%", String.valueOf(product.getItemchance().getItemStack().getCount()))
            .replace("%total%", String.valueOf(amount * product.getItemchance().getItemStack().getCount()))
            .replace("%product%", ItemUtils.getTranslatedName(product.getItemchance().getItemStack()))
            .replace("%price%", EconomyUtil.formatCurrency(totalPrice, currency, player.getUuid()))
            .replace("%unitprice%", EconomyUtil.formatCurrency(unitPrice, currency, player.getUuid()))
            .replace("%sell%", EconomyUtil.formatCurrency(totalPrice, currency, player.getUuid()))
            .replace("%currency%", getCurrency())
            .replace("%symbol%", EconomyUtil.getSymbol(getCurrency()))
            .replace("%balance%", EconomyUtil.getBalance(player, getCurrency(), EconomyUtil.getDecimals(getCurrency())))
        )
      );
    }
    return false;
  }


  private BigDecimal calculatePrice(Product product, TypeMenu typeMenu, int amount) {
    BigDecimal pricePerItem = (typeMenu == TypeMenu.BUY) ? product.getBuy() : product.getSell();
    return pricePerItem.multiply(BigDecimal.valueOf(amount));
  }


  private String generateTitle(Product product, TypeMenu typeMenu) {
    return (typeMenu == TypeMenu.BUY) ?
      CobbleUtils.shopLang.getTitleBuy().replace("%product%", ItemUtils.getTranslatedName(product.getItemchance().getItemStack())) :
      CobbleUtils.shopLang.getTitleSell().replace("%product%", ItemUtils.getTranslatedName(product.getItemchance().getItemStack()));
  }

  private void createAmountButton(ShopMenu shopMenu, ChestTemplate template, ServerPlayerEntity player,
                                  Product product, TypeMenu typeMenu,
                                  ItemModel addModel, ItemModel removeModel,
                                  int increment, int amount) {
    if (product.getItemchance().getType() == ItemChance.ItemChanceType.MONEY
      || product.getItemchance().getType() == ItemChance.ItemChanceType.COMMAND) return;
    template.set(addModel.getSlot(), GooeyButton.builder()
      .display(addModel.getItemStack(increment))
      .title(AdventureTranslator.toNative(addModel.getDisplayname()))
      .lore(Text.class, AdventureTranslator.toNativeL(addModel.getLore()))
      .onClick(action -> {
        SoundUtil.playSound(CobbleUtils.shopLang.getSoundAdd(), player);
        openBuySellMenu(shopMenu, player, product, typeMenu, amount + increment);
      })
      .build());


    template.set(removeModel.getSlot(), GooeyButton.builder()
      .display(removeModel.getItemStack(increment))
      .title(AdventureTranslator.toNative(removeModel.getDisplayname()))
      .lore(Text.class, AdventureTranslator.toNativeL(removeModel.getLore()))
      .onClick(action -> {
        SoundUtil.playSound(CobbleUtils.shopLang.getSoundRemove(), player);
        openBuySellMenu(shopMenu, player, product, typeMenu, Math.max(amount - increment, 1));
      })
      .build());


  }

  private void createViewProductButton(ShopMenu shopMenu, ServerPlayerEntity player, ChestTemplate template, Product product,
                                       TypeMenu typeMenu,
                                       int amount,
                                       BigDecimal price, String symbol) {
    List<String> loreProduct = new ArrayList<>(CobbleUtils.shopLang.getLoreProduct());
    if (typeMenu == TypeMenu.SELL) {
      loreProduct.removeIf(s -> s.contains("%buy%"));
    } else {
      loreProduct.removeIf(s -> s.contains("%sell%"));
    }
    loreProduct.replaceAll(s -> s
      .replace("%buy%", EconomyUtil.formatCurrency(price, currency, player.getUuid()))
      .replace("%sell%", EconomyUtil.formatCurrency(price, currency, player.getUuid()))
      .replace("%currency%", getCurrency())
      .replace("%symbol%", symbol)
      .replace("%amount%", String.valueOf(amount == 0 ? 1 : amount))
      .replace("%amountproduct%", String.valueOf(product.getItemchance().getItemStack().getCount()))
      .replace("%total%", String.valueOf((amount == 0 ? 1 : amount) * product.getItemchance().getItemStack().getCount()))
      .replace("%balance%", EconomyUtil.getBalance(player, getCurrency(), EconomyUtil.getDecimals(getCurrency())))
    );

    ItemStack viewProduct = getViewItemStack(product, (amount == 0 ? 1 : amount));

    viewProduct.setCount((amount == 0 ? 1 : amount));
    template.set(CobbleUtils.shopConfig.getSlotViewProduct(), GooeyButton.builder()
      .display(viewProduct)
      .title(AdventureTranslator.toNative(ItemUtils.getTranslatedName(viewProduct)))
      .lore(Text.class, AdventureTranslator.toNativeL(loreProduct))
      .onClick(action -> {
        TypeError typeError = getTypeError(product, player);
        if (typeError == TypeError.NONE) {
          SoundUtil.playSound(CobbleUtils.shopLang.getSoundOpen(), player);
          if (action.getClickType() == ButtonClick.LEFT_CLICK || action.getClickType() == ButtonClick.SHIFT_LEFT_CLICK) {
            if (product.getBuy().compareTo(BigDecimal.ZERO) > 0) {
              SoundUtil.playSound(getSoundopen(), player);
              openBuySellMenu(shopMenu, player, product, TypeMenu.BUY, 0);
            }
          } else if (action.getClickType() == ButtonClick.RIGHT_CLICK || action.getClickType() == ButtonClick.SHIFT_RIGHT_CLICK) {
            if (product.getSell().compareTo(BigDecimal.ZERO) > 0) {
              SoundUtil.playSound(getSoundopen(), player);
              openBuySellMenu(shopMenu, player, product, TypeMenu.SELL, 0);
            }
          }
        } else {
          sendError(player, typeError);
        }
      })
      .build());
  }

  private void createCancelButton(ChestTemplate template, ServerPlayerEntity player) {
    ItemModel cancel = CobbleUtils.shopLang.getCancel();
    template.set(cancel.getSlot(), cancel.getButton(action -> open(player, CobbleUtils.shopConfig.getShop())));
  }

  private ItemStack getViewItemStack(Product product, int amount) {
    ItemStack viewProduct;
    ItemChance.ItemChanceType itemChanceType = product.getItemchance().getType();
    if (itemChanceType == ItemChance.ItemChanceType.COMMAND
      || itemChanceType == ItemChance.ItemChanceType.MONEY) {
      viewProduct = product.getItemchance().getItemStack();

      if (product.getDisplay() != null && !product.getDisplay().isEmpty()) {
        viewProduct = new ItemChance(product.getDisplay(), 100).getItemStack();
      }

      if (product.getDisplayname() != null && !product.getDisplayname().isEmpty()) {
        viewProduct.setCustomName(
          AdventureTranslator.toNative(
            product.getDisplayname()
          )
        );
      }
    } else {
      viewProduct = product.getItemchance().getItemStack();
    }
    viewProduct.setCount(amount);
    return viewProduct;
  }

  private void createCloseButton(ChestTemplate template, ServerPlayerEntity player) {
    template.set((CobbleUtils.shopConfig.getRowsBuySellMenu() * 9) - 5, UIUtils.getCloseButton(action -> open(player, CobbleUtils.shopConfig.getShop())));
  }

}
