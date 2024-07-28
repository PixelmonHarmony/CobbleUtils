package com.kingpixel.cobbleutils.Model;

/**
 * @author Carlos Varas Alonso - 27/07/2024 13:13
 */
public class DataBaseConfig {
  private DataBaseType type;
  private String url;
  private String user;
  private String password;

  public DataBaseConfig() {
    this.type = DataBaseType.JSON;
    this.url = "";
    this.user = "admin";
    this.password = "admin";
  }

  public DataBaseConfig(DataBaseType type, String url, String user, String password) {
    this.type = type;
    this.url = url;
    this.user = user;
    this.password = password;
  }
}
