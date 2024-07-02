package com.kingpixel.cobbleutils.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.MoneyChance;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 25/06/2024 22:19
 */
@Getter
public class PoolMoney {
  private Map<String, List<MoneyChance>> randomMoney;


  public PoolMoney() {
    randomMoney = Map.of(
      "a", List.of(new MoneyChance(1, 100)),
      "b", List.of(new MoneyChance(2, 100), new MoneyChance(3, 100))
    );
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_RANDOM, "money.json",
      el -> {
        Gson gson = Utils.newGson();
        PoolMoney config = gson.fromJson(el, PoolMoney.class);
        randomMoney = config.getRandomMoney();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_RANDOM, "money.json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write money.json file for" + CobbleUtils.MOD_NAME + " .");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No money.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_RANDOM, "money.json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write money.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }

  }

  /**
   * Método para obtener un ítem aleatorio basado en las probabilidades configuradas.
   *
   * @param category La categoría de ítems de la que seleccionar.
   *
   * @return El ítem seleccionado según las probabilidades.
   */
  public int getRandomMoney(String category) {
    List<MoneyChance> moneys = randomMoney.get(category);
    if (moneys == null || moneys.isEmpty()) {
      return 0;
    }

    int totalWeight = moneys.stream().mapToInt(MoneyChance::getChance).sum();
    int randomValue = Utils.RANDOM.nextInt(totalWeight) + 1;

    int currentWeight = 0;
    for (MoneyChance itemChance : moneys) {
      currentWeight += itemChance.getChance();
      if (randomValue <= currentWeight) {
        return itemChance.getMoney();
      }
    }
    return 0;
  }


}
