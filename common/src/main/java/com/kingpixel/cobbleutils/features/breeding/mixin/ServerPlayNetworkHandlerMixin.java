package com.kingpixel.cobbleutils.features.breeding.mixin;

/**
 * Code by Landonjw
 */
/*
@Mixin(sER.class)
public abstract class ServerPlayNetworkHandlerMixin {

  @Shadow public ServerPlayer player;

  @Inject(method = "", at = @At("HEAD"))
  public void breeding$onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
    if (!player.hasPose(Pose.FALL_FLYING) && !player.isInvulnerable()) { // No elytra or flight

      double deltaMovement = getDeltaMovement(player, packet);

      player.getInventory().main.stream()
        .filter(stack -> stack.getItem() instanceof PokemonEgg)
        .forEach(egg -> updateEggSteps(egg, deltaMovement));
    }
  }

  @Unique
  private double getDeltaMovement(ServerPlayerEntity player, PlayerMoveC2SPacket packet) {
    double oldX = player.get();
    double oldZ = player.getZ();
    double newX = MathHelper.clamp(packet.getX(oldX), -3.0E7D, 3.0E7D);
    double newZ = MathHelper.clamp(packet.getZ(oldZ), -3.0E7D, 3.0E7D);

    var deltaMovement = Math.sqrt(Math.pow(newX - oldX, 2) + Math.pow(newZ - oldZ, 2));
    return hasStepAcceleratingPokemon(player) ? deltaMovement : deltaMovement / 2;
  }

  @Unique
  private boolean hasStepAcceleratingPokemon(ServerPlayerEntity player) {
    var stepAcceleratingAbilities = Arrays.asList(
      "magmaarmor",
      "flamebody",
      "steamengine"
    );

    var party = Cobblemon.INSTANCE.getStorage().getParty(player);
    for (Pokemon pokemon : party) {
      if (stepAcceleratingAbilities.contains(pokemon.getAbility().getName().toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  @Unique
  private void updateEggSteps(ItemStack egg, double deltaMovement) {
    // Stuff
  }

}
*/