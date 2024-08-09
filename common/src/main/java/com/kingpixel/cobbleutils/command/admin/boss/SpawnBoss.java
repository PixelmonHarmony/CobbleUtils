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
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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
                CobbleUtils.config.getBosses().getBossChances().forEach(bossChance -> builder.suggest(bossChance.getRarity()));
                return builder.buildFuture();
              })
              .executes(context -> {
                if (!context.getSource().isPlayer()) {
                  return 0;
                }
                ServerPlayer player = context.getSource().getPlayerOrException();
                String boss = StringArgumentType.getString(context, "boss");
                spawnBoss(boss, player.level(), player.position());
                return 1;
              }).then(
                Commands.literal("user")
                  .then(
                    Commands.argument("player", EntityArgument.player())
                      .executes(context -> {
                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                        Vec3 pos = player.position();
                        String boss = StringArgumentType.getString(context, "boss");
                        spawnBoss(boss, player.level(), pos);
                        return 1;
                      })
                  )
              ).then(
                Commands.literal("coords")
                  .then(
                    Commands.argument("pos", Vec3Argument.vec3())
                      .then(
                        Commands.argument("world", DimensionArgument.dimension())
                          .executes(context -> {
                            Vec3 pos = Vec3Argument.getVec3(context, "pos");
                            String boss = StringArgumentType.getString(context, "boss");
                            Level level = DimensionArgument.getDimension(context, "world");
                            spawnBoss(boss, level, pos);
                            return 1;
                          })
                      )
                  )
              )
          )
      )
    );

  }

  private static void spawnBoss(String boss, Level level, Vec3 pos) {
    BossChance bossChance = CobbleUtils.config.getBosses().getBossChance(boss);
    if (bossChance == null) return;

    PokemonEntity pokemonEntity;
    if (CobbleUtils.config.getBosses().isForceAspectBoss()) {
      int n = Utils.RANDOM.nextInt(bossChance.getPokemons().getPokemon().size());
      pokemonEntity = PokemonProperties.Companion.parse(bossChance.getPokemons().getPokemon().get(n) + bossChance.getPokemons().getFormsoraspects()).createEntity(level);
    } else {
      pokemonEntity = PokemonProperties.Companion.parse("random uncatchable=yes").createEntity(level);
    }
    Pokemon pokemon = pokemonEntity.getPokemon();
    pokemon.getPersistentData().putBoolean(CobbleUtilsTags.BOSS_TAG, true);
    pokemon.getPersistentData().putString(CobbleUtilsTags.BOSS_RARITY_TAG, bossChance.getRarity());
    pokemon.getPersistentData().putString(SIZE_TAG, SIZE_CUSTOM_TAG);
    if (bossChance.getMinsize() != bossChance.getMaxsize()) {
      pokemon.setScaleModifier(Utils.RANDOM.nextFloat(bossChance.getMinsize(), bossChance.getMaxsize()));
    } else {
      pokemon.setScaleModifier(bossChance.getMinsize());
    }
    pokemon.setShiny(CobbleUtils.config.getBosses().isShiny());
    pokemon.setNickname(Component.literal(bossChance.getRarity()));
    if (bossChance.getMinlevel() != bossChance.getMaxlevel()) {
      pokemon.setLevel(Utils.RANDOM.nextInt(bossChance.getMinlevel(), bossChance.getMaxlevel()));
    } else {
      pokemon.setLevel(bossChance.getMinlevel());
    }
    pokemonEntity.setPos(pos.x, pos.y, pos.z);
    level.addFreshEntity(pokemonEntity);
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return 0;
  }


}