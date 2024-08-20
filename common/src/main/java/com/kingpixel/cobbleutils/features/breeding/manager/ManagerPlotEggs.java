package com.kingpixel.cobbleutils.features.breeding.manager;

import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.reflect.TypeToken;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.models.PlotBreeding;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Data;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Author: Carlos Varas Alonso - 02/08/2024 14:09
 */
@Getter
@Data
public class ManagerPlotEggs {
  private Map<UUID, List<PlotBreeding>> eggs = new HashMap<>();

  public ManagerPlotEggs() {
    File dataFolder = Utils.getAbsolutePath(CobbleUtils.PATH_BREED_DATA);
    if (!dataFolder.exists()) {
      dataFolder.mkdirs();
    }
  }

  public void init(ServerPlayerEntity player) {
    CompletableFuture.runAsync(() -> {
      File playerFile = Utils.getAbsolutePath(CobbleUtils.PATH_BREED_DATA + player.getUuid() + ".json");
      if (playerFile.exists()) {
        try (FileReader reader = new FileReader(playerFile)) {
          Type listType = new TypeToken<List<PlotBreeding>>() {
          }.getType();
          List<PlotBreeding> playerEggs = Utils.newWithoutSpacingGson().fromJson(reader, listType);
          if (playerEggs.isEmpty()) {
            for (int i = 0; i < CobbleUtils.breedconfig.getMaxplots(); i++) {
              playerEggs.add(new PlotBreeding());
            }
          } else if (playerEggs.size() < CobbleUtils.breedconfig.getMaxplots()) {
            for (int i = playerEggs.size(); i < CobbleUtils.breedconfig.getMaxplots(); i++) {
              playerEggs.add(new PlotBreeding());
            }
          } else if (playerEggs.size() > CobbleUtils.breedconfig.getMaxplots()) {
            List<PlotBreeding> eggsToRemove = playerEggs.subList(CobbleUtils.breedconfig.getMaxplots(),
              playerEggs.size());

            for (PlotBreeding egg : eggsToRemove) {
              RewardsUtils.saveRewardPokemon(player, egg.obtainMale());
              RewardsUtils.saveRewardPokemon(player, egg.obtainFemale());
              egg.getEggs().forEach(pokemon -> {
                try {
                  RewardsUtils.saveRewardPokemon(player, Pokemon.Companion.loadFromJSON(pokemon));
                } catch (NoPokemonStoreException e) {
                  throw new RuntimeException(e);
                }
              });
            }

            playerEggs = playerEggs.subList(0, CobbleUtils.breedconfig.getMaxplots());
          }
          eggs.put(player.getUuid(), playerEggs);
          writeInfo(player);
        } catch (IOException e) {
          e.printStackTrace();
        } catch (NoPokemonStoreException e) {
          throw new RuntimeException(e);
        }
      } else {
        eggs.put(player.getUuid(), new ArrayList<>());
        for (int i = 0; i < CobbleUtils.breedconfig.getMaxplots(); i++) {
          eggs.get(player.getUuid()).add(new PlotBreeding());
        }
        writeInfo(player);
      }

    });

  }

  public void remove(ServerPlayerEntity player) {
    writeInfo(player).join();
  }

  public void checking(ServerPlayerEntity player) {
    eggs.computeIfAbsent(player.getUuid(), k -> new ArrayList<>())
      .forEach(plotBreeding -> plotBreeding.checking(player));
  }

  public CompletableFuture<Void> writeInfo(ServerPlayerEntity player) {
    return CompletableFuture.runAsync(() -> {
      File playerFile = Utils.getAbsolutePath(CobbleUtils.PATH_BREED_DATA + player.getUuid() + ".json");
      List<PlotBreeding> playerEggs = eggs.get(player.getUuid());
      try (FileWriter writer = new FileWriter(playerFile)) {
        Utils.newWithoutSpacingGson().toJson(playerEggs, writer);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public CompletableFuture<Void> writeInfo(UUID uuid) {
    return CompletableFuture.runAsync(() -> {
      File playerFile = Utils.getAbsolutePath(CobbleUtils.PATH_BREED_DATA + uuid + ".json");
      List<PlotBreeding> playerEggs = eggs.get(uuid);
      try (FileWriter writer = new FileWriter(playerFile)) {
        Utils.newWithoutSpacingGson().toJson(playerEggs, writer);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }
}
