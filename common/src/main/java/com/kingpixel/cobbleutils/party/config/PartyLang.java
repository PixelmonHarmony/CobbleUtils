package com.kingpixel.cobbleutils.party.config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public class PartyLang {
  private String prefix;
  private String titlemenu;
  private String titlemembers;
  private String titleinvites;
  private String partyCreated;
  private String partyAlreadyExists;
  private String partyNotExists;
  private String partyAlreadyInParty;
  private String partyDeleted;
  private String partyLeave;
  private String partyJoin;
  private String partyKick;
  private String partyKickOther;
  private String partyKickYourSelf;
  private String partyPromote;
  private String partyDemote;
  private String partyList;
  private String partyInfo;
  private String partyInvite;
  private String partyInviteSend;
  private String partyFull;
  private String partynotInParty;
  private String partyNotOwner;
  private String partyInviteYourSelf;
  private String partyPlayerHasInviteOtherParty;
  private String partyPlayerHasInvite;
  private String partyNotInvites;
  private String partyChat;
  private String partyChatNotValidMessage;
  private List<String> partyLoreMember;


  /**
   * Constructor to generate a file if one doesn't exist.
   */
  public PartyLang() {
    prefix = "§7[§6Party§7] ";
    titlemenu = "§6Party Menu";
    titlemembers = "§6Members";
    titleinvites = "§6Invites";
    partyCreated = "%partyprefix% Party created!";
    partyAlreadyExists = "%partyprefix% Party already exists!";
    partyNotExists = "%partyprefix% Party does not exist!";
    partyAlreadyInParty = "%partyprefix% You are already in a party!";
    partyDeleted = "%partyprefix% Party deleted!";
    partyLeave = "%partyprefix% You left the party!";
    partyJoin = "%partyprefix% You joined the party!";
    partyKick = "%partyprefix% You kicked %player% from the party!";
    partyKickOther = "%partyprefix% You were kicked from the party!";
    partyKickYourSelf = "%partyprefix% You can't kick yourself!";
    partyPromote = "%partyprefix% You promoted %player% to leader!";
    partyDemote = "%partyprefix% You demoted %player% from leader!";
    partyList = "%partyprefix% Parties: %partys%";
    partyInfo = "%partyprefix% Party: %partyname%\nLeader: %leader%\nMembers: %members%";
    partyInvite = "%partyprefix% You invited %player% to the party!";
    partyInviteSend = "%partyprefix% You have been invited to %partyname% party!";
    partyFull = "%partyprefix% Party is full!";
    partynotInParty = "%partyprefix% You are not in a party!";
    partyNotOwner = "%partyprefix% You are not the owner of the party!";
    partyInviteYourSelf = "%partyprefix% You can't invite yourself!";
    partyPlayerHasInviteOtherParty = "%partyprefix% %player% has an invite to another party!";
    partyPlayerHasInvite = "%partyprefix% %player% has an invite to the party!";
    partyChat = "%partyprefix% %partyname% %player%: %message%";
    partyNotInvites = "%partyprefix% You don't have any invites!";
    partyChatNotValidMessage = "%partyprefix% Your message contains invalid characters!";
    partyLoreMember = List.of("§6Leader: %leader%", "§6Members: %members%");
  }

  /**
   * Method to initialize the config.
   */
  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleUtils.PATH_PARTY_LANG, CobbleUtils.config.getLang() + ".json",
      el -> {
        Gson gson = Utils.newGson();
        PartyLang lang = gson.fromJson(el, PartyLang.class);
        this.prefix = lang.getPrefix();
        this.titlemenu = lang.getTitlemenu();
        this.titlemembers = lang.getTitlemembers();
        this.titleinvites = lang.getTitleinvites();
        this.partyCreated = lang.getPartyCreated();
        this.partyAlreadyExists = lang.getPartyAlreadyExists();
        this.partyAlreadyInParty = lang.getPartyAlreadyInParty();
        this.partyDeleted = lang.getPartyDeleted();
        this.partyNotExists = lang.getPartyNotExists();
        this.partyLeave = lang.getPartyLeave();
        this.partyJoin = lang.getPartyJoin();
        this.partyKick = lang.getPartyKick();
        this.partyPromote = lang.getPartyPromote();
        this.partyDemote = lang.getPartyDemote();
        this.partyList = lang.getPartyList();
        this.partyInfo = lang.getPartyInfo();
        this.partyInvite = lang.getPartyInvite();
        this.partyInviteSend = lang.getPartyInviteSend();
        this.partyFull = lang.getPartyFull();
        this.partynotInParty = lang.getPartynotInParty();
        this.partyNotOwner = lang.getPartyNotOwner();
        this.partyInviteYourSelf = lang.getPartyInviteYourSelf();
        this.partyPlayerHasInviteOtherParty = lang.getPartyPlayerHasInviteOtherParty();
        this.partyPlayerHasInvite = lang.getPartyPlayerHasInvite();
        this.partyKickOther = lang.getPartyKickOther();
        this.partyKickYourSelf = lang.getPartyKickYourSelf();
        this.partyChat = lang.getPartyChat();
        this.partyNotInvites = lang.getPartyNotInvites();
        this.partyChatNotValidMessage = lang.getPartyChatNotValidMessage();
        this.partyLoreMember = lang.getPartyLoreMember();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_PARTY_LANG, CobbleUtils.config.getLang() +
            ".json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.fatal("Could not write lang.json file for " + CobbleUtils.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info("No lang.json file found for" + CobbleUtils.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleUtils.PATH_PARTY_LANG, CobbleUtils.config.getLang() +
          ".json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.fatal("Could not write lang.json file for " + CobbleUtils.MOD_NAME + ".");
      }
    }
  }

}
