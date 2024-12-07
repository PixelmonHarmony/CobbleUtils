package com.kingpixel.cobbleutils.Model;

import com.kingpixel.cobbleutils.util.SoundUtil;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 07/12/2024 2:31
 */
@Getter
public class Sound {
  private String sound;
  private double range;

  public Sound() {
    this.sound = "minecraft:block.note_block.harp";
    this.range = 16.0;
  }

  public void playSoundNearPlayers(Entity entity) {
    List<ServerPlayerEntity> players = entity.getWorld().getEntitiesByClass(ServerPlayerEntity.class,
      new Box(entity.getBlockPos()).expand(16.0), player -> true);
    if (players != null && !players.isEmpty()) {
      players.forEach(player -> {
        SoundUtil.playSound(getSound(), player);
      });
    }
  }
}
