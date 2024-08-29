package com.kingpixel.cobbleutils.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketMain extends WebSocketServer {

  private static final Logger LOGGER = Logger.getLogger(WebSocketMain.class.getName());
  private static final int PORT = 8080;
  private static Economy econ = null;

  public static boolean setupEconomy() {
    if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    econ = rsp.getProvider();
    return econ != null;
  }

  public WebSocketMain() {
    super(new InetSocketAddress(PORT));
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    LOGGER.log(Level.INFO, "Nueva conexión desde: " + conn.getRemoteSocketAddress());
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    LOGGER.log(Level.INFO, "Conexión cerrada desde: " + conn.getRemoteSocketAddress());
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    //LOGGER.log(Level.INFO, "Mensaje recibido: " + message);
    try {
      // Parsear el mensaje como JSON
      JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

      // Obtener la ruta (acción) del mensaje
      String action = jsonObject.get("action").getAsString(); // Ruta enviada por el cliente

      // Manejar diferentes rutas
      switch (action) {
        case "getBalance":
          handleGetBalance(conn, jsonObject);
          break;
        case "hasEnough":
          handleHasEnough(conn, jsonObject);
          break;
        case "addMoney":
          handleAddMoney(conn, jsonObject);
          break;
        case "removeMoney":
          handleRemoveMoney(conn, jsonObject);
          break;
        default:
          sendErrorResponse(conn, "Unknown action: " + action);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error al procesar el mensaje JSON: " + e.getMessage());
      JsonObject errorResponse = new JsonObject();
      errorResponse.addProperty("status", "error");
      errorResponse.addProperty("message", "Invalid JSON format");
      conn.send(errorResponse.toString());
    }
  }

  private void handleRemoveMoney(WebSocket conn, JsonObject jsonObject) {
    try {
      JsonObject response = new JsonObject();
      if (removeMoney(jsonObject).type == EconomyResponse.ResponseType.SUCCESS) {
        response.addProperty("status", "success");
      } else {
        response.addProperty("status", "unsuccess");
      }

      conn.send(response.toString());
    } catch (Exception e) {
      sendErrorResponse(conn, "Invalid parameters for addmoney action.");
    }
  }

  private EconomyResponse removeMoney(JsonObject jsonObject) {
    return econ.withdrawPlayer(getPlayer(getUserId(jsonObject)), getAmount(jsonObject));
  }

  private void handleHasEnough(WebSocket conn, JsonObject jsonObject) {
    try {

      JsonObject response = new JsonObject();
      if (hasEnough(jsonObject)) {
        response.addProperty("status", "success");
      } else {
        response.addProperty("status", "unsuccess");
      }

      conn.send(response.toString());
    } catch (Exception e) {
      sendErrorResponse(conn, "Invalid parameters for hasEnough action.");
    }
  }

  private boolean hasEnough(JsonObject jsonObject) {
    boolean result = econ.has(getPlayer(getUserId(jsonObject)), getAmount(jsonObject));
    if (result) {
      removeMoney(jsonObject);
      return true;
    }
    return false;
  }

  private void handleAddMoney(WebSocket conn, JsonObject jsonObject) {
    try {
      JsonObject response = new JsonObject();
      if (addMoney(jsonObject).type == EconomyResponse.ResponseType.SUCCESS) {
        response.addProperty("status", "success");
      } else {
        response.addProperty("status", "unsuccess");
      }

      conn.send(response.toString());
    } catch (Exception e) {
      sendErrorResponse(conn, "Invalid parameters for addMoney action.");
    }
  }

  private EconomyResponse addMoney(JsonObject jsonObject) {
    return econ.depositPlayer(getPlayer(getUserId(jsonObject)), getAmount(jsonObject));
  }

  private void handleGetBalance(WebSocket conn, JsonObject jsonObject) {
    try {
      UUID userId = getUserId(jsonObject);

      JsonObject response = new JsonObject();
      response.addProperty("status", getStatus(true));
      response.addProperty("balance", econ.getBalance(getPlayer(userId)));

      conn.send(response.toString());
    } catch (Exception e) {
      sendErrorResponse(conn, "Invalid parameters for getBalance action.");
    }
  }

  private String getStatus(boolean status) {
    return status ? "success" : "unsuccess";
  }

  private UUID getUserId(JsonObject jsonObject) {
    return UUID.fromString(jsonObject.get("playerUUID").getAsString());
  }

  private double getAmount(JsonObject jsonObject) {
    return jsonObject.get("amount").getAsDouble();
  }

  private OfflinePlayer getPlayer(UUID playerUUID) {
    return Bukkit.getOfflinePlayer(playerUUID);
  }

  private void sendErrorResponse(WebSocket conn, String message) {
    JsonObject errorResponse = new JsonObject();
    errorResponse.addProperty("status", "error");
    errorResponse.addProperty("message", message);
    conn.send(errorResponse.toString());
  }

  @Override
  public void onError(WebSocket conn, Exception ex) {
    LOGGER.log(Level.SEVERE, "Error en el servidor WebSocket: " + ex.getMessage(), ex);
  }

  @Override
  public void onStart() {
    LOGGER.log(Level.INFO, "Servidor WebSocket iniciado en el puerto " + PORT);
  }

  public static void init() {
    setupEconomy();
    WebSocketMain server = new WebSocketMain();
    server.start();
  }
}
