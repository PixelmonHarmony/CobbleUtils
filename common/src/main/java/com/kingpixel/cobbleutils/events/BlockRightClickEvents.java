package com.kingpixel.cobbleutils.events;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.CobbleUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BlockRightClickEvents {

  public static void register(ServerPlayerEntity player, Hand hand, BlockPos blockPos, Direction direction) {
    if (hand != Hand.MAIN_HAND) {
      return;
    }
    try {
      BlockEntity blockEntity = player.getWorld().getBlockEntity(blockPos);
      if (blockEntity == null) {
        return;
      }

      // Obtener información básica del BlockEntity
      BlockState state = blockEntity.getCachedState();
      Block block = state.getBlock();
      String id = block.getTranslationKey();
      NbtCompound tag = blockEntity.createNbt();

      // Lógica específica para diferentes tipos de bloques
      if (id.contains("fossil_analyzer")) {
        if (!CobbleUtils.config.isFossil())
          return;
        handleFossilAnalyzer(player, blockEntity);
      } else if ((id.contains("cobblemon.restoration_tank") || id.contains("cobblemon.monitor"))) {
        if (!CobbleUtils.config.isFossil())
          return;
        handleRestorationTankOrMonitor(player, tag);
      } else {
      }

    } catch (Exception e) {
      CobbleUtils.LOGGER.error("Error in BlockRightClickEvents: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void handleFossilAnalyzer(ServerPlayerEntity player, BlockEntity blockEntity) {
    if (blockEntity == null) {
      return;
    }
    NbtCompound tag = blockEntity.createNbt();
    if (tag.contains("MultiblockStore")) {
      fossilMachine(player, tag);
    }
  }

  private static void handleRestorationTankOrMonitor(ServerPlayerEntity player, NbtCompound tag) {
    if (tag.contains("ControllerBlock")) {
      NbtCompound controllerTag = tag.getCompound("ControllerBlock");
      if (controllerTag.contains("X") && controllerTag.contains("Y") && controllerTag.contains("Z")) {
        BlockPos controllerPos = new BlockPos(controllerTag.getInt("X"), controllerTag.getInt("Y"),
          controllerTag.getInt("Z"));
        BlockEntity blockEntityTank = player.getWorld().getBlockEntity(controllerPos);
        if (blockEntityTank != null) {
          NbtCompound blocktag = blockEntityTank.createNbt();
          fossilMachine(player, blocktag);
        } else {
          CobbleUtils.LOGGER.error("BlockRightClickEvents: BlockEntityTank is null at " + controllerPos);
        }
      } else {
        CobbleUtils.LOGGER.error("BlockRightClickEvents: ControllerBlock tag is missing position data.");
      }
    }
  }

  private static void fossilMachine(ServerPlayerEntity player, NbtCompound tag) {
    NbtCompound multiblockstore = tag.getCompound("MultiblockStore");
    if (multiblockstore == null)
      return;
    int organic = multiblockstore.getInt("OrganicContent");
    int timeleft = multiblockstore.getInt("TimeLeft");
    boolean hasCreatedPokemon = multiblockstore.getBoolean("HasCreatedPokemon");
    if (hasCreatedPokemon) {
      player.sendMessage(AdventureTranslator.toNative(CobbleUtils.language.getMessagefossilcomplete()));
      return;
    }

    if (timeleft != -1 && organic == 128) {
      int time = timeleft / 20;
      player.sendMessage(AdventureTranslator.toNative(CobbleUtils.language.getMessagefossiltime().replace(
        "%time%", CobbleUtilities.convertSecondsToTime(time))));
    }
  }
}
