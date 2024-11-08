package com.kingpixel.cobbleutils.Model;

import lombok.Getter;

/**
 * @author Carlos Varas Alonso - 06/11/2024 22:41
 */
@Getter
public class Particle {
  private String particle;
  private int numberParticles;
  private Integer offsetX;
  private Integer offsetY;
  private Integer offsetZ;
  private Integer speed;
  private Double radius;

  public Particle() {
    this.particle = "minecraft:flame";
    this.numberParticles = 1;
    this.offsetX = 1;
    this.offsetY = 1;
    this.offsetZ = 1;
    this.speed = 0;
    this.radius = null;
  }

  public Particle(boolean isNeedNearPlayers) {
    this.particle = "minecraft:flame";
    this.numberParticles = 1;
    this.offsetX = 1;
    this.offsetY = 1;
    this.offsetZ = 1;
    this.speed = 0;
    if (isNeedNearPlayers) {
      this.radius = 10.0;
    } else {
      this.radius = null;
    }
  }

  public Particle(String particle) {
    this.particle = particle;
    this.numberParticles = 1;
    this.offsetX = 1;
    this.offsetY = 1;
    this.offsetZ = 1;
    this.speed = 0;
    this.radius = null;
  }

  public Particle(String particle, int numberParticles) {
    this.particle = particle;
    this.numberParticles = numberParticles;
    this.offsetX = 1;
    this.offsetY = 1;
    this.offsetZ = 1;
    this.speed = 0;
    this.radius = null;
  }
}
