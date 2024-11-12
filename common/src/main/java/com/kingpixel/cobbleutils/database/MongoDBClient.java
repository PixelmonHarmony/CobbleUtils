package com.kingpixel.cobbleutils.database;

import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.features.breeding.models.PlotBreeding;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDBClient implements DatabaseClient {
  private final MongoClient mongoClient;
  private final MongoDatabase database;
  private final MongoCollection<Document> plotCollection;

  public MongoDBClient(DataBaseConfig databaseConfig) {
    MongoClient tempClient = null;
    MongoDatabase tempDatabase = null;
    MongoCollection<Document> tempCollection = null;

    try {
      // Verifica y formatea la URL para asegurar que no haya conflictos con el puerto
      String dbUrl = validateDatabaseUrl(databaseConfig.getUrl());

      // Crear el cliente MongoDB usando la URL con puerto especificado
      tempClient = MongoClients.create(dbUrl);
      tempDatabase = tempClient.getDatabase(databaseConfig.getDatabase());
      tempCollection = tempDatabase.getCollection("plotBreeding");

      // Forzar la creación de la colección si no existe
      if (!tempDatabase.listCollectionNames().into(new ArrayList<>()).contains("plotBreeding")) {
        Document tempDoc = new Document("temp", "createCollection");
        tempCollection.insertOne(tempDoc);
        tempCollection.deleteOne(tempDoc);
      }

    } catch (IllegalArgumentException | MongoException e) {
      e.printStackTrace();
      System.out.println("Error inicializando MongoDBClient: " + e.getMessage());
      // Cerrar la conexión en caso de error
      if (tempClient != null) {
        tempClient.close();
      }
      throw new RuntimeException("No se pudo inicializar MongoDBClient", e);
    }

    this.mongoClient = tempClient;
    this.database = tempDatabase;
    this.plotCollection = tempCollection;
  }

  private String validateDatabaseUrl(String url) {
    // Asegura que no esté usando mongodb+srv si se especifica el puerto
    if (url.startsWith("mongodb+srv://")) {
      throw new IllegalArgumentException("mongodb+srv no permite especificar un puerto. Usa 'mongodb://' en su lugar.");
    }
    return url;
  }

  @Override
  public void connect() {
    // Puedes implementar una verificación simple para ver si la conexión está activa
    try {
      database.listCollectionNames().first(); // Intenta acceder a la base de datos
    } catch (MongoException e) {
      e.printStackTrace();
      System.out.println("Error conectando a MongoDB: " + e.getMessage());
    }
  }

  @Override
  public void disconnect() {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

  @Override
  public void save() {
    // Este método puede no ser necesario si los datos se guardan en tiempo real
  }

  @Override
  public List<PlotBreeding> getPlots(ServerPlayerEntity player) {
    List<PlotBreeding> plots = new ArrayList<>();
    String playerId = player.getUuidAsString();

    try {
      Document playerDoc = plotCollection.find(Filters.eq("playerId", playerId)).first();

      // Si no se encuentra el jugador en la base de datos, realizamos el "checking"
      if (playerDoc == null) {
        System.out.println("No se encontraron plots para el jugador con ID: " + playerId);
      } else {
        // Si el jugador tiene plots, los agregamos a la lista
        List<Document> plotDocs = (List<Document>) playerDoc.get("plots");
        for (Document doc : plotDocs) {
          plots.add(PlotBreeding.fromDocument(doc));
        }
      }
    } catch (MongoException e) {
      e.printStackTrace();
      System.out.println("Error obteniendo los plots: " + e.getMessage());
    }

    return plots;
  }

  @Override
  public void savePlots(ServerPlayerEntity player, List<PlotBreeding> plots) {
    String playerId = player.getUuidAsString();
    ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true); // Reemplaza si existe, inserta si no

    try {
      List<Document> plotDocs = new ArrayList<>();
      for (PlotBreeding plot : plots) {
        plotDocs.add(plot.toDocument(playerId));
      }

      // Crear un documento que contiene la lista de plots para el jugador
      Document playerDoc = new Document("playerId", playerId)
        .append("plots", plotDocs);

      // Reemplaza o inserta el documento del jugador
      plotCollection.replaceOne(Filters.eq("playerId", playerId), playerDoc, replaceOptions);
    } catch (MongoException e) {
      e.printStackTrace();
      System.out.println("Error guardando los plots: " + e.getMessage());
    }
  }

  @Override
  public void checkDaycarePlots(ServerPlayerEntity player) {
    DatabaseClientFactory.CheckDaycarePlots(player);
  }

  @Override
  public void removeDataIfNecessary(ServerPlayerEntity player) {
    // Implementación opcional para eliminar datos antiguos si es necesario
  }
}
