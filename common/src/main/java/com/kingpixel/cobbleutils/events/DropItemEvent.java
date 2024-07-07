package com.kingpixel.cobbleutils.events;

import com.kingpixel.cobbleutils.util.RewardsUtils;
import dev.architectury.event.EventResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @author Carlos Varas Alonso - 02/07/2024 20:16
 */
public class DropItemEvent {
  /**
   * Registers the drop of an item.
   * <p>
   * TODO: Fix dupe when you give a player an item and they drop it
   * example: /give @s minecraft:stone 1 and the player have all the slots full less one with 63 items
   * the player will recibe the item and the item will be saved in the rewards inventory
   * <p>
   *
   * @param player     Player that dropped the item
   * @param itemEntity ItemEntity that was dropped
   *
   * @return EventResult
   */
  public static EventResult register(Player player, ItemEntity itemEntity) {
    //if (true) return EventResult.pass(); // TODO: Remove this line when fixing the dupe
    ItemStack itemStack = itemEntity.getItem();
    Inventory inventory = player.getInventory();
    if (player.isCreative()) return EventResult.pass();
    if (player.isDeadOrDying()) return EventResult.pass();


    // CobbleUtils.LOGGER.info("Item dropped: " + itemEntity.getItem().getCount() + " " + itemEntity.getItem()
    // .getHoverName().getString());
    if (inventory.getSlotWithRemainingSpace(itemStack) == -1 && inventory.getFreeSlot() == -1) {
      RewardsUtils.saveRewardItemStack(player, itemStack);
      itemEntity.remove(Entity.RemovalReason.KILLED);
      return EventResult.interruptTrue();
    }

    return EventResult.pass();
  }


}
