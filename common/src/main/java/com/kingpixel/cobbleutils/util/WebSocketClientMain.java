package com.kingpixel.cobbleutils.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketClientMain extends WebSocketClient {

  private static final Logger LOGGER = Logger.getLogger(WebSocketClientMain.class.getName());
  private static WebSocketClientMain INSTANCE;

  // Almacenar futuros para las respuestas
  private CompletableFuture<Boolean> addMoneyResponseFuture;
  private CompletableFuture<Boolean> hasEnoughResponseFuture;
  private CompletableFuture<Boolean> removeMoneyResponseFuture;
  private CompletableFuture<Double> getBalanceResponseFuture;

  public static WebSocketClientMain getInstance() {
    if (INSTANCE == null) {
      init();
    }
    return INSTANCE;
  }

  public WebSocketClientMain(String serverUri) throws URISyntaxException {
    super(new URI(serverUri));
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    LOGGER.log(Level.INFO, "Conectado al servidor WebSocket.");
  }

  @Override
  public void onMessage(String message) {
    LOGGER.log(Level.INFO, "Mensaje recibido del servidor: " + message);

    // Parsear el mensaje recibido del servidor
    JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
    String status = jsonObject.get("status").getAsString();

    // Identificar la acción correspondiente y completar la futura
    if (jsonObject.has("balance")) {
      double balance = jsonObject.get("balance").getAsDouble();
      if (getBalanceResponseFuture != null) {
        getBalanceResponseFuture.complete(balance);
        getBalanceResponseFuture = null;
      }
    } else if (status.equals("success")) {
      if (addMoneyResponseFuture != null) {
        addMoneyResponseFuture.complete(true);
        addMoneyResponseFuture = null;
      } else if (hasEnoughResponseFuture != null) {
        hasEnoughResponseFuture.complete(true);
        hasEnoughResponseFuture = null;
      } else if (removeMoneyResponseFuture != null) {
        removeMoneyResponseFuture.complete(true);
        removeMoneyResponseFuture = null;
      }
    } else if (status.equals("unsuccess")) {
      if (addMoneyResponseFuture != null) {
        addMoneyResponseFuture.complete(false);
        addMoneyResponseFuture = null;
      } else if (hasEnoughResponseFuture != null) {
        hasEnoughResponseFuture.complete(false);
        hasEnoughResponseFuture = null;
      } else if (removeMoneyResponseFuture != null) {
        removeMoneyResponseFuture.complete(false);
        removeMoneyResponseFuture = null;
      }
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
    JsonObject request = new JsonObject();
    request.addProperty("action", "getBalance");
    request.addProperty("playerUUID", playerUUID.toString());
    request.addProperty("currency", currency);
    send(request.toString());

    getBalanceResponseFuture = new CompletableFuture<>();
    return getBalanceResponseFuture;
  }

  // Método para verificar si un jugador tiene suficiente dinero
  public CompletableFuture<Boolean> hasEnough(UUID playerUUID, String currency, double amount) {
    JsonObject request = new JsonObject();
    request.addProperty("action", "hasEnough");
    request.addProperty("playerUUID", playerUUID.toString());
    request.addProperty("amount", amount);
    request.addProperty("currency", currency);
    send(request.toString());

    hasEnoughResponseFuture = new CompletableFuture<>();
    return hasEnoughResponseFuture;
  }

  // Método para añadir dinero a un jugador
  public CompletableFuture<Boolean> addMoney(UUID playerUUID, String currency, double amount) {
    JsonObject request = new JsonObject();
    request.addProperty("action", "addMoney");
    request.addProperty("playerUUID", playerUUID.toString());
    request.addProperty("amount", amount);
    request.addProperty("currency", currency);
    send(request.toString());

    addMoneyResponseFuture = new CompletableFuture<>();
    return addMoneyResponseFuture;
  }

  // Método para remover dinero de un jugador
  public CompletableFuture<Boolean> removeMoney(UUID playerUUID, String currency, double amount) {
    JsonObject request = new JsonObject();
    request.addProperty("action", "removeMoney");
    request.addProperty("playerUUID", playerUUID.toString());
    request.addProperty("amount", amount);
    request.addProperty("currency", currency);
    send(request.toString());

    removeMoneyResponseFuture = new CompletableFuture<>();
    return removeMoneyResponseFuture;
  }

  // Método para manejar la respuesta de error
  private void handleError(String errorMessage) {
    LOGGER.log(Level.SEVERE, "Error recibido del servidor: " + errorMessage);
  }

  public static boolean init() {
    try {
      // Iniciar el cliente WebSocket
      WebSocketClientMain client = new WebSocketClientMain("ws://localhost:8080");
      client.connect();

      // Esperar a que la conexión esté lista
      while (!client.isOpen()) {
        Thread.sleep(100);
      }
      INSTANCE = client;
      return true;
    } catch (URISyntaxException | InterruptedException e) {
      LOGGER.log(Level.SEVERE, "Error al iniciar el cliente WebSocket: " + e.getMessage(), e);
      return false;
    }
  }
}
