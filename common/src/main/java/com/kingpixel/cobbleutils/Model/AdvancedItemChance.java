package com.kingpixel.cobbleutils.Model;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
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
import com.kingpixel.cobbleutils.util.SoundUtil;
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
  private String title;
  private String sound;
  private int amountReward;
  private boolean giveAll;
  private Particle particle;
  private Animations animation;
  private Map<String, List<ItemChance>> lootTable;

  public AdvancedItemChance() {
    this.title = "";
    this.sound = "";
    this.amountReward = 3;
    this.giveAll = false;
    this.particle = new Particle();
    this.animation = Animations.NONE;
    this.lootTable = Map.of(
      "", ItemChance.defaultItemChances(),
      "group.vip", List.of(
        new ItemChance()
      )
    );
  }

  public AdvancedItemChance defaultAdvancedItemChances() {
    this.lootTable = Map.of(
      "", ItemChance.defaultItemChances(),
      "group.vip", List.of(
        new ItemChance()
      )
    );
    return this;
  }

  public boolean checker(ServerPlayerEntity player) {
    boolean error = false;
    if (amountReward < 1) {
      error = true;
    }

    if (lootTable == null || lootTable.isEmpty()) {
      error = true;
    }

    if (error) {
      PlayerUtils.sendMessage(player,
        "%prefix% please notify the administrator of the error in the configuration",
        "[ERROR]");
    }
    return error;
  }

  public void giveRewards(ServerPlayerEntity player) {
    if (checker(player)) return;
    List<ItemChance> rewards = getList(player);

    if (giveAll) {
      ItemChance.getAllRewards(rewards, player);
    } else {
      ItemChance.getRewards(rewards, player, amountReward);
    }

    if (sound != null && !sound.isEmpty()) {
      SoundUtil.playSound(SoundUtil.getSound(sound), player);
    }

    if (particle != null) {
      particle.sendParticles(player, player);
    }


    if (CobbleUtils.config.isDebug()) {
      switch (animation) {
        case CSGO:
          csgoAnimation(player, rewards);
          break;
        case VISUALITEMS:
          visualItemsAnimation(player, rewards);
          break;
        case TOTEM:
          totemAnimation(player, rewards);
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

  private void totemAnimation(ServerPlayerEntity player, List<ItemChance> rewards) {
    // Iterate through the rewards and display them in front of the player
    List<ItemEntity> itemEntities = new ArrayList<>();
    for (ItemChance reward : rewards) {
      ItemStack itemStack = reward.getItemStack();
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


  private void visualItemsAnimation(ServerPlayerEntity player, List<ItemChance> rewards) {
  }

  private void csgoAnimation(ServerPlayerEntity player, List<ItemChance> rewards) {
  }

  private List<ItemStack> getItemStacks(List<ItemChance> itemChances) {
    List<ItemStack> itemStacks = new ArrayList<>();
    itemChances.forEach(itemChance -> itemStacks.add(itemChance.getItemStack()));
    return itemStacks;
  }


  public void openMenu(ServerPlayerEntity player, Consumer<ChestTemplate> templateConsumer) {
    ChestTemplate template = ChestTemplate.builder(6).build();

    List<Button> buttons = getButtons(player);

    template.rectangle(0, 0, 5, 9, new PlaceholderButton());

    ItemModel itemPrevious = CobbleUtils.language.getItemPrevious();
    template.set(45, LinkedPageButton.builder()
      .display(itemPrevious.getItemStack())
      .linkType(LinkType.Previous)
      .build());

    ItemModel itemClose = CobbleUtils.language.getItemClose();
    template.set(49, itemClose.getButton(action -> {
      UIManager.closeUI(player);
    }));

    ItemModel itemNext = CobbleUtils.language.getItemNext();
    template.set(53, LinkedPageButton.builder()
      .display(itemNext.getItemStack())
      .linkType(LinkType.Next)
      .build());

    // Apply additional modifications to the template
    templateConsumer.accept(template);

    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder()
      .title(AdventureTranslator.toNative(title == null || title.isEmpty() ? CobbleUtils.language.getTitleLoot() : title));

    UIManager.openUIForcefully(player, PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder));
  }


  @Deprecated
  public void openMenu(ServerPlayerEntity player) {
    ChestTemplate template = ChestTemplate.builder(6)
      .build();

    List<Button> buttons = getButtons(player);


    new Shop.Rectangle(6).apply(template);

    ItemModel itemPrevious = CobbleUtils.language.getItemPrevious();
    template.set(45, LinkedPageButton.builder()
      .display(itemPrevious.getItemStack())
      .linkType(LinkType.Previous)
      .build());

    ItemModel itemClose = CobbleUtils.language.getItemClose();
    template.set(49, itemClose.getButton(action -> {
      UIManager.closeUI(player);
    }));

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

    ItemStack display = itemChance.getItemStack();
    if (itemChance.getDisplay() != null && !itemChance.getDisplay().isEmpty())
      display = new ItemChance(itemChance.getDisplay(), 100).getItemStack();

    return GooeyButton.builder()
      .display(display)
      .title(AdventureTranslator.toNative(ItemChance.getTitle(itemChance)))
      .lore(Text.class, AdventureTranslator.toNativeL(lore))
      .build();
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


