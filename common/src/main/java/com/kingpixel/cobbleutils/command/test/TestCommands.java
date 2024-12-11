package com.kingpixel.cobbleutils.command.test;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 26/08/2024 2:28
 */
public class TestCommands {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base.then(
        CommandManager.argument("pokemon", EntityArgumentType.entity())
          .executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayer();
            Entity target = EntityArgumentType.getEntity(context, "pokemon");
            if (target instanceof PokemonEntity pokemonEntity) {
              PlayerUtils.sendMessage(
                player,
                "Atk: " + pokemonEntity.getPokemon().getStat(Stats.ATTACK) + " Def: " + pokemonEntity.getPokemon().getStat(Stats.DEFENCE) +
                  " SpAtk: " + pokemonEntity.getPokemon().getStat(Stats.SPECIAL_ATTACK) + " SpDef: " + pokemonEntity.getPokemon().getStat(Stats.SPECIAL_DEFENCE) +
                  " Speed: " + pokemonEntity.getPokemon().getStat(Stats.SPEED) + " HP: " + pokemonEntity.getPokemon().getStat(Stats.HP)
              );
            }
            return 1;
          })
      )
    );
  }
}
