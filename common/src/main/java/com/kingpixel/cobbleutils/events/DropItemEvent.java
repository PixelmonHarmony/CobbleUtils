package com.kingpixel.cobbleutils.events;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @author Carlos Varas Alonso - 02/07/2024 20:16
 */
public class DropItemEvent {
  public static EventResult register(Player player, ItemEntity itemEntity) {
    ItemStack itemStack = itemEntity.getItem();
    Inventory inventory = player.getInventory();

    if (player.isDeadOrDying()) {
      return EventResult.pass();
    }
    CobbleUtils.LOGGER.info("ItemEntity uuid -> " + itemEntity.getUUID());
    CobbleUtils.LOGGER.info("ItemEntity -> " + PickUpEvent.ItemEntityUUIDs.contains(itemEntity.getUUID()));

    player.sendSystemMessage(Component.literal(inventory.getSlotWithRemainingSpace(itemStack) + " " + inventory.getFreeSlot()));
    // Si no se pudo apilar completamente, verificar si hay un espacio libre en el inventario
    if (inventory.getSlotWithRemainingSpace(itemStack) == -1 && inventory.getFreeSlot() == -1) {
      if (PickUpEvent.ItemEntityUUIDs.contains(itemEntity.getUUID())) return EventResult.pass();
      player.sendSystemMessage(Component.literal("Se ha añadido un ítem a tu lista de recompensas. " + itemStack.getHoverName()));
      RewardsUtils.saveRewardItemStack(player, itemStack);
      itemEntity.remove(Entity.RemovalReason.DISCARDED);
      return EventResult.pass();
    }
    // Permitir que el ítem se maneje normalmente si hay espacio libre o puede ser apilado
    return EventResult.pass();
  }


}
