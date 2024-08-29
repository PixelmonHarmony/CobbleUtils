package com.kingpixel.cobbleutils.Model.shops;

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

@Getter
@ToString
@Data
public class ShopTransactions {
  // UUID (Player) -> Product -> TransactionSummary
  public static Map<UUID, Map<String, TransactionSummary>> transactions = new ConcurrentHashMap<>();

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

    public BigDecimal getTotalSpent() {
      return totalBoughtPrice;
    }

    public BigDecimal getTotalEarned() {
      return totalSoldPrice;
    }

    public BigDecimal getNet() {
      return totalSoldPrice.subtract(totalBoughtPrice);
    }

    public BigDecimal getTotalMoneySpent() {
      return totalBoughtPrice;
    }

    public BigDecimal getTotalBoughtQuantity() {
      return totalBoughtAmount;
    }

    public BigDecimal getTotalSoldQuantity() {
      return totalSoldAmount;
    }
  }

  private static boolean firstTime = false;

  public static synchronized void addTransaction(UUID player, Shop shop, ShopAction action, Shop.Product product, BigDecimal amount, BigDecimal price) {
    transactions.computeIfAbsent(player, k -> new HashMap<>())
      .computeIfAbsent(product.getProduct(), k -> new TransactionSummary()); // Usa el ID del producto como clave

    TransactionSummary summary = transactions.get(player).get(product.getProduct());

    if (action == ShopAction.BUY) {
      summary.addBuy(amount, price);
    } else if (action == ShopAction.SELL) {
      summary.addSell(amount, price);
    }
    summary.setCurrency(shop.getCurrency());
  }

  public static synchronized void addTransaction(UUID player, String currency, ShopAction action, Shop.Product product, BigDecimal amount, BigDecimal price) {
    transactions.computeIfAbsent(player, k -> new HashMap<>())
      .computeIfAbsent(product.getProduct(), k -> new TransactionSummary()); // Usa el ID del producto como clave

    TransactionSummary summary = transactions.get(player).get(product.getProduct());

    if (action == ShopAction.BUY) {
      summary.addBuy(amount, price);
    } else if (action == ShopAction.SELL) {
      summary.addSell(amount, price);
    }
    summary.setCurrency(currency);
  }

  public static synchronized void updateTransaction(UUID player, ShopMenu shopMenu) {
    loadTransactions(shopMenu).thenRun(() -> {
      List<Shop.Product> currentProducts = shopMenu.getAllProducts();

      transactions.computeIfPresent(player, (uuid, productMap) -> {
        productMap.keySet().removeIf(productId -> {
          // Filtra los productos que no están en los productos actuales
          Shop.Product product = shopMenu.getProductById(productId);
          return product == null || !currentProducts.contains(product);
        });
        return productMap.isEmpty() ? null : productMap;
      });

      Utils.writeFileAsync(shopMenu.getLogg(), player + ".json", Utils.newWithoutSpacingGson().toJson(transactions.get(player)))
        .exceptionally(ex -> {
          ex.printStackTrace(); // Manejo de excepciones
          return null;
        });
    }).exceptionally(ex -> {
      ex.printStackTrace(); // Manejo de excepciones durante la carga
      return null;
    });
  }

  public static CompletableFuture<Void> loadTransactions(ShopMenu shopMenu) {
    if (firstTime) {
      return CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.runAsync(() -> {
      File folder = Utils.getAbsolutePath(shopMenu.getLogg());
      File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
      if (files == null) return;

      for (File file : files) {
        try {
          // Leer el archivo JSON
          String data = Utils.readFileSync(file);

          // Deserializar el JSON
          Gson gson = Utils.newWithoutSpacingGson();
          Type type = new TypeToken<Map<String, TransactionSummary>>() {
          }.getType();
          Map<String, TransactionSummary> map = gson.fromJson(data, type);

          if (map == null) {
            System.err.println("El archivo " + file.getName() + " contiene un JSON malformado.");
            continue;
          }

          // Procesar y convertir el ID del producto de vuelta a Shop.Product
          Map<String, TransactionSummary> processedMap = new HashMap<>();
          map.forEach((productId, summary) -> {
            Shop.Product product = shopMenu.getProductById(productId);
            if (product != null) {
              processedMap.put(productId, summary);
            }
          });

          // Almacenar las transacciones procesadas
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
