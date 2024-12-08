package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.ButtonAction;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.cobbleutils.features.shops.Shop;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import lombok.Getter;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Carlos Varas Alonso - 21/11/2024 3:15
 */
@Getter
public class AdvancedItemChance {
  // Title of the menu rewards
  private String title;
  // Options for the rewards
  private boolean giveAll;
  private int amountReward;
  private Map<String, Integer> amountRewardsPermission;
  // Effects and animations
  private Sound newSound;
  private Particle particle;
  private Animations animation;
  // Rewards

  private Map<String, List<ItemChance>> lootTable;

  public AdvancedItemChance() {
    this.title = "";
    this.giveAll = false;
    this.amountReward = 3;
    this.amountRewardsPermission = Map.of(
      "", amountReward,
      "group.vip", 5
    );
    //this.sound = "minecraft:block.note_block.harp";
    this.newSound = new Sound();
    this.particle = new Particle();
    this.animation = Animations.NONE;
    this.lootTable = Map.of(
      "", ItemChance.defaultItemChances(),
      "group.vip", List.of(
        new ItemChance()
      )
    );
  }

  private enum TypeError {
    NONE, AMOUNTREWARD, LOOTTABLE
  }

  public boolean checker(ServerPlayerEntity player) {
    boolean error = false;
    TypeError typeError = TypeError.NONE;

    if (amountReward < 1) {
      error = true;
      typeError = TypeError.AMOUNTREWARD;
    }

    if (lootTable == null || lootTable.isEmpty()) {
      error = true;
      typeError = TypeError.LOOTTABLE;
    }

    switch (typeError) {
      case AMOUNTREWARD:
        PlayerUtils.sendMessage(player,
          "%prefix% &cplease notify the administrator of the error in the configuration in the amountReward",
          "&7[&cERROR&7]");
        break;
      case LOOTTABLE:
        PlayerUtils.sendMessage(player,
          "%prefix% &cplease notify the administrator of the error in the configuration in the lootTable",
          "&7[&cERROR&7]");
        break;
      default:
        break;
    }

    if (error) {
      PlayerUtils.sendMessage(player,
        "%prefix% please notify the administrator of the error in the configuration",
        "[ERROR]");
    }
    return error;
  }

  private int getAmountReward(ServerPlayerEntity player) {
    if (amountRewardsPermission == null || amountRewardsPermission.isEmpty()) return amountReward;

    int amount = amountReward;
    for (Map.Entry<String, Integer> entry : amountRewardsPermission.entrySet()) {
      if (PermissionApi.hasPermission(player, entry.getKey(), 2)) {
        if (entry.getValue() > amount) {
          amount = entry.getValue();
        }
      }
    }
    return amount;
  }

  public void giveRewards(ServerPlayerEntity player) {
    if (checker(player)) return;
    List<ItemChance> rewards = getList(player);

    if (giveAll) {
      ItemChance.getAllRewards(rewards, player);
    } else {
      rewards = ItemChance.getRewards(rewards, player, getAmountReward(player));
    }

    if (getNewSound() != null) {
      //SoundUtil.playSound(sound, player);
      getNewSound().start(player);
    }

    if (particle != null) {
      particle.sendParticles(player, player);
    }


    if (CobbleUtils.config.isDebug() && animation != null) {
      List<ItemStack> showRewards = getListDisplay(rewards);
      switch (animation) {
        case CSGO:
          csgoAnimation(player, showRewards);
          break;
        case VISUALITEMS:
          visualItemsAnimation(player, showRewards);
          break;
        case TOTEM:
          totemAnimation(player, showRewards);
          break;
        default:
          break;
      }
    }
  }

  private enum Animations {
    NONE, // No animation
    VISUALITEMS, // Show the won items in front of the user
    CSGO, // Show the items in a CSGO style
    TOTEM // Show the items in a totem style
  }

  // Animations methods

  // TODO: Implement the animation of totem showing the items in front of the player
  private void totemAnimation(ServerPlayerEntity player, List<ItemStack> rewards) {
    // Iterate through the rewards and display them in front of the player
    List<ItemEntity> itemEntities = new ArrayList<>();
    for (ItemStack itemStack : rewards) {
      // Create an item entity to display the item
      ItemEntity itemEntity = new ItemEntity(player.getWorld(), player.getX(), player.getY() + 1, player.getZ(), itemStack);
      // Set the item entity to be stationary
      itemEntity.setVelocity(0, 0, 0);
      itemEntity.setNoGravity(true);
      itemEntity.setPickupDelay(32767);

      itemEntities.add(itemEntity);
    }

    itemEntities.forEach(itemEntity -> {
      // Spawn the item entity in the world
      if (player.getWorld().spawnEntity(itemEntity)) {
        if (CobbleUtils.config.isDebug()) {
          CobbleUtils.LOGGER.info("Item entity spawned");
        }
      } else {
        if (CobbleUtils.config.isDebug()) {
          CobbleUtils.LOGGER.info("Item entity not spawned");
        }
      }
    });
  }


  private void visualItemsAnimation(ServerPlayerEntity player, List<ItemStack> rewards) {
  }

