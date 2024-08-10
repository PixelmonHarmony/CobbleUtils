package com.kingpixel.cobbleutils.command.admin.boss;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.CobbleUtilsTags;
import com.kingpixel.cobbleutils.Model.options.BossChance;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.SIZE_CUSTOM_TAG;
import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.SIZE_TAG;

/**
 * @author Carlos Varas Alonso - 03/08/2024 5:29
 */
public class SpawnBoss implements Command<ServerCommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base.then(
        CommandManager.literal("spawnboss")
          .requires(source -> LuckPermsUtil.checkPermission(source, 4, List.of("cobbleutils.spawnboss", "cobbleutils.admin")))
          .then(
            CommandManager.argument("boss", StringArgumentType.string())
              .suggests((context, builder) -> {
                CobbleUtils.config.getBosses().getBossChances()
                  .forEach(bossChance -> builder.suggest(bossChance.getRarity()));
                return builder.buildFuture();
              })
              .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                  return 0;
                }
                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                String boss = StringArgumentType.getString(context, "boss");
                spawnBoss(boss, player.getWorld(), player.getPos());
                return 1;
              }).then(
                CommandManager.literal("user")
                  .then(
                    CommandManager.argument("player", EntityArgumentType.player())
                      .executes(context -> {
                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context,
                          "player");
                        Vec3d pos = player.getPos();
                        String boss = StringArgumentType.getString(context, "boss");
                        spawnBoss(boss, player.getWorld(), pos);
                        return 1;
                      })))
              .then(
                CommandManager.literal("coords")
                  .then(
                    CommandManager.argument("pos", Vec3ArgumentType.vec3())
                      .then(
                        CommandManager.argument("world", DimensionArgumentType.dimension())
                          .executes(context -> {
                            Vec3d pos = Vec3ArgumentType.getVec3(context, "pos");
                            String boss = StringArgumentType.getString(context, "boss");
                            World level = DimensionArgumentType.getDimensionArgument(context,
                              "world");
                            spawnBoss(boss, level, pos);
                            return 1;
                          })))))));

  }

  private static void spawnBoss(String boss, World level, Vec3d pos) {
    BossChance bossChance = CobbleUtils.config.getBosses().getBossChance(boss);
    if (bossChance == null)
      return;

    PokemonEntity pokemonEntity;
    if (CobbleUtils.config.getBosses().isForceAspectBoss()) {
      int n = Utils.RANDOM.nextInt(bossChance.getPokemons().getPokemon().size());
      pokemonEntity = PokemonProperties.Companion
        .parse(bossChance.getPokemons().getPokemon().get(n) + bossChance.getPokemons().getFormsoraspects())
        .createEntity(level);
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
    pokemon.setNickname(Text.literal(bossChance.getRarity()));
    if (bossChance.getMinlevel() != bossChance.getMaxlevel()) {
      pokemon.setLevel(Utils.RANDOM.nextInt(bossChance.getMinlevel(), bossChance.getMaxlevel()));
    } else {
      pokemon.setLevel(bossChance.getMinlevel());
    }
    pokemonEntity.setPos(pos.x, pos.y, pos.z);
    level.spawnEntity(pokemonEntity);
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }

}