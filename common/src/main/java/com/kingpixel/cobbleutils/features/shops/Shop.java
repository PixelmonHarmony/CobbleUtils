package com.kingpixel.cobbleutils.features.shops;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.ButtonClick;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.TemplateType;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemChance;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.config.ShopConfig;
import com.kingpixel.cobbleutils.features.shops.models.Product;
import com.kingpixel.cobbleutils.features.shops.models.types.ShopType;
import com.kingpixel.cobbleutils.features.shops.models.types.ShopTypeDynamic;
import com.kingpixel.cobbleutils.features.shops.models.types.ShopTypeDynamicWeekly;
import com.kingpixel.cobbleutils.util.*;
import lombok.*;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 02/08/2024 9:25
 */
@Getter
@Setter
@EqualsAndHashCode
@Data
@ToString
public class Shop {
  private boolean active;
  private String id;
  private String title;
  private String currency;
  private short rows;
  private short slotbalance;
  private ItemModel previous;
  private int slotPrevious;
  private List<Integer> slotsPrevious;
  private String closeCommand;
  private ItemModel close;
  private int slotClose;
  private List<Integer> slotsClose;
  private ItemModel next;
  private int slotNext;
  private List<Integer> slotsNext;
  private int globalDiscount;
  private String soundopen;
  private String soundclose;
  private String colorItem;
  private ShopType shopType;
  private TemplateType templateType;
  //private ItemModel money;
  private Rectangle rectangle;
  private ItemModel display;
  private List<Product> products;
  private List<FillItems> fillItems;

  public Shop(String id, String title, ShopType shopType, short rows, List<String> lore) {
    this.active = true;
    this.id = id;
    this.title = title;
    this.rows = rows;
    this.slotbalance = 47;
    this.slotNext = 53;
    this.slotsNext = List.of();
    this.slotPrevious = 45;
    this.slotsPrevious = List.of();
    this.slotClose = 49;
    this.slotsClose = List.of();
    this.soundopen = "cobblemon:pc.on";
    this.soundclose = "cobblemon:pc.off";
    this.currency = "dollars";
    this.templateType = TemplateType.CHEST;
    this.rectangle = new Rectangle();
    this.shopType = shopType;
    this.colorItem = "<#6bd68f>";
    this.closeCommand = "";
    this.globalDiscount = 0;
    this.display = new ItemModel("cobblemon:poke_ball");
    display.setDisplayname(title);
    display.setLore(lore);
    this.products = getDefaultProducts();
    this.fillItems = new ArrayList<>();
    this.fillItems.add(new FillItems());
    switch (shopType.getTypeShop()) {
      case DYNAMIC:

        break;
      case DYNAMIC_WEEKLY:

        break;
      case WEEKLY:
        break;
      default:

        break;
    }
  }

  @Getter
  @Setter
  @EqualsAndHashCode
  @Data
  @ToString
  private static class Rectangle {
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

    public int getSlotsFree(int rows) {
      int totalSlots = rows * 9;
      int occupiedSlots = length * width;

      int startSlot = (startRow * 9) + startColumn; // Calcula el índice inicial
      int endSlot = startSlot + occupiedSlots - 1;  // Calcula el índice final ocupado

      if (endSlot >= totalSlots) {
        endSlot = totalSlots - 1;
      }

      int actualOccupiedSlots = endSlot - startSlot + 1; // Slots realmente ocupados

      return totalSlots - actualOccupiedSlots;
    }

  }


  private List<Product> getDefaultProducts() {
    List<Product> products = new ArrayList<>();
    products.add(new Product());
    products.add(new Product(true));
    return products;
  }


  @EqualsAndHashCode(callSuper = true)
  @Getter
  @Setter
  @Data
  @ToString
  public static class FillItems extends ItemModel {
    private List<Integer> slots;

    public FillItems() {
      super("minecraft:gray_stained_glass_pane");
      slots = new ArrayList<>();
    }
  }

