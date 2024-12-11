package com.kingpixel.cobbleutils.config;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.Model.CobbleUtilsTags;
import com.kingpixel.cobbleutils.Model.Particle;
import com.kingpixel.cobbleutils.Model.Sound;
import com.kingpixel.cobbleutils.util.ArraysPokemons;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.*;

/**
 * @author Carlos Varas Alonso - 11/12/2024 21:13
 */
@Getter
@Setter
public class BossConfig {
  public static Map<String, BossConfig> typeBoss = new HashMap<>();
  private static Integer oldLevel = Cobblemon.INSTANCE.getConfig().getMaxPokemonLevel();
  private boolean active;
  private boolean shiny;
  private String rarity;
  private String pokemonData;
  private String nickname;
  private boolean glowing;
  private Formatting glowingColor;
  private float chance;
  private int minlevel;
  private int maxlevel;
  private float minsize;
  private float maxsize;
  private Sound sound;
  private Particle particle;
  private List<String> pokemons;
  private List<String> blacklist;
  private AdvancedItemChance rewards;

  public BossConfig() {
    this.active = true;
    this.shiny = true;
    this.glowing = true;
    this.nickname = "§9§lBoss §c§l%pokemon%";
    this.glowingColor = Formatting.GOLD;
    this.chance = 50;
    this.minlevel = 105;
    this.maxlevel = 110;
    this.minsize = 1.5f;
    this.maxsize = 2.0f;
    this.sound = new Sound();
    this.particle = new Particle();
    this.pokemons = List.of(
      "rattata"
      , "rattataalola"
      , "raticate",
      "*");
    this.blacklist = List.of("ditto");
    this.rewards = new AdvancedItemChance();
  }

  public BossConfig(String rarity) {
    this();
    this.rarity = rarity;
    this.pokemonData = rarity;
  }

  public static boolean notCanBeBoss(Pokemon pokemon) {
    return pokemon.isPlayerOwned() || pokemon.getShiny() || pokemon.isLegendary() || pokemon.isUltraBeast()
      || PokemonUtils.getIvsAverage(pokemon.getIvs()) == 31 || ArraysPokemons.getTypePokemon(pokemon) != ArraysPokemons.TypePokemon.NORMAL;
  }

  public static BossConfig getBossConfigByRarity(String boss) {
    return typeBoss.get(boss);
  }

