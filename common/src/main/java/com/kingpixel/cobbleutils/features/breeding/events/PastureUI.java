package com.kingpixel.cobbleutils.features.breeding.events;

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.ui.PlotBreedingUI;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * @author Carlos Varas Alonso - 02/08/2024 14:20
 */
public class PastureUI {
  public static void register() {
    InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, blockPos, direction) -> {
      if (!CobbleUtils.breedconfig.isChangeuipasture())
        return EventResult.pass();
      BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
      if (blockEntity == null) {
        blockPos = new BlockPos(blockPos.getX(), blockPos.getY() - 1, blockPos.getZ());
        blockEntity = player.getWorld().getBlockEntity(blockPos);
        if (blockEntity == null)
          return EventResult.pass();
        if (blockEntity instanceof PokemonPastureBlockEntity) {
          PlotBreedingUI.open((ServerPlayerEntity) player);
        } else {
          return EventResult.pass();
        }
      }
      if (blockEntity instanceof PokemonPastureBlockEntity) {
        PlotBreedingUI.open((ServerPlayerEntity) player);
      }

      return EventResult.pass();
    });
  }
}
