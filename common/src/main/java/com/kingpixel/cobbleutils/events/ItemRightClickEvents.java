package com.kingpixel.cobbleutils.events;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.ui.ShinyTokenUI;
import com.kingpixel.cobbleutils.util.Utils;
import dev.architectury.event.CompoundEventResult;
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
    if (itemStack.hasTag() && itemStack.getTag().contains("shinytoken") && itemStack.getItem() == Utils.parseItemId(CobbleUtils.config.getShinytoken().getItem()).getItem()) {
      ShinyTokenUI.openmenu(player);
    }
    if (itemStack.getItem().getDescriptionId(player.getMainHandItem()).contains("shulker_box") && CobbleUtils.config.isShulkers()) {
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