  public void open(ServerPlayerEntity player, ShopConfig shopConfig, String mod_id, boolean byCommand) {
    if (!LuckPermsUtil.checkPermission(player, mod_id + ".shop." + this.getId())) {
      player.sendMessage(
        AdventureTranslator.toNative(
          CobbleUtils.shopLang.getMessageNotHavePermission()
            .replace("%prefix%", CobbleUtils.shopLang.getPrefix())
        )
      );
      return;
    }
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
        BigDecimal buy = product.getBuy();
        BigDecimal sell = product.getSell();


        TypeError typeError = getTypeError(product, player);


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
          .title(AdventureTranslator.toNative(getTitleItem(product)))
          .lore(Text.class, AdventureTranslator.toNativeL(getLoreProduct(
            buy, sell, product, player, symbol, typeError, BigDecimal.ONE
          )))
          .onClick(action -> {
            if (typeError == TypeError.NONE) {
              if (product.getPermission() != null) {
                if (LuckPermsUtil.checkPermission(player, product.getPermission()) && product.getNotCanBuyWithPermission() != null && product.getNotCanBuyWithPermission())
                  return;
              }
              switch (getShopAction(product)) {
                case BUY:
                  if (buy.compareTo(BigDecimal.ZERO) > 0) {
                    SoundUtil.playSound(getSoundopen(), player);
                    openBuySellMenu(player, shopConfig, product, TypeMenu.BUY, defaultamount, mod_id, byCommand);
                  }
                  break;
                case SELL:
                  if (sell.compareTo(BigDecimal.ZERO) > 0) {
                    SoundUtil.playSound(getSoundopen(), player);
                    openBuySellMenu(player, shopConfig, product, TypeMenu.SELL, defaultamount, mod_id, byCommand);
                  }
                  break;
                case BUY_SELL:
                  if (action.getClickType() == ButtonClick.LEFT_CLICK || action.getClickType() == ButtonClick.SHIFT_LEFT_CLICK) {
                    if (buy.compareTo(BigDecimal.ZERO) > 0) {
                      SoundUtil.playSound(getSoundopen(), player);
                      openBuySellMenu(player, shopConfig, product, TypeMenu.BUY, defaultamount, mod_id, byCommand);
                    }
                  } else if (action.getClickType() == ButtonClick.RIGHT_CLICK || action.getClickType() == ButtonClick.SHIFT_RIGHT_CLICK) {
                    if (sell.compareTo(BigDecimal.ZERO) > 0) {
                      SoundUtil.playSound(getSoundopen(), player);
                      openBuySellMenu(player, shopConfig, product, TypeMenu.SELL, defaultamount, mod_id, byCommand);
                    }
                  }
                  break;
                default:
                  sendError(player, typeError);
                  break;
              }
            } else {
              sendError(player, typeError);
            }
          })
          .build();

        buttons.add(button);
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

      if (slotbalance > 0 && slotbalance < 54) {
        template.set(this.slotbalance, GooeyButton.builder()
          .display(balance.getItemStack())
          .title(AdventureTranslator.toNative(balance.getDisplayname()))
          .lore(Text.class, AdventureTranslator.toNativeL(lorebalance))
          .build());

      }

      // Display
      int slotfree = rectangle.getSlotsFree(rows);

      if (!byCommand) {
        GooeyButton close = getClose().getButton(action -> {
          if (closeCommand == null || closeCommand.isEmpty()) {
            ShopConfigMenu.open(player, shopConfig, mod_id, byCommand);
          } else {
            PlayerUtils.executeCommand(closeCommand, player);
          }
        });
        template.set(getSlotClose(), close);
        if (slotsClose != null && !slotsClose.isEmpty())
          slotsClose.forEach(slot -> template.set(slot, close));
      }

      if (slotfree - products.size() < 0) {
        LinkedPageButton next = LinkedPageButton.builder()
          .display(getNext().getItemStack())
          .title(AdventureTranslator.toNative(getNext().getDisplayname()))
          .onClick(action -> {
            SoundUtil.playSound(getSoundopen(), action.getPlayer());
          })
          .linkType(LinkType.Next)
          .build();
        template.set(getSlotNext(), next);
        if (slotsNext != null && !slotsNext.isEmpty())
          slotsNext.forEach(slot -> template.set(slot, next));

        LinkedPageButton previous = LinkedPageButton.builder()
          .display(getPrevious().getItemStack())
          .title(AdventureTranslator.toNative(getPrevious().getDisplayname()))
          .onClick(action -> {
            SoundUtil.playSound(getSoundopen(), action.getPlayer());
          })
          .linkType(LinkType.Previous)
          .build();
        template.set(getSlotPrevious(), previous);
        if (slotsPrevious != null && !slotsPrevious.isEmpty())
          slotsPrevious.forEach(slot -> template.set(slot, previous));
      }

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

  public enum ShopAction {
    BUY, SELL, BUY_SELL
  }

  public ShopAction getShopAction(Product product) {
    if (product.getBuy().compareTo(BigDecimal.ZERO) > 0 && product.getSell().compareTo(BigDecimal.ZERO) > 0) {
      return ShopAction.BUY_SELL;
    } else if (product.getBuy().compareTo(BigDecimal.ZERO) > 0) {
      return ShopAction.BUY;
    } else if (product.getSell().compareTo(BigDecimal.ZERO) > 0) {
      return ShopAction.SELL;
    }
    return null;
  }

  private List<String> getLoreProduct(BigDecimal buy, BigDecimal sell, Product product, ServerPlayerEntity player,
                                      String symbol, TypeError typeError, BigDecimal amount) {
    List<String> lore = new ArrayList<>(CobbleUtils.shopLang.getLoreProduct());

    if (product.getLore() != null && !product.getLore().isEmpty()) {
      lore.addAll(product.getLore());
    }

    if (buy.compareTo(BigDecimal.ZERO) <= 0) {
      lore.removeIf(s -> s.contains("%buy%"));
      lore.removeIf(s -> s.contains("%removebuy%"));
    }

    if (sell.compareTo(BigDecimal.ZERO) <= 0) {
      lore.removeIf(s -> s.contains("%sell%"));
      lore.removeIf(s -> s.contains("%removesell%"));
    }

    int discount = getDiscount(product);

    String priceWithoutDiscount = EconomyUtil.formatCurrency(calculatePrice(product, TypeMenu.BUY, amount, false),
      currency,
      player.getUuid());

    String priceDiscount = EconomyUtil.formatCurrency(calculatePrice(product, TypeMenu.BUY, amount, true), currency, player.getUuid());

    haveDiscount(product);

    lore.replaceAll(s -> s
      .replace("%buy%", haveDiscount(product) ? "&m" + priceWithoutDiscount + "&r &e" + priceDiscount :
        priceWithoutDiscount)
      .replace("%sell%", EconomyUtil.formatCurrency(calculatePrice(product, TypeMenu.SELL, amount, false), currency,
        player.getUuid()))
      .replace("%currency%", getCurrency())
      .replace("%symbol%", symbol)
      .replace("%amount%", amount.toString())
      .replace("%amountproduct%", String.valueOf(product.getItemchance().getItemStack().getCount()))
      .replace("%total%", String.valueOf((amount.compareTo(BigDecimal.ZERO) == 0) ? 1 : amount.multiply(BigDecimal.valueOf(product.getItemchance().getItemStack().getCount()))))
      .replace("%balance%", EconomyUtil.getBalance(player, getCurrency(), EconomyUtil.getDecimals(getCurrency())))
      .replace("%removebuy%", "")
      .replace("%removesell%", "")
      .replace("%discount%", (discount > 0) ? discount + "%" : "")

    );

    if (typeError == TypeError.PERMISSION) {
      lore = new ArrayList<>(lore);
      lore.add(CobbleUtils.shopLang.getNotPermission()
        .replace("%product%", ItemUtils.getTranslatedName(product.getItemchance().getItemStack())));
      lore.replaceAll(s ->
        s.replace("%description%", product.getLore() != null ? String.valueOf(product.getLore()) : "")
      );
    } else if (typeError != TypeError.NONE) {
      lore = new ArrayList<>();
      lore.add("&cError in this product");
      lore.add("&cContact the server administrator");
      lore.add(product.getItemchance().toString());
      lore.add("&cError: " + typeError.name());
    }

    return lore;
  }

  private boolean haveDiscount(Product product) {
    return ((product.getDiscount() != null && product.getDiscount() > 0) || globalDiscount > 0);
  }

  private int getDiscount(Product product) {
    int discount = product.getDiscount() != null && product.getDiscount() > 0 ? product.getDiscount() : this.globalDiscount;
    return discount;
  }

  private enum TypeMenu {
    BUY, SELL, STACK
  }

  private enum TypeError {
    INVALID_PRICE, ZERO, PERMISSION, BAD_DISCOUNT, INVALID_PRICE_WITH_DISCOUNT, NONE
  }

  private TypeError getTypeError(Product product, ServerPlayerEntity player) {
    BigDecimal globalDiscount = BigDecimal.valueOf(this.globalDiscount);
    if (product.getBuy().compareTo(BigDecimal.ZERO) <= 0 && product.getSell().compareTo(BigDecimal.ZERO) <= 0) {
      return TypeError.ZERO;
    } else if (!LuckPermsUtil.checkPermission(player, product.getPermission())) {
      return TypeError.PERMISSION;
    } else if (product.getBuy().compareTo(BigDecimal.ZERO) > 0 && product.getSell().compareTo(product.getBuy()) > 0) {
      return TypeError.INVALID_PRICE;
    } else if (product.getDiscount() != null && (product.getDiscount() > 100) || globalDiscount.compareTo(BigDecimal.valueOf(100)) > 0) {
      return TypeError.BAD_DISCOUNT;
    } else {
      if (product.getBuy().compareTo(BigDecimal.ZERO) == 0) return TypeError.NONE;


      BigDecimal applicableDiscount = product.getDiscount() != null && product.getDiscount() > 0 ?
        BigDecimal.valueOf(product.getDiscount()) :
        globalDiscount;


      BigDecimal discountedBuyPrice = product.getBuy().multiply(BigDecimal.ONE.subtract(applicableDiscount.divide(BigDecimal.valueOf(100))));

      if (product.getSell().compareTo(discountedBuyPrice) > 0) {
        return TypeError.INVALID_PRICE_WITH_DISCOUNT;
      }
    }

    return TypeError.NONE;
  }


  private void sendError(ServerPlayerEntity player, TypeError typeError) {
    switch (typeError) {
      case INVALID_PRICE:
        player.sendMessage(Text.literal("Buy price is higher than sell price contact the server administrator"));
        break;
      case ZERO:
        player.sendMessage(Text.literal("Buy and sell price is zero contact the server administrator"));
        break;
      case PERMISSION:
        player.sendMessage(Text.literal("You don't have permission to buy or sell this product"));
        break;
      case BAD_DISCOUNT:
        player.sendMessage(Text.literal("Discount is not valid contact the server administrator"));
        break;
      case INVALID_PRICE_WITH_DISCOUNT:
        player.sendMessage(Text.literal("Sell price is higher than buy price with discount contact the server administrator"));
        break;
      default:
        player.sendMessage(Text.literal("Error in this product contact the server administrator"));
        break;
    }
  }

  private void openBuySellMenu(ServerPlayerEntity player, ShopConfig shopConfig,
                               Product product, TypeMenu typeMenu,
                               int amount, String mod_id, boolean byCommand) {
    ChestTemplate template = ChestTemplate
      .builder(shopConfig.getShop().getRowsBuySellMenu())
      .build();

    int maxStack = product.getItemchance().getItemStack().getMaxCount();
    BigDecimal price = calculatePrice(product, typeMenu, BigDecimal.valueOf(amount), true);
    String title = generateTitle(product, typeMenu);
    String symbol = EconomyUtil.getSymbol(getCurrency());

    // Botones de cantidad
    if (maxStack != 1) {
      createAmountButton(shopConfig, template, player, product, typeMenu, CobbleUtils.shopLang.getAdd1(),
        CobbleUtils.shopLang.getRemove1(), 1, amount, mod_id, byCommand);

      if (maxStack == 16) {
        createAmountButton(shopConfig, template, player, product, typeMenu, CobbleUtils.shopLang.getAdd8(),
          CobbleUtils.shopLang.getRemove8(), 8, amount, mod_id, byCommand);
        createAmountButton(shopConfig, template, player, product, typeMenu, CobbleUtils.shopLang.getAdd16(),
          CobbleUtils.shopLang.getRemove16(), 16, amount, mod_id, byCommand);
      }
      if (maxStack == 64) {
        createAmountButton(shopConfig, template, player, product, typeMenu, CobbleUtils.shopLang.getAdd10(),
          CobbleUtils.shopLang.getRemove10(), 10, amount, mod_id, byCommand);
        createAmountButton(shopConfig, template, player, product, typeMenu, CobbleUtils.shopLang.getAdd64(),
          CobbleUtils.shopLang.getRemove64(), 64, amount, mod_id, byCommand);
      }
    }

    // Botón de ver producto
    createViewProductButton(shopConfig, mod_id, player, template, product, typeMenu, amount, price, symbol, byCommand);

    // Botón de confirmar
    ItemModel confirm = CobbleUtils.shopLang.getConfirm();
    template.set(confirm.getSlot(), confirm.getButton(action -> {

      int finalamount = (amount == 0) ? 1 : amount;
      if (typeMenu == TypeMenu.BUY) {
        if (buyProduct(player, product, finalamount, price)) {
          ShopTransactions.addTransaction(player.getUuid(), this, ShopTransactions.ShopAction.BUY, product, BigDecimal.valueOf(finalamount), price);
          open(player, shopConfig, mod_id, false);
          ShopTransactions.updateTransaction(player.getUuid(), shopConfig.getShop());
        }
      }
      if (typeMenu == TypeMenu.SELL) {
        if (sellProduct(player, product, finalamount)) {
          ShopTransactions.addTransaction(player.getUuid(), this, ShopTransactions.ShopAction.SELL, product,
            BigDecimal.valueOf(finalamount), price);
          open(player, shopConfig, mod_id, false);
          ShopTransactions.updateTransaction(player.getUuid(), shopConfig.getShop());
        }
      }

    }));

    // Botón de comprar pilas completas
    //createMaxStackButton(template, player, product, typeMenu, maxStack);

    // Botón de cancelar
    createCancelButton(template, shopConfig, mod_id, player, byCommand);

    // Relleno y botón de cerrar
    template.fill(GooeyButton.of(Utils.parseItemId(CobbleUtils.config.getFill())));

    createCloseButton(template, shopConfig, mod_id, player, byCommand);

    GooeyPage page = GooeyPage.builder()
      .title(AdventureTranslator.toNative(title))
      .template(template)
      .build();

    page.subscribe(amount, () -> {
      createViewProductButton(shopConfig, mod_id, player, template, product, typeMenu, amount, price, symbol, byCommand);
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

      if (itemChance.getType() == ItemChance.ItemChanceType.ITEM) {
        if (itemChance.getItem().startsWith("item:")) {
          itemsPerPackage = Integer.parseInt(itemChance.getItem().split(":")[1]);
        }
      }

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
      product.getSell().divide(BigDecimal.valueOf(packageSize), digits, RoundingMode.UNNECESSARY);
    BigDecimal totalPrice =
      unitPrice.multiply(BigDecimal.valueOf(amount));

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


  private BigDecimal calculatePrice(Product product, TypeMenu typeMenu, BigDecimal amount, boolean showDiscount) {

    // Verificamos si hay un descuento y si es un tipo de menú de compra (BUY)
    BigDecimal finalAmount = (amount.compareTo(BigDecimal.ZERO) <= 0) ? BigDecimal.ONE : amount;
    if (showDiscount) {
      int discount = getDiscount(product);
      if (discount > 0 && discount <= 100 && typeMenu == TypeMenu.BUY) {
        // Calcular el precio por artículo (multiplicando por la cantidad)
        BigDecimal pricePerItem = product.getBuy();
        // Calcular el monto del descuento
        BigDecimal discountAmount = pricePerItem.multiply(BigDecimal.valueOf(discount)).divide(BigDecimal.valueOf(100));
        // Calcular el precio final con el descuento aplicado y la cantidad
        return pricePerItem.subtract(discountAmount).multiply(finalAmount);
      }
    }

    // Si no hay descuento o no es una compra, usamos el precio según el tipo de menú
    BigDecimal pricePerItem = (typeMenu == TypeMenu.BUY) ? product.getBuy() : product.getSell();
    return pricePerItem.multiply(finalAmount);
  }


  private String generateTitle(Product product, TypeMenu typeMenu) {
    return (typeMenu == TypeMenu.BUY) ?
      CobbleUtils.shopLang.getTitleBuy().replace("%product%", ItemUtils.getTranslatedName(product.getItemchance().getItemStack())) :
      CobbleUtils.shopLang.getTitleSell().replace("%product%", ItemUtils.getTranslatedName(product.getItemchance().getItemStack()));
  }

  private void createAmountButton(ShopConfig shopConfig, ChestTemplate template, ServerPlayerEntity player,
                                  Product product, TypeMenu typeMenu,
                                  ItemModel addModel, ItemModel removeModel,
                                  int increment, int amount,
                                  String mod_id, boolean byCommand) {
    if (product.getItemchance().getType() == ItemChance.ItemChanceType.MONEY || product.getItemchance().getType() == ItemChance.ItemChanceType.COMMAND)
      return;
    template.set(addModel.getSlot(), GooeyButton.builder()
      .display(addModel.getItemStack(increment))
      .title(AdventureTranslator.toNative(addModel.getDisplayname()))
      .lore(Text.class, AdventureTranslator.toNativeL(addModel.getLore()))
      .onClick(action -> {
        SoundUtil.playSound(CobbleUtils.shopLang.getSoundAdd(), player);
        openBuySellMenu(player, shopConfig, product, typeMenu, amount + increment, mod_id, byCommand);
      })
      .build());


    template.set(removeModel.getSlot(), GooeyButton.builder()
      .display(removeModel.getItemStack(increment))
      .title(AdventureTranslator.toNative(removeModel.getDisplayname()))
      .lore(Text.class, AdventureTranslator.toNativeL(removeModel.getLore()))
      .onClick(action -> {
        SoundUtil.playSound(CobbleUtils.shopLang.getSoundRemove(), player);
        openBuySellMenu(player, shopConfig, product, typeMenu, Math.max(amount - increment, 1), mod_id, byCommand);
      })
      .build());


  }

  private void createViewProductButton(ShopConfig shopConfig, String mod_id,
                                       ServerPlayerEntity player, ChestTemplate template,
                                       Product product, TypeMenu typeMenu,
                                       int amount, BigDecimal price,
                                       String symbol, boolean byCommand) {


    ItemStack viewProduct = getViewItemStack(product, (amount == 0 ? 1 : amount));

    BigDecimal buy;
    BigDecimal sell;

    if (typeMenu == TypeMenu.BUY) {
      buy = price;
      sell = BigDecimal.ZERO;
    } else if (typeMenu == TypeMenu.SELL) {
      buy = BigDecimal.ZERO;
      sell = price;
    } else {
      buy = product.getBuy();
      sell = product.getSell();
    }

    viewProduct.setCount((amount == 0 ? 1 : amount));
    template.set(shopConfig.getShop().getSlotViewProduct(), GooeyButton.builder()
      .display(viewProduct)
      .title(AdventureTranslator.toNative(getTitleItem(product)))
      .lore(Text.class, AdventureTranslator.toNativeL(getLoreProduct(
        buy, sell,
        product, player,
        symbol, TypeError.NONE,
        BigDecimal.valueOf(amount)
      )))
      .onClick(action -> {
        TypeError typeError = getTypeError(product, player);
        if (typeError == TypeError.NONE) {
          SoundUtil.playSound(CobbleUtils.shopLang.getSoundOpen(), player);
          if (action.getClickType() == ButtonClick.LEFT_CLICK || action.getClickType() == ButtonClick.SHIFT_LEFT_CLICK) {
            if (product.getBuy().compareTo(BigDecimal.ZERO) > 0) {
              SoundUtil.playSound(getSoundopen(), player);
              openBuySellMenu(player, shopConfig, product, TypeMenu.BUY, 0, mod_id, byCommand);
            }
          } else if (action.getClickType() == ButtonClick.RIGHT_CLICK || action.getClickType() == ButtonClick.SHIFT_RIGHT_CLICK) {
            if (product.getSell().compareTo(BigDecimal.ZERO) > 0) {
              SoundUtil.playSound(getSoundopen(), player);
              openBuySellMenu(player, shopConfig, product, TypeMenu.SELL, 0, mod_id, byCommand);
            }
          }
        } else {
          sendError(player, typeError);
        }
      })
      .build());
  }

  private String getTitleItem(Product product) {
    String titleItem;

    if (product.getDisplayname() != null && !product.getDisplayname().isEmpty()) {
      titleItem = product.getDisplayname();
    } else {
      if (product.getColor() == null || product.getColor().isEmpty()) {
        titleItem =
          (this.colorItem == null ? "" : this.colorItem) + ItemUtils.getTranslatedName(product.getItemchance().getItemStack());
      } else {
        titleItem = product.getColor() + ItemUtils.getTranslatedName(product.getItemchance().getItemStack());
      }
    }
    return titleItem;
  }

  private void createCancelButton(ChestTemplate template, ShopConfig shopConfig,
                                  String mod_id, ServerPlayerEntity player, boolean byCommand) {
    ItemModel cancel = CobbleUtils.shopLang.getCancel();
    template.set(cancel.getSlot(), cancel.getButton(action -> open(player, shopConfig, mod_id, byCommand)));
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

  private void createCloseButton(ChestTemplate template, ShopConfig shopConfig,
                                 String mod_id, ServerPlayerEntity player, boolean byCommand) {
    template.set((shopConfig.getShop().getRowsBuySellMenu() * 9) - 5, UIUtils.getCloseButton(action -> open(player,
      shopConfig, mod_id, byCommand)));
  }

}
