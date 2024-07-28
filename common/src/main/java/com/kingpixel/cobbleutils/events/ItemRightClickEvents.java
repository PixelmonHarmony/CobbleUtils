package com.kingpixel.cobbleutils.events;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.ui.ShinyTokenUI;
import dev.architectury.event.CompoundEventResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @author Carlos Varas Alonso - 25/06/2024 19:38
 */
public class ItemRightClickEvents {
  public static CompoundEventResult register(Player player, InteractionHand hand) {
    ItemStack itemStack = player.getItemInHand(hand);
    CompoundTag tag = itemStack.getTag();
    if (tag == null) return CompoundEventResult.pass();
    if (itemStack.hasTag() && tag.contains("shinytoken") && itemStack.getItem() == CobbleUtils.config.getShinytoken().getItemStack().getItem()) {
      if (!CobbleUtils.config.isActiveshinytoken()) return CompoundEventResult.pass();
      ShinyTokenUI.openmenu(player);
    }
    if (itemStack.getItem().getDescriptionId(player.getMainHandItem()).contains("shulker_box")) {
      if (!CobbleUtils.config.isShulkers()) return CompoundEventResult.pass();
      return shulkers((ServerPlayer) player, hand, itemStack);
    }
    return CompoundEventResult.pass();

  }

  private static CompoundEventResult shulkers(ServerPlayer player, InteractionHand hand, ItemStack itemStack) {
    try {
      player.displayClientMessage(Component.literal("Shulker Box abierta!"), true);
      player.openItemGui(itemStack, hand);
      return CompoundEventResult.pass();
    } catch (Exception e) {
      CobbleUtils.LOGGER.error("Error al abrir la Shulker Box: " + e.getMessage());
      e.printStackTrace();
    }
    return CompoundEventResult.pass();
  }
}
