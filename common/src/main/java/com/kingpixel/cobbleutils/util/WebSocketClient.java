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

  private static final AtomicLong REQUEST_ID_GENERATOR = new AtomicLong();  // Generador de IDs únicos para las solicitudes
  private static final ConcurrentHashMap<Long, CompletableFuture<?>> pendingRequests = new ConcurrentHashMap<>();  // Almacenar futuros de solicitudes pendientes

  private static final int MAX_RETRY_ATTEMPTS = 5;
  private static final int RETRY_DELAY_MS = 2000;

  public static WebSocketClient getInstance() {
    try {
      if (INSTANCE == null) {
        synchronized (WebSocketClient.class) {
          if (INSTANCE == null) {
            init();
          }
        }
      }
      return INSTANCE;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error al obtener la instancia del cliente WebSocket: " + e.getMessage(), e);
      return null;
    }
  }

  private WebSocketClient(String serverUri) throws URISyntaxException {
    super(new URI(serverUri));
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    LOGGER.log(Level.INFO, "Conectado al servidor WebSocket.");
  }

  @Override
  public void onMessage(String message) {
    if (CobbleUtils.config.isDebug()) {
      LOGGER.log(Level.INFO, "Mensaje recibido del servidor: " + message);
    }

    // Parsear el mensaje recibido del servidor
    try {
      JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

      Long requestId = jsonObject.has("requestId") ? jsonObject.get("requestId").getAsLong() : null;

      if (requestId != null) {
        CompletableFuture<?> future = pendingRequests.remove(requestId);  // Obtener el futuro correspondiente

        if (future == null) {
          LOGGER.log(Level.WARNING, "No se encontró la solicitud correspondiente para requestId: " + requestId);
          return;
        }

        handleResponse(jsonObject, future);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error al procesar el mensaje del servidor: " + message, e);
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
        LOGGER.log(Level.WARNING, "Respuesta del servidor sin status.");
        future.completeExceptionally(new RuntimeException("Respuesta del servidor sin status."));
      }
    } finally {
      pendingRequests.remove(jsonObject.get("requestId").getAsLong());
    }
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    LOGGER.log(Level.INFO, "Conexión cerrada. Razón: " + reason);
  }

  @Override
  public void onError(Exception ex) {
    LOGGER.log(Level.SEVERE, "Error en el cliente WebSocket: " + ex.getMessage(), ex);
  }

  // Método para solicitar el balance de un jugador
  public CompletableFuture<Double> getBalance(UUID playerUUID, String currency) {
    return sendRequest(playerUUID, currency, 0, "getBalance", new CompletableFuture<>());
  }

  // Método para verificar si un jugador tiene suficiente dinero
  public CompletableFuture<Boolean> hasEnough(UUID playerUUID, String currency, double amount) {
    return sendRequest(playerUUID, currency, amount, "hasEnough", new CompletableFuture<>());
  }

  // Método para añadir dinero a un jugador
  public CompletableFuture<Boolean> addMoney(UUID playerUUID, String currency, double amount) {
    return sendRequest(playerUUID, currency, amount, "addMoney", new CompletableFuture<>());
  }

  // Método para remover dinero de un jugador
  public CompletableFuture<Boolean> removeMoney(UUID playerUUID, String currency, double amount) {
    return sendRequest(playerUUID, currency, amount, "removeMoney", new CompletableFuture<>());
  }

  public static boolean init() {
    int attempt = 0;
    while (attempt < MAX_RETRY_ATTEMPTS) {
      try {
        WebSocketClient client = new WebSocketClient("ws://localhost:8080");
        client.connectBlocking();  // Bloquea hasta que la conexión esté abierta
        INSTANCE = client;
        return true;
      } catch (URISyntaxException | InterruptedException e) {
        LOGGER.log(Level.SEVERE, "Error al iniciar el cliente WebSocket (Intento " + (attempt + 1) + "): " + e.getMessage(), e);
        attempt++;
        try {
          Thread.sleep(RETRY_DELAY_MS);  // Espera antes de intentar reconectar
        } catch (InterruptedException ignored) {
        }
      }
    }
    return false;
  }

  // Método para agregar un permiso
  public void addPermission(String permission) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    sendRequest(null, null, 0, "addPermission", future).thenAccept(success -> {
      if (CobbleUtils.config.isDebug()) {
        if (success) {
          LOGGER.log(Level.INFO, "Permiso agregado correctamente: " + permission);
        } else {
          LOGGER.log(Level.WARNING, "Error al agregar el permiso: " + permission);
        }
      }
    });
  }

  // Método para verificar un permiso de un jugador
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

    // Establecer un timeout para la solicitud
    future.orTimeout(30, TimeUnit.SECONDS).exceptionally(ex -> {
      pendingRequests.remove(requestId);
      LOGGER.log(Level.WARNING, "Solicitud con requestId " + requestId + " ha expirado.", ex);
      return null;
    });

    return future;
  }
}
