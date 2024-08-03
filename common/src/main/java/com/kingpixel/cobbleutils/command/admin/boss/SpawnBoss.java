package com.kingpixel.cobbleutils.command.admin.boss;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.CobbleUtilsTags;
import com.kingpixel.cobbleutils.Model.options.BossChance;
import com.kingpixel.cobbleutils.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.SIZE_CUSTOM_TAG;
import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.SIZE_TAG;

/**
 * @author Carlos Varas Alonso - 03/08/2024 5:29
 */
public class SpawnBoss implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.then(
        Commands.literal("spawnboss")
          .requires(source -> source.hasPermission(2))
          .then(
            Commands.argument("boss", StringArgumentType.string())
              .suggests((context, builder) -> {
                CobbleUtils.config.getBosses().getBossChances().forEach(bossChance -> {
                  builder.suggest(bossChance.getRarity());
                });
                return builder.buildFuture();
              })
              .executes(context -> {
                if (!context.getSource().isPlayer()) {
                  return 0;
                }
                ServerPlayer player = context.getSource().getPlayerOrException();
                String boss = StringArgumentType.getString(context, "boss");
                spawnBoss(boss, player, player.position());
                return 1;
              }).then(
                Commands.literal("user")
                  .then(
                    Commands.argument("player", EntityArgument.player())
                      .executes(context -> {
                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                        Vec3 pos = player.position();
                        String boss = StringArgumentType.getString(context, "boss");
                        spawnBoss(boss, player, pos);
                        return 1;
                      })
                  )
              ).then(
                Commands.literal("coords")
                  .then(
                    Commands.argument("pos", Vec3Argument.vec3())
                      .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        Vec3 pos = Vec3Argument.getVec3(context, "pos");
                        String boss = StringArgumentType.getString(context, "boss");
                        spawnBoss(boss, player, pos);
                        return 1;
                      })
                  )
              )
          )
      )
    );

  }

  private static void spawnBoss(String boss, ServerPlayer player, Vec3 pos) {
    BossChance bossChance = CobbleUtils.config.getBosses().getBossChance(boss);
    if (bossChance == null) return;

    PokemonEntity pokemonEntity = null;
    if (CobbleUtils.config.getBosses().isForceAspectBoss()) {
      int n = Utils.RANDOM.nextInt(bossChance.getPokemons().getPokemon().size());
      pokemonEntity = PokemonProperties.Companion.parse(bossChance.getPokemons().getPokemon().get(n) + bossChance.getPokemons().getFormsoraspects()).createEntity(player.level());
    } else {
      pokemonEntity = PokemonProperties.Companion.parse("random uncatchable=yes").createEntity(player.level());
    }
    Pokemon pokemon = pokemonEntity.getPokemon();
    pokemon.getPersistentData().putBoolean(CobbleUtilsTags.BOSS_TAG, true);
    pokemon.getPersistentData().putString(CobbleUtilsTags.BOSS_RARITY_TAG, bossChance.getRarity());
    pokemon.getPersistentData().putString(SIZE_TAG, SIZE_CUSTOM_TAG);
    pokemon.setScaleModifier(Utils.RANDOM.nextFloat(bossChance.getMinsize(), bossChance.getMaxsize()));
    pokemon.setShiny(CobbleUtils.config.getBosses().isShiny());
    pokemon.setNickname(Component.literal(bossChance.getRarity()));
    pokemon.setLevel(Utils.RANDOM.nextInt(bossChance.getMinlevel(), bossChance.getMaxlevel()));
    pokemonEntity.setPos(pos.x, pos.y, pos.z);
    player.level().addFreshEntity(pokemonEntity);
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return 0;
  }


}