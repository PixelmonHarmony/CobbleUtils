package com.kingpixel.cobbleutils.Model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Carlos Varas Alonso - 19/11/2024 2:16
 */
@Getter
@Setter
public class WebHookData {
  private boolean ENABLED;
  private String URL_WEBHOOK;
  private String AVATAR_URL;
  private String USERNAME;
  private String COLOR;

  public WebHookData(String URL_WEBHOOK, String AVATAR_URL, String USERNAME) {
    this.ENABLED = false;
    this.URL_WEBHOOK = URL_WEBHOOK;
    this.AVATAR_URL = AVATAR_URL;
    this.USERNAME = USERNAME;
    this.COLOR = Integer.toHexString(0x00FF00);
  }
}
