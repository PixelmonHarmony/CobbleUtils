package com.kingpixel.cobbleutils.events;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.CobbleUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockRightClickEvents {

  public static void register(Player player, InteractionHand hand, BlockPos blockPos, Direction direction) {
    if (hand != InteractionHand.MAIN_HAND) {
      return;
    }

    try {
      BlockEntity blockEntity = player.level().getBlockEntity(blockPos);
      if (blockEntity == null) {
        return;
      }

      // Obtener información básica del BlockEntity
      BlockState state = blockEntity.getBlockState();
      Block block = state.getBlock();
      String id = block.getDescriptionId();
      CompoundTag tag = blockEntity.getUpdateTag();


      // Lógica específica para diferentes tipos de bloques
      if (id.contains("fossil_analyzer") && CobbleUtils.config.isFossil()) {
        handleFossilAnalyzer(player, blockEntity);
      } else if ((id.contains("cobblemon.restoration_tank") || id.contains("cobblemon.monitor")) && CobbleUtils.config.isFossil()) {
        handleRestorationTankOrMonitor(player, tag);
      } else {
      }

    } catch (Exception e) {
      CobbleUtils.LOGGER.error("Error in BlockRightClickEvents: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void handleFossilAnalyzer(Player player, BlockEntity blockEntity) {
    if (blockEntity == null) {
      return;
    }
    CompoundTag tag = blockEntity.getUpdateTag();
    if (tag.contains("MultiblockStore")) {
      fossilMachine(player, tag);
    }
  }

  private static void handleRestorationTankOrMonitor(Player player, CompoundTag tag) {
    if (tag.contains("ControllerBlock")) {
      CompoundTag controllerTag = tag.getCompound("ControllerBlock");
      if (controllerTag.contains("X") && controllerTag.contains("Y") && controllerTag.contains("Z")) {
        BlockPos controllerPos = new BlockPos(controllerTag.getInt("X"), controllerTag.getInt("Y"),
          controllerTag.getInt("Z"));
        BlockEntity blockEntityTank = player.level().getBlockEntity(controllerPos);
        if (blockEntityTank != null) {
          CompoundTag blocktag = blockEntityTank.getUpdateTag();
          fossilMachine(player, blocktag);
        } else {
          CobbleUtils.LOGGER.error("BlockRightClickEvents: BlockEntityTank is null at " + controllerPos);
        }
      } else {
        CobbleUtils.LOGGER.error("BlockRightClickEvents: ControllerBlock tag is missing position data.");
      }
    }
  }

  private static void fossilMachine(Player player, CompoundTag tag) {
    CompoundTag multiblockstore = tag.getCompound("MultiblockStore");
    if (multiblockstore == null) return;
    int organic = multiblockstore.getInt("OrganicContent");
    int timeleft = multiblockstore.getInt("TimeLeft");
    boolean hasCreatedPokemon = multiblockstore.getBoolean("HasCreatedPokemon");
    if (hasCreatedPokemon) {
      player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.language.getMessagefossilcomplete()));
      return;
    }

    if (timeleft != -1 && organic == 128) {
      int time = timeleft / 20;
      player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.language.getMessagefossiltime().replace(
        "%time%", CobbleUtilities.convertSecondsToTime(time))));
    }
  }
}
