package com.kingpixel.cobbleutils.events;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.CobbleUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * @author Carlos Varas Alonso - 14/06/2024 3:08
 */
public class BlockRightClickEvents {

  public static void register(Player player, InteractionHand hand, BlockPos blockpos, Direction direction) {
    if (hand == InteractionHand.MAIN_HAND) {
      try {
        // Fosil
        BlockEntity blockEntity = player.level().getBlockEntity(blockpos);
        if (blockEntity == null) {
          return;
        }
        Block block = blockEntity.getBlockState().getBlock();
        String id = block.getDescriptionId();
        CompoundTag tag = blockEntity.getUpdateTag();
        if (id.contains("fossil_analyzer")) {
          if (tag.get("MultiblockStore") != null) {
            fossilMachine(player, tag);
          }
          return;
        }
        if (id.contains("cobblemon.restoration_tank") || id.contains("cobblemon.monitor")) {

          CompoundTag controllerBlock = (CompoundTag) tag.get("ControllerBlock");
          assert controllerBlock != null;
          BlockPos a = BlockEntity.getPosFromTag(controllerBlock);
          blockEntity = player.level().getBlockEntity(a);
          if (blockEntity != null) {
            CompoundTag newtag = blockEntity.getUpdateTag();
            if (newtag.get("MultiblockStore") != null) {
              fossilMachine(player, newtag);
            }
          }
        }
        // Otras cosas
      } catch (Exception e) {
        CobbleUtils.LOGGER.error("Error in BlockRightClickEvents: " + e.getMessage());
      }
    }
  }

  private static void fossilMachine(Player player, CompoundTag tag) {
    CompoundTag multiblockstore = (CompoundTag) tag.get("MultiblockStore");
    if (multiblockstore.contains("TimeLeft", Tag.TAG_INT)) {
      int timeLeft = multiblockstore.getInt("TimeLeft");
      boolean hasCreatedPokemon = multiblockstore.getBoolean("HasCreatedPokemon");
      int organicContent = multiblockstore.getInt("OrganicContent");

      if (hasCreatedPokemon || organicContent > 0) {
        if (timeLeft > 0) {
          int time = timeLeft / 20;
          player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.language.getMessagefossiltime().replace(
            "%time%", CobbleUtilities.convertSecondsToTime(time))));
        } else {
          player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.language.getMessagefossilcomplete()));
        }
      }
    }

  }
}
