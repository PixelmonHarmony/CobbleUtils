package com.kingpixel.cobbleutils.database;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:02
 */
public interface DatabaseClient {
  void connect();


  void disconnect();

  void save();
}
