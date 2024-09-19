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
  private UUID playeruuid;
  private String name;

  public PlayerInfo(String name, UUID playeruuid) {
    this.playeruuid = playeruuid;
    this.name = name;
  }

  public PlayerInfo(UUID partyId, UUID playeruuid, String name) {
    this.playeruuid = playeruuid;
    this.name = name;
  }

  public static PlayerInfo fromPlayer(ServerPlayerEntity player) {
    return new PlayerInfo(player.getGameProfile().getName(), player.getUuid());
  }
}