  public static void init() {
    typeBoss.clear();
    File folder = Utils.getAbsolutePath(CobbleUtils.PATH_BOSS);
    if (!folder.exists()) {
      folder.mkdirs();
      createDefaultBossConfig();
    } else {
      Arrays.stream(folder.listFiles()).toList().forEach(bossFile -> {
        CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_BOSS, bossFile.getName(),
          el -> {
            Gson gson = Utils.newGson();
            BossConfig config = gson.fromJson(el, BossConfig.class);
            if (!config.isActive()) return;
            config.setRarity(bossFile.getName().replace(".json", ""));
            typeBoss.put(config.getRarity(), config);
            if (config.getGlowingColor() == null) config.setGlowingColor(Formatting.GOLD);
            String data = gson.toJson(config);
            CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_BOSS, bossFile.getName(),
              data);
            if (!futureWrite.join()) {
              CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
            }
          });

        if (!futureRead.join()) {
          CobbleUtils.LOGGER.info("No config.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
          Gson gson = Utils.newGson();
          String data = gson.toJson(new BossConfig());
          CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_BOSS, bossFile.getName(),
            data);

          if (!futureWrite.join()) {
            CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
          }
        }
      });
    }
  }

  private static void createDefaultBossConfig() {
    BossConfig common = new BossConfig("common");
    BossConfig uncommon = new BossConfig("uncommon");
    BossConfig rare = new BossConfig("rare");
    BossConfig epic = new BossConfig("epic");

    List<BossConfig> bossConfigs = List.of(common, uncommon, rare, epic);

    Gson gson = Utils.newGson();
    bossConfigs.forEach(bossConfig -> {
      typeBoss.put(bossConfig.getRarity(), bossConfig);
      String data = gson.toJson(bossConfig);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_BOSS, bossConfig.getRarity() + ".json",
        data);
      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write config.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    });

  }

  public static void openMenuRewards(ServerPlayerEntity serverPlayerEntity, Pokemon pokemon) {
    String rarity = pokemon.getPersistentData().getString(CobbleUtilsTags.BOSS_RARITY_TAG);
    if (rarity.isEmpty()) return;
    BossConfig bossConfig = typeBoss.get(rarity);
    if (bossConfig == null) return;
    bossConfig.getRewards().openMenu(serverPlayerEntity);
  }

  public static void GiveRewards(ServerPlayerEntity player, Pokemon pokemon) {
    if (pokemon == null || player == null) return;
    String rarity = pokemon.getPersistentData().getString(CobbleUtilsTags.BOSS_RARITY_TAG);
    if (rarity.isEmpty()) return;
    BossConfig bossConfig = typeBoss.get(rarity);
    if (bossConfig == null) return;
    bossConfig.getRewards().giveRewards(player);
  }

  public static BossConfig getBossConfig(Pokemon pokemon) {
    if (pokemon == null) return null;
    List<BossConfig> bossConfigs = typeBoss
      .values()
      .stream()
      .filter(BossConfig::isActive)
      .filter(bossConfig -> (bossConfig.getPokemons().contains(pokemon.showdownId())
        || bossConfig.getPokemons().contains(pokemon.getForm().formOnlyShowdownId())
        || bossConfig.getPokemons().contains("*")) && (!bossConfig.getBlacklist().contains(pokemon.showdownId())
        || bossConfig.getBlacklist().contains(pokemon.getForm().formOnlyShowdownId())))
      .toList();
    if (bossConfigs.isEmpty()) return null;
    float totalWeight = bossConfigs.stream().map(BossConfig::getChance).reduce(0f, Float::sum);
    float randomValue = Utils.RANDOM.nextFloat() * totalWeight;
    float currentWeight = 0;
    for (BossConfig bossConfig : bossConfigs) {
      currentWeight += bossConfig.getChance();
      if (randomValue <= currentWeight) {
        return bossConfig;
      }
    }
    return bossConfigs.get(Utils.RANDOM.nextInt(bossConfigs.size()));
  }

  public boolean apply(PokemonEntity pokemonEntity) {
    String form = this.getPokemonData();
    int level;
    pokemonEntity.setInvulnerable(true);
    Pokemon pokemon = pokemonEntity.getPokemon();
    PokemonProperties.Companion.parse("uncatchable=yes " + form).apply(pokemon);
    if (oldLevel < getMaxlevel()) {
      Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(this.getMaxlevel());
    }
    if (this.getMinlevel() != this.getMaxlevel()) {
      level = Utils.RANDOM.nextInt(this.getMinlevel(), this.getMaxlevel());
      pokemon.setLevel(level);
    } else {
      level = this.getMinlevel();
      pokemon.setLevel(level);
    }
    Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(oldLevel);
    pokemon.getPersistentData().putString(BOSS_RARITY_TAG, this.getRarity());
    pokemon.getPersistentData().putBoolean(BOSS_TAG, true);
    pokemon.setShiny(this.isShiny());
    pokemon.getPersistentData().putString(SIZE_TAG, SIZE_CUSTOM_TAG);
    if (this.getMinsize() != this.getMaxsize()) {
      pokemon.setScaleModifier(Utils.RANDOM.nextFloat(this.getMinsize(), this.getMaxsize()));
    } else {
      pokemon.setScaleModifier(this.getMinsize());
    }
    if (isGlowing()) {
      pokemonEntity.setGlowing(true);

      pokemonEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 0x333333, false,
        false));
      Scoreboard scoreboard = pokemonEntity.getEntityWorld().getScoreboard();
      Team team = scoreboard.getTeam("boss_" + getRarity());
      if (team == null) team = scoreboard.addTeam("boss_" + getRarity());
      team.setColor(glowingColor);

      scoreboard.addPlayerToTeam(pokemonEntity.getEntityName(), team);
    }


    pokemonEntity.setCustomName(
      Text.literal(
        getNickname().replace("%pokemon%", pokemon.getDisplayName().getString())
      )
    );
    this.getSound().start(pokemonEntity);
    this.getParticle().sendParticlesNearPlayers(pokemonEntity);
    return true;
  }
}
