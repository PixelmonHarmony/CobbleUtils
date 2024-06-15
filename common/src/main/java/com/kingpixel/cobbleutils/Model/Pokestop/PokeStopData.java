package com.kingpixel.cobbleutils.Model.Pokestop;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.world.phys.Vec3;

/**
 * @author Carlos Varas Alonso - 15/06/2024 15:50
 */
@Getter
@Setter
@ToString
public class PokeStopData {
  private String type;
  private Vec3 pos;

  public PokeStopData(String type, Vec3 pos) {
    this.type = type;
    this.pos = pos;
  }

}
