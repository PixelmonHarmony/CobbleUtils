package com.kingpixel.cobbleutils.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kingpixel.cobbleutils.CobbleUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {

  private static final Logger LOGGER = Logger.getLogger(WebSocketClient.class.getName());
  private static volatile WebSocketClient INSTANCE;

  private static final AtomicLong REQUEST_ID_GENERATOR = new AtomicLong();  // Unique ID generator for requests
  private static final ConcurrentHashMap<Long, CompletableFuture<?>> pendingRequests = new ConcurrentHashMap<>();  // Store pending request futures

  private static final int MAX_RETRY_ATTEMPTS = 5;
  private static final int RETRY_DELAY_MS = 2000;

  public static WebSocketClient getInstance() {
    if (INSTANCE == null || !INSTANCE.isOpen()) {
      synchronized (WebSocketClient.class) {
        if (INSTANCE == null || !INSTANCE.isOpen()) {
          boolean success = init();
          if (!success) {
            LOGGER.log(Level.SEVERE, "Failed to initialize WebSocket client.");
            EconomyUtil.economyType = null;
            LuckPermsUtil.PERMISSION_TYPE = null;
            return null;
          }
        }
      }
    }
    return null;
  }

  private WebSocketClient(String serverUri) throws URISyntaxException {
    super(new URI(serverUri));
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    LOGGER.log(Level.INFO, "Connected to WebSocket server.");
  }

  @Override
  public void onMessage(String message) {
    if (CobbleUtils.config.isDebug()) {
      LOGGER.log(Level.INFO, "Message received from server: " + message);
    }

    // Parse the received message from the server
    try {
      JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

      Long requestId = jsonObject.has("requestId") ? jsonObject.get("requestId").getAsLong() : null;

      if (requestId != null) {
        CompletableFuture<?> future = pendingRequests.remove(requestId);  // Get the corresponding future

        if (future == null) {
          LOGGER.log(Level.WARNING, "No matching request found for requestId: " + requestId);
          return;
        }

        handleResponse(jsonObject, future);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error processing message from server: " + message, e);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void handleResponse(JsonObject jsonObject, CompletableFuture<T> future) {
    try {
      String status = jsonObject.has("status") ? jsonObject.get("status").getAsString() : null;

      if (jsonObject.has("balance")) {
        double balance = jsonObject.get("balance").getAsDouble();
        ((CompletableFuture<Double>) future).complete(balance);
      } else if (status != null) {
        boolean success = status.equals("success");
        ((CompletableFuture<Boolean>) future).complete(success);
      } else {
        LOGGER.log(Level.WARNING, "Server response without status.");
        future.completeExceptionally(new RuntimeException("Server response without status."));
      }
    } finally {
      pendingRequests.remove(jsonObject.get("requestId").getAsLong());
    }
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    LOGGER.log(Level.INFO, "Connection closed. Reason: " + reason);
    // Set INSTANCE to null when the connection is closed
    INSTANCE = null;
  }

  @Override
  public void onError(Exception ex) {
    LOGGER.log(Level.SEVERE, "WebSocket client error: " + ex.getMessage(), ex);
  }

  // Method to request a player's balance
  public CompletableFuture<Double> getBalance(UUID playerUUID, String currency) {
    return sendRequest(playerUUID, currency, 0, "getBalance", new CompletableFuture<>());
  }

  // Method to check if a player has enough money
  public CompletableFuture<Boolean> hasEnough(UUID playerUUID, String currency, double amount) {
    return sendRequest(playerUUID, currency, amount, "hasEnough", new CompletableFuture<>());
  }

  // Method to add money to a player
  public CompletableFuture<Boolean> addMoney(UUID playerUUID, String currency, double amount) {
    return sendRequest(playerUUID, currency, amount, "addMoney", new CompletableFuture<>());
  }

  // Method to remove money from a player
  public CompletableFuture<Boolean> removeMoney(UUID playerUUID, String currency, double amount) {
    return sendRequest(playerUUID, currency, amount, "removeMoney", new CompletableFuture<>());
  }

  private static boolean init() {
    int attempt = 0;
    while (attempt < MAX_RETRY_ATTEMPTS) {
      try {
        WebSocketClient client = new WebSocketClient("ws://localhost:49154");
        client.connectBlocking();  // Blocks until the connection is open
        if (client.isOpen()) {
          INSTANCE = client;
          return true;
        }
      } catch (URISyntaxException | InterruptedException e) {
        LOGGER.log(Level.SEVERE, "Error initializing WebSocket client (Attempt " + (attempt + 1) + "): " + e.getMessage(), e);
      }
      attempt++;
      try {
        Thread.sleep(RETRY_DELAY_MS);  // Wait before retrying
      } catch (InterruptedException ignored) {
      }
    }
    return false;
  }

  // Method to add a permission
  public void addPermission(String permission) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    sendRequest(null, null, 0, "addPermission", future).thenAccept(success -> {
      if (CobbleUtils.config.isDebug()) {
        if (success) {
          LOGGER.log(Level.INFO, "Permission successfully added: " + permission);
        } else {
          LOGGER.log(Level.WARNING, "Error adding permission: " + permission);
        }
      }
    });
  }

  // Method to check a player's permission
  public CompletableFuture<Boolean> checkPermission(ServerPlayerEntity player, String permission) {
    return sendRequest(UUID.fromString(player.getUuid().toString()), null, 0, "checkPermission", new CompletableFuture<>(), permission);
  }

  @NotNull
  private <T> CompletableFuture<T> sendRequest(UUID playerUUID, String currency, double amount, String action, CompletableFuture<T> future) {
    return sendRequest(playerUUID, currency, amount, action, future, null);
  }

  @NotNull
  private <T> CompletableFuture<T> sendRequest(UUID playerUUID, String currency, double amount, String action, CompletableFuture<T> future, String permission) {
    long requestId = REQUEST_ID_GENERATOR.incrementAndGet();
    JsonObject request = new JsonObject();
    request.addProperty("requestId", requestId);
    request.addProperty("action", action);

    if (playerUUID != null) {
      request.addProperty("playerUUID", playerUUID.toString());
    }
    if (currency != null) {
      request.addProperty("currency", currency);
    }
    if (amount != 0) {
      request.addProperty("amount", amount);
    }
    if (permission != null) {
      request.addProperty("permission", permission);
    }

    pendingRequests.put(requestId, future);
    send(request.toString());

    // Set a timeout for the request
    future.orTimeout(30, TimeUnit.SECONDS).exceptionally(ex -> {
      pendingRequests.remove(requestId);
      LOGGER.log(Level.WARNING, "Request with requestId " + requestId + " has timed out.", ex);
      return null;
    });

    return future;
  }
}
