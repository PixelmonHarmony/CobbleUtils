package com.kingpixel.cobbleutils.Model;

import com.kingpixel.cobbleutils.util.SoundUtil;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 07/12/2024 2:31
 */
@Getter
public class Sound {
  private boolean variousPlayers;
  private String sound;
  private double range;

  public Sound() {
    this.variousPlayers = false;
    this.sound = "minecraft:entity.fox.death";
    this.range = 16.0;
  }

  public void start(Entity entity) {
    if (variousPlayers) {
      playSoundNearPlayers(entity);
    } else {
      if (entity instanceof ServerPlayerEntity player) {
        playSoundPlayer(player);
      }
    }
  }

  public void playSoundNearPlayers(Entity entity) {
    List<ServerPlayerEntity> players = entity.getWorld().getEntitiesByClass(ServerPlayerEntity.class,
      new Box(entity.getBlockPos()).expand(getRange()), player -> true);
    SoundEvent sound = SoundUtil.getSound(getSound());
    if (players != null && !players.isEmpty()) {
      players.forEach(player -> {
        SoundUtil.playSound(sound, player);
      });
    }
  }

  public void playSoundPlayer(ServerPlayerEntity player) {
    SoundUtil.playSound(getSound(), player);
  }
}
