package com.kingpixel.cobbleutils.party.models;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Carlos Varas Alonso - 28/06/2024 3:31
 */
@Getter
@Setter
public class UserParty {

  private String partyName;
  private boolean HasParty;

  public UserParty(String partyName, boolean HasParty) {

    this.partyName = partyName;
    this.HasParty = HasParty;
  }


}
