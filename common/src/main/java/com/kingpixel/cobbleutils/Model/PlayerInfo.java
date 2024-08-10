package com.kingpixel.cobbleutils.Model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 28/06/2024 3:04
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class PlayerInfo {
  private String name;
  private UUID uuid;

  public PlayerInfo(String name, UUID uuid) {
    this.name = name;
    this.uuid = uuid;
  }

  public static PlayerInfo fromPlayer(ServerPlayerEntity player) {
    return new PlayerInfo(player.getGameProfile().getName(), player.getUuid());
  }
}