  private void csgoAnimation(ServerPlayerEntity player, List<ItemStack> rewards) {
  }


  // Menus methods

  public void openMenu(ServerPlayerEntity player, Consumer<ChestTemplate> templateConsumer) {
    ChestTemplate template = ChestTemplate.builder(6)
      .build();

    ItemModel itemClose = CobbleUtils.language.getItemClose();
    template.set(49, itemClose.getButton(action -> {
      UIManager.closeUI(player);
    }));
    templateConsumer.accept(template);
    applyTemplate(player, template);
  }

  public void openMenu(ServerPlayerEntity player, Consumer<ChestTemplate> templateConsumer,
                       Consumer<ButtonAction> close) {
    ChestTemplate template = ChestTemplate.builder(6)
      .build();
    ItemModel itemClose = CobbleUtils.language.getItemClose();
    template.set(49, itemClose.getButton(close));
    templateConsumer.accept(template);
    applyTemplate(player, template);
  }

  @Deprecated
  public void openMenu(ServerPlayerEntity player) {
    ChestTemplate template = ChestTemplate.builder(6)
      .build();

    applyTemplate(player, template);
  }

  private void applyTemplate(ServerPlayerEntity player, ChestTemplate template) {
    List<Button> buttons = getButtons(player);

    Shop.Rectangle rectangle = new Shop.Rectangle(1, 1, 4, 7);
    rectangle.apply(template);

    ItemModel info = CobbleUtils.language.getItemAdvancedRewardsInfo();
    List<String> infoLore = new ArrayList<>(info.getLore());
    infoLore.replaceAll(s -> s
      .replace("%amount%", String.valueOf(getAmountReward(player)))
      .replace("%getall%", giveAll ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo()));

    if (info.getSlot() >= 0) {
      template.set(info.getSlot(), GooeyButton.builder()
        .display(info.getItemStack())
        .lore(Text.class, AdventureTranslator.toNativeL(infoLore))
        .build());
    }

    ItemModel itemPrevious = CobbleUtils.language.getItemPrevious();
    template.set(45, LinkedPageButton.builder()
      .display(itemPrevious.getItemStack())
      .linkType(LinkType.Previous)
      .build());

    ItemModel itemNext = CobbleUtils.language.getItemNext();
    template.set(53, LinkedPageButton.builder()
      .display(itemNext.getItemStack())
      .linkType(LinkType.Next)
      .build());

    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder()
      .title(AdventureTranslator.toNative(title == null || title.isEmpty() ? CobbleUtils.language.getTitleLoot() : title));

    UIManager.openUIForcefully(player, PaginationHelper.createPagesFromPlaceholders(template, buttons,
      linkedPageBuilder));
  }

  public List<Button> getButtons(ServerPlayerEntity player) {
    List<Button> buttons = new ArrayList<>();
    double totalWeight = getList(null).stream().mapToDouble(ItemChance::getChance).sum();
    lootTable.forEach((key, value) -> {
      value.forEach(itemChance -> {
        boolean hasPermission = PermissionApi.hasPermission(player, key, 2);
        double chance = hasPermission ? itemChance.getChance() : 0;
        buttons.add(getButton(itemChance, key, chance, hasPermission, totalWeight));
      });
    });
    return buttons;
  }

  private GooeyButton getButton(ItemChance itemChance, String permission, double chance, boolean havePermission,
                                double totalWeight) {
    // Calcula el porcentaje basado en el peso total
    double percentage = totalWeight > 0 ? (chance / totalWeight) * 100 : 0;

    // Prepara el lore para mostrar el porcentaje calculado
    List<String> lore = new ArrayList<>(CobbleUtils.language.getLorechance());
    lore.replaceAll(s -> s.replace("%chance%", String.format("%.2f", percentage)));
    if (!havePermission) {
      lore.add(CobbleUtils.language.getMessagePermissionRewards()
        .replace("%permission%", permission));
    }


    return GooeyButton.builder()
      .display(getDisplay(itemChance))
      .title(AdventureTranslator.toNative(ItemChance.getTitle(itemChance)))
      .lore(Text.class, AdventureTranslator.toNativeL(lore))
      .build();
  }

  private ItemStack getDisplay(ItemChance itemChance) {
    ItemStack display = itemChance.getItemStack();
    if (itemChance.getDisplay() != null && !itemChance.getDisplay().isEmpty())
      display = new ItemChance(itemChance.getDisplay(), 100).getItemStack();
    return display;
  }

  private List<ItemStack> getListDisplay(List<ItemChance> itemChances) {
    List<ItemStack> itemStacks = new ArrayList<>();
    itemChances.forEach(itemChance -> {
      itemStacks.add(getDisplay(itemChance));
    });
    return itemStacks;
  }


  private List<ItemChance> getList(ServerPlayerEntity player) {
    List<ItemChance> itemChances = new ArrayList<>();
    lootTable.forEach((key, value) -> {
      if (PermissionApi.hasPermission(player, key, 2)) {
        itemChances.addAll(value);
      }
    });
    return itemChances;
  }

}


