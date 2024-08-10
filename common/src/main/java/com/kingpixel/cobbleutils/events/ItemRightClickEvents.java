package com.kingpixel.cobbleutils.events;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.ui.ShinyTokenUI;
import dev.architectury.event.CompoundEventResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

/**
 * @author Carlos Varas Alonso - 25/06/2024 19:38
 */
public class ItemRightClickEvents {
  public static CompoundEventResult register(PlayerEntity player, Hand hand) {
    ItemStack itemStack = player.getStackInHand(hand);
    NbtCompound tag = itemStack.getNbt();
    if (tag == null)
      return CompoundEventResult.pass();
    if (itemStack.hasNbt() && tag.contains("shinytoken")
      && itemStack.getItem() == CobbleUtils.config.getShinytoken().getItemStack().getItem()) {
      if (!CobbleUtils.config.isActiveshinytoken())
        return CompoundEventResult.pass();
      ShinyTokenUI.openmenu((ServerPlayerEntity) player);
    }
    if (itemStack.getItem().getTranslationKey(player.getMainHandStack()).contains("shulker_box")) {
      if (!CobbleUtils.config.isShulkers())
        return CompoundEventResult.pass();
      return shulkers((ServerPlayerEntity) player, hand, itemStack);
    }
    return CompoundEventResult.pass();

  }

  private static CompoundEventResult shulkers(ServerPlayerEntity player, Hand hand, ItemStack itemStack) {
    try {
      player.sendMessageToClient(Text.literal("Shulker Box abierta!"), true);
      //player.item(itemStack, hand);
      return CompoundEventResult.pass();
    } catch (Exception e) {
      CobbleUtils.LOGGER.error("Error al abrir la Shulker Box: " + e.getMessage());
      e.printStackTrace();
    }
    return CompoundEventResult.pass();
  }
}
