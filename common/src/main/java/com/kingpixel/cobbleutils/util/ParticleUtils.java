package com.kingpixel.cobbleutils.util;

import com.kingpixel.cobbleutils.Model.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 06/11/2024 22:39
 */
@Deprecated
public class ParticleUtils {
  // Publics
  @Deprecated
  public static void sendParticles(Particle particle, ServerPlayerEntity player, List<Entity> entities) {
    entities.forEach(entity -> sendParticles(particle, player, entity));
  }

  @Deprecated
  public static void sendParticles(Particle particle, ServerPlayerEntity player, Entity entity) {
    DefaultParticleType particleType;
    String[] split = particle.getParticle().split(":");
    Identifier identifier = new Identifier(split[0], split[1]);

    try {
      if (Registries.PARTICLE_TYPE.get(identifier) instanceof DefaultParticleType) {
        particleType = (DefaultParticleType) Registries.PARTICLE_TYPE.get(identifier);
      } else {
        particleType = ParticleTypes.LAVA;
      }
    } catch (Exception e) {
      particleType = ParticleTypes.LAVA;
    }

    ParticleS2CPacket particleS2CPacket = getParticleS2CPacket(particle, entity, particleType);

    player.networkHandler.sendPacket(particleS2CPacket);
  }

  @Deprecated
  public static void sendParticlesNearPlayers(Particle particle, Entity entity, int radius) {
    entity.getWorld().getPlayers(TargetPredicate.DEFAULT, entity.getControllingPassenger(),
      Box.from(entity.getPos()).expand(radius)).forEach(player -> sendParticles(particle, PlayerUtils.castPlayer(player),
      entity));
  }

  // Privates
  @Deprecated
  private static @NotNull ParticleS2CPacket getParticleS2CPacket(Particle particle, Entity entity, DefaultParticleType particleType) {
    int offsetX = particle.getOffsetX() == null ? 0 : particle.getOffsetX();
    int offsetY = particle.getOffsetY() == null ? 0 : particle.getOffsetY();
    int offsetZ = particle.getOffsetZ() == null ? 0 : particle.getOffsetZ();
    int speed = particle.getSpeed() == null ? 0 : particle.getSpeed();

    return new ParticleS2CPacket(particleType, true,
      entity.getX(),
      entity.getY(), entity.getZ(), offsetX, offsetY, offsetZ, speed, particle.getNumberParticles());
  }
}
