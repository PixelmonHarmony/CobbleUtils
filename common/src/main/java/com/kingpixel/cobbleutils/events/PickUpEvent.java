package com.kingpixel.cobbleutils.events;

import dev.architectury.event.EventResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 03/07/2024 1:14
 */
public class PickUpEvent {
  public static Set<UUID> ItemEntityUUIDs = new HashSet<>();

  public static EventResult register(Player player, ItemEntity itemEntity, ItemStack itemStack) {
    ItemEntityUUIDs.add(itemEntity.getUUID());
    return EventResult.pass();
  }
}
