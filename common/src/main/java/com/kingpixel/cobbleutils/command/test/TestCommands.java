package com.kingpixel.cobbleutils.command.test;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.entity.pokemon.effects.IllusionEffect;
import com.kingpixel.cobbleutils.util.ArraysPokemons;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kotlin.Unit;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.Set;

/**
 * @author Carlos Varas Alonso - 26/08/2024 2:28
 */
public class TestCommands {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base.then(
        CommandManager.literal("type")
          .then(
            CommandManager.argument("type", StringArgumentType.string())
              .suggests((context, builder) -> {
                for (ElementalType type : ElementalTypes.INSTANCE.all()) {
                  builder.suggest(type.getName().toUpperCase());
                }
                return builder.buildFuture();
              })
              .executes(context -> {
                String type = StringArgumentType.getString(context, "type");
                ElementalType elementalType = ElementalTypes.INSTANCE.get(type.toUpperCase());
                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                Cobblemon.INSTANCE.getStorage().getParty(player).add(ArraysPokemons.getRandomPokemon(elementalType));
                ArraysPokemons.getRandomPokemons(Collections.singleton(elementalType),
                  Set.of(ArraysPokemons.TypePokemon.LEGENDARY,
                    ArraysPokemons.TypePokemon.PARADOX,
                    ArraysPokemons.TypePokemon.ULTRABEAST,
                    ArraysPokemons.TypePokemon.MYTHICAL)).forEach(pokemon -> {
                  pokemon.sendOut(player.getServerWorld(), player.getPos(), new IllusionEffect(), (pokemonEntity) -> {
                    player.sendMessage(Text.literal(
                        "You have received a " + pokemonEntity.getPokemon().getDisplayName().getString() + "!"
                      )
                    );
                    return Unit.INSTANCE;
                  });
                });
                return 1;
              })
          )
      )
    );
  }
}
