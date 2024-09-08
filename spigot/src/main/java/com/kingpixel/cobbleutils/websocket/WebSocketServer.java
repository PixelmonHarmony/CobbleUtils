package com.kingpixel.cobbleutils.websocket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.types.PermissionNode;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketServer extends org.java_websocket.server.WebSocketServer {

  private static final Logger LOGGER = Logger.getLogger(WebSocketServer.class.getName());
  private static final int PORT = 49154;
  private static Economy econ = null;
  private static LuckPerms luckPerms = null;

  public WebSocketServer() {
    super(new InetSocketAddress(PORT));
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    LOGGER.log(Level.INFO, "Nueva conexión desde: " + conn.getRemoteSocketAddress());
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    LOGGER.log(Level.INFO, "Conexión cerrada desde: " + conn.getRemoteSocketAddress() + " Razón: " + reason);
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    try {
      JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
      String action = getStringProperty(jsonObject, "action");

      if (action == null) {
        sendErrorResponse(conn, "Action missing in the request.");
        return;
      }

      Long requestId = getRequestId(jsonObject);
      if (requestId == null) {
        sendErrorResponse(conn, "Request ID missing or invalid.");
        return;
      }

      switch (action) {
        case "getBalance" -> handleGetBalance(conn, jsonObject, requestId);
        case "hasEnough" -> handleHasEnough(conn, jsonObject, requestId);
        case "addMoney" -> handleAddMoney(conn, jsonObject, requestId);
        case "removeMoney" -> handleRemoveMoney(conn, jsonObject, requestId);
        case "checkPermission" -> handleCheckPermission(conn, jsonObject, requestId);
        case "addPermission" -> handleAddPermission(conn, jsonObject, requestId);
        default -> sendErrorResponse(conn, "Unknown action: " + action);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error al procesar el mensaje JSON: " + e.getMessage(), e);
      sendErrorResponse(conn, "Invalid JSON format");
    }
  }

  private void handleAddPermission(WebSocket conn, JsonObject jsonObject, Long requestId) {
    try {
      String permission = getStringProperty(jsonObject, "permission");
      if (permission == null) {
        sendErrorResponse(conn, "Permission missing in the request.");
        return;
      }
      PermissionNode.builder(permission).build();
      luckPerms.getNodeBuilderRegistry().forPermission().permission(permission).build();

      sendSuccessResponse(conn, requestId);
    } catch (Exception e) {
      sendErrorResponse(conn, "Error processing addPermission action: " + e.getMessage());
    }
  }

  private void handleAddMoney(WebSocket conn, JsonObject jsonObject, Long requestId) {
    try {
      EconomyResponse response = addMoney(jsonObject);
      sendEconomyResponse(conn, requestId, response);
    } catch (Exception e) {
      sendErrorResponse(conn, "Invalid parameters for addMoney action.");
    }
  }

  private EconomyResponse addMoney(JsonObject jsonObject) {
    return econ.depositPlayer(getPlayer(getUserId(jsonObject)), getAmount(jsonObject));
  }

  private void handleRemoveMoney(WebSocket conn, JsonObject jsonObject, Long requestId) {
    try {
      EconomyResponse response = removeMoney(jsonObject);
      sendEconomyResponse(conn, requestId, response);
    } catch (Exception e) {
      sendErrorResponse(conn, "Invalid parameters for removeMoney action.");
    }
  }

  private EconomyResponse removeMoney(JsonObject jsonObject) {
    return econ.withdrawPlayer(getPlayer(getUserId(jsonObject)), getAmount(jsonObject));
  }

  private void handleHasEnough(WebSocket conn, JsonObject jsonObject, Long requestId) {
    try {
      boolean result = hasEnough(jsonObject);
      sendBooleanResponse(conn, requestId, result);
    } catch (Exception e) {
      sendErrorResponse(conn, "Invalid parameters for hasEnough action.");
    }
  }

  private boolean hasEnough(JsonObject jsonObject) {
    return econ.has(getPlayer(getUserId(jsonObject)), getAmount(jsonObject));
  }

  private void handleGetBalance(WebSocket conn, JsonObject jsonObject, Long requestId) {
    try {
      UUID userId = getUserId(jsonObject);
      double balance = econ.getBalance(getPlayer(userId));

      JsonObject response = new JsonObject();
      response.addProperty("requestId", requestId);
      response.addProperty("status", "success");
      response.addProperty("balance", balance);

      conn.send(response.toString());
    } catch (Exception e) {
      sendErrorResponse(conn, "Invalid parameters for getBalance action.");
    }
  }

  private void handleCheckPermission(WebSocket conn, JsonObject jsonObject, Long requestId) {
    try {
      UUID playerUUID = getUserId(jsonObject);
      String permission = getStringProperty(jsonObject, "permission");

      if (permission == null) {
        sendErrorResponse(conn, "Permission missing in the request.");
        return;
      }

      boolean hasPermission = checkPermission(playerUUID, permission);
      sendBooleanResponse(conn, requestId, hasPermission);
    } catch (Exception e) {
      sendErrorResponse(conn, "Error processing checkPermission action: " + e.getMessage());
    }
  }

  private boolean checkPermission(UUID playerUUID, String permission) {
    try {
      return luckPerms.getUserManager().getUser(playerUUID).getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error checking permission: " + e.getMessage(), e);
      return false;
    }
  }

  private String getStringProperty(JsonObject jsonObject, String propertyName) {
    JsonElement element = jsonObject.get(propertyName);
    return element != null && !element.isJsonNull() ? element.getAsString() : null;
  }

  private Long getRequestId(JsonObject jsonObject) {
    JsonElement requestIdElement = jsonObject.get("requestId");
    return requestIdElement != null && !requestIdElement.isJsonNull() ? requestIdElement.getAsLong() : null;
  }

  private Double getAmount(JsonObject jsonObject) {
    JsonElement amountElement = jsonObject.get("amount");
    return amountElement != null && !amountElement.isJsonNull() ? amountElement.getAsDouble() : null;
  }

  private UUID getUserId(JsonObject jsonObject) {
    String uuidString = getStringProperty(jsonObject, "playerUUID");
    return uuidString != null ? UUID.fromString(uuidString) : null;
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

  private void sendSuccessResponse(WebSocket conn, Long requestId) {
    JsonObject response = new JsonObject();
    response.addProperty("requestId", requestId);
    response.addProperty("status", "success");
    conn.send(response.toString());
  }

  private void sendEconomyResponse(WebSocket conn, Long requestId, EconomyResponse response) {
    JsonObject responseJson = new JsonObject();
    responseJson.addProperty("requestId", requestId);
    responseJson.addProperty("status", response.type == EconomyResponse.ResponseType.SUCCESS ? "success" : "unsuccess");
    conn.send(responseJson.toString());
  }

  private void sendBooleanResponse(WebSocket conn, Long requestId, boolean result) {
    JsonObject response = new JsonObject();
    response.addProperty("requestId", requestId);
    response.addProperty("status", result ? "success" : "unsuccess");
    conn.send(response.toString());
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
    setupLuckPerms();
    WebSocketServer server = new WebSocketServer();
    server.start();
  }

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

  public static boolean setupLuckPerms() {
    try {
      luckPerms = LuckPermsProvider.get();
      return luckPerms != null;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error setting up LuckPerms: " + e.getMessage(), e);
      return false;
    }
  }
}
