package com.kingpixel.cobbleutils.features.shops;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@ToString
@Data
public class ShopTransactions {

  // UUID (Player) -> Product -> TransactionSummary
  public static final Map<UUID, Map<String, TransactionSummary>> transactions = new ConcurrentHashMap<>();
  private static final ReentrantLock writeLock = new ReentrantLock();
  private static boolean firstTime = false;

  @Getter
  @ToString
  @Data
  public static class TransactionSummary {
    private String currency = "";
    private BigDecimal totalBoughtAmount = BigDecimal.ZERO;
    private BigDecimal totalBoughtPrice = BigDecimal.ZERO;
    private BigDecimal totalSoldAmount = BigDecimal.ZERO;
    private BigDecimal totalSoldPrice = BigDecimal.ZERO;

    public void addBuy(BigDecimal amount, BigDecimal price) {
      totalBoughtAmount = totalBoughtAmount.add(amount);
      totalBoughtPrice = totalBoughtPrice.add(price);
    }

    public void addSell(BigDecimal amount, BigDecimal price) {
      totalSoldAmount = totalSoldAmount.add(amount);
      totalSoldPrice = totalSoldPrice.add(price);
    }

    // Devuelve el total de dinero gastado en compras
    public BigDecimal getTotalSpent() {
      return totalBoughtPrice;
    }

    // Devuelve el total de dinero ganado con las ventas
    public BigDecimal getTotalEarned() {
      return totalSoldPrice;
    }

    // Devuelve la diferencia neta entre lo ganado y lo gastado
    public BigDecimal getNet() {
      return totalSoldPrice.subtract(totalBoughtPrice);
    }

    // Devuelve la cantidad total comprada
    public BigDecimal getTotalBoughtQuantity() {
      return totalBoughtAmount;
    }

    // Devuelve la cantidad total vendida
    public BigDecimal getTotalSoldQuantity() {
      return totalSoldAmount;
    }
  }


  public static synchronized void addTransaction(UUID player, Shop shop, ShopAction action, Shop.Product product, BigDecimal amount, BigDecimal price) {
    addTransaction(player, shop.getCurrency(), action, product, amount, price);
  }

  public static synchronized void addTransaction(UUID player, String currency, ShopAction action, Shop.Product product, BigDecimal amount, BigDecimal price) {
    transactions.computeIfAbsent(player, k -> new HashMap<>())
      .computeIfAbsent(product.getProduct(), k -> new TransactionSummary())
      .setCurrency(currency);

    TransactionSummary summary = transactions.get(player).get(product.getProduct());

    if (action == ShopAction.BUY) {
      summary.addBuy(amount, price);
    } else if (action == ShopAction.SELL) {
      summary.addSell(amount, price);
    }
  }

  public static synchronized void updateTransaction(UUID player, ShopConfigMenu shopConfigMenu) {
    loadTransactions(shopConfigMenu).thenRun(() -> {
      List<Shop.Product> currentProducts = shopConfigMenu.getAllProducts();

      transactions.computeIfPresent(player, (uuid, productMap) -> {
        productMap.keySet().removeIf(productId -> shopConfigMenu.getProductById(productId) == null);
        return productMap.isEmpty() ? null : productMap;
      });

      // Escribe el archivo de manera segura usando un lock para evitar concurrencia
      writeLock.lock();
      try {
        Utils.writeFileAsync(shopConfigMenu.getLogg(), player + ".json", Utils.newWithoutSpacingGson().toJson(transactions.get(player)))
          .exceptionally(ex -> {
            ex.printStackTrace(); // Manejo de excepciones
            return null;
          });
      } finally {
        writeLock.unlock();
      }
    }).exceptionally(ex -> {
      ex.printStackTrace(); // Manejo de excepciones durante la carga
      return null;
    });
  }

  public static CompletableFuture<Void> loadTransactions(ShopConfigMenu shopConfigMenu) {
    if (firstTime) {
      return CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.runAsync(() -> {
      File folder = Utils.getAbsolutePath(shopConfigMenu.getLogg());
      File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
      if (files == null) return;

      for (File file : files) {
        try {
          String data = Utils.readFileSync(file);
          Gson gson = Utils.newWithoutSpacingGson();
          Type type = new TypeToken<Map<String, TransactionSummary>>() {
          }.getType();
          Map<String, TransactionSummary> map = gson.fromJson(data, type);

          if (map == null) {
            System.err.println("El archivo " + file.getName() + " contiene un JSON malformado.");
            continue;
          }

          Map<String, TransactionSummary> processedMap = new HashMap<>();
          map.forEach((productId, summary) -> {
            Shop.Product product = shopConfigMenu.getProductById(productId);
            if (product != null) {
              processedMap.put(productId, summary);
            }
          });

          transactions.put(UUID.fromString(file.getName().replace(".json", "")), processedMap);
        } catch (JsonSyntaxException e) {
          System.err.println("Error de sintaxis JSON en el archivo " + file.getName() + ": " + e.getMessage());
        } catch (Exception e) {
          System.err.println("Error al procesar el archivo " + file.getName() + ": " + e.getMessage());
        }
      }

      firstTime = true;
    });
  }

  public enum ShopAction {
    BUY,
    SELL
  }
}