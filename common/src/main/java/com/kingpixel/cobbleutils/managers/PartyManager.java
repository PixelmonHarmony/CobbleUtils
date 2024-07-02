package com.kingpixel.cobbleutils.managers;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.PlayerInfo;
import com.kingpixel.cobbleutils.party.event.CreatePartyEvent;
import com.kingpixel.cobbleutils.party.event.DeletePartyEvent;
import com.kingpixel.cobbleutils.party.models.PartyData;
import com.kingpixel.cobbleutils.party.models.UserParty;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.util.*;

/**
 * @author Carlos Varas Alonso - 28/06/2024 2:28
 */
@Getter
@SuppressWarnings("NullPointerException")
public class PartyManager {
  // Map<PartyName, PartyData>
  private Map<String, PartyData> parties;
  // Map<UserUUID, PartyName>
  private Map<UUID, UserParty> userParty;

  public PartyManager() {
    this.parties = new HashMap<>();
    this.userParty = new HashMap<>();
  }

  public void createParty(String partyName, PlayerInfo owner) {
    UserParty userParty = this.userParty.get(owner.getUuid());
    PartyData partyData = new PartyData(partyName, owner);
    if (userParty.isHasParty()) {
      // Esta en una party el jugador
      CobbleUtils.server.getPlayerList().getPlayer(owner.getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyAlreadyInParty()));
      return;
    }
    if (parties.get(partyName) == null) {
      // No existe el party
      parties.put(partyName, partyData);
      userParty.setHasParty(true);
      userParty.setPartyName(partyName);
      partyData.init();
      CreatePartyEvent.CREATE_PARTY_EVENT.emit(partyData);
      CobbleUtils.server.getPlayerList().getPlayer(owner.getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyCreated()));
    } else {
      // Existe el party
      CobbleUtils.server.getPlayerList().getPlayer(owner.getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyAlreadyExists()));
    }
  }

  public void deleteParty(String partyName) {
    if (parties.get(partyName) == null) {
      CobbleUtils.server.getPlayerList().getPlayer(parties.get(partyName).getOwner().getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyNotExists()));
    } else {
      PartyData partyData = parties.get(partyName);
      for (PlayerInfo playerInfo : partyData.getMembers()) {
        userParty.put(playerInfo.getUuid(), null);
      }
      parties.remove(partyName);
      deletefile(partyName);
      CobbleUtils.server.getPlayerList().getPlayer(parties.get(partyName).getOwner().getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyDeleted()));
    }
  }

  public void addMember(String partyName, PlayerInfo playerInfo) {
    PartyData partyData = parties.get(partyName);
    if (partyData == null) {
      CobbleUtils.server.getPlayerList().getPlayer(playerInfo.getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyNotExists()));
    } else {
      if (partyData.getMembers().size() == CobbleUtils.partyConfig.getMaxPartySize()) {
        CobbleUtils.server.getPlayerList().getPlayer(playerInfo.getUuid())
          .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyFull()));
        return;
      }
      partyData.getMembers().add(playerInfo);
      userParty.put(playerInfo.getUuid(), new UserParty(partyName, true));
      partyData.writeInfo();
      CobbleUtils.server.getPlayerList().getPlayer(playerInfo.getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyJoin()));
    }
  }

  public void leaveParty(String partyname, PlayerInfo playerInfo) {
    PartyData partyData = parties.get(partyname);
    Player player = CobbleUtils.server.getPlayerList().getPlayer(playerInfo.getUuid());
    boolean isowner = partyData.getOwner().equals(playerInfo);
    if (isowner) {
      // Soy el owner
      if (partyData.getMembers().size() == 1) {
        // Soy el unico miembro
        parties.remove(partyname);
        userParty.get(playerInfo.getUuid()).setPartyName("");
        userParty.get(playerInfo.getUuid()).setHasParty(false);
        deletefile(partyname);
        DeletePartyEvent.DELETE_PARTY_EVENT.emit(partyData);
        player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyDeleted()));
      } else {
        // No soy el unico miembro
        partyData.getMembers().remove(playerInfo);
        userParty.get(playerInfo.getUuid()).setPartyName("");
        userParty.get(playerInfo.getUuid()).setHasParty(false);
        partyData.setOwner(partyData.getMembers().iterator().next());
        partyData.writeInfo();
        player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyLeave()));
      }
    } else {
      // No soy el owner
      partyData.getMembers().remove(playerInfo);
      userParty.get(playerInfo.getUuid()).setPartyName("");
      userParty.get(playerInfo.getUuid()).setHasParty(false);
      partyData.writeInfo();
      player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyLeave()));
    }
  }

  private void deletefile(String partyName) {
    File file = Utils.getAbsolutePath(CobbleUtils.PATH_PARTY_DATA + partyName + ".json");
    if (CobbleUtils.config.isDebug())
      CobbleUtils.server.sendSystemMessage(AdventureTranslator.toNative("Deleting file: " + file.getAbsolutePath() +
        " " + file.exists()));
    if (file.exists()) {
      file.delete();
    }
  }

  public void invitePlayer(UUID owneruuid, UUID inviteuuid, UserParty owner, UserParty inviteData) {
    boolean isOwner = parties.get(owner.getPartyName()).getOwner().getUuid().equals(owneruuid);
    Player playerowner = CobbleUtils.server.getPlayerList().getPlayer(owneruuid);
    Player playerinvite = CobbleUtils.server.getPlayerList().getPlayer(inviteuuid);

    if (owneruuid.equals(inviteuuid)) {
      playerowner.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyInviteYourSelf()));
      return;
    }

    if (!isOwner) {
      playerowner.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyNotOwner()));
      return;
    }

    if (inviteData.isHasParty()) {
      playerowner.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyAlreadyInParty()));
      return;
    }

    if (parties.get(owner.getPartyName()).getMembers().size() == CobbleUtils.partyConfig.getMaxPartySize()) {
      playerowner.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyFull()));
      return;
    }

    for (Map.Entry<String, PartyData> entry : parties.entrySet()) {
      String partyName = entry.getKey();
      PartyData partyData = entry.getValue();

      if (partyData.getInvites().contains(inviteuuid)) {
        if (owner.getPartyName().equals(partyName)) {
          playerowner.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyPlayerHasInvite()
            .replace("%player%", playerinvite.getGameProfile().getName())));
        } else {
          playerowner
            .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyPlayerHasInviteOtherParty()
              .replace("%player%", playerinvite.getGameProfile().getName())));
        }
        return;
      }
    }
    parties.get(owner.getPartyName()).getInvites().add(inviteuuid);
    playerowner.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyInvite()
      .replace("%player%", CobbleUtils.server.getPlayerList().getPlayer(inviteuuid).getGameProfile().getName())));
    playerinvite.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyInviteSend()
      .replace("%partyname%", owner.getPartyName())));
  }

  public void joinParty(PlayerInfo playerInfo) {
    boolean hasParty = userParty.get(playerInfo.getUuid()).isHasParty();
    if (hasParty) {
      CobbleUtils.server.getPlayerList().getPlayer(playerInfo.getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyAlreadyInParty()));
      return;
    }
    boolean hasInvites = parties.values().stream()
      .anyMatch(partyData -> partyData.getInvites().contains(playerInfo.getUuid()));
    if (!hasInvites) {
      CobbleUtils.server.getPlayerList().getPlayer(playerInfo.getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyNotInvites()));
      return;
    }
    parties.forEach((partyName, partyData) -> {
      if (partyData.getInvites().contains(playerInfo.getUuid())) {
        partyData.getMembers().add(playerInfo);
        userParty.get(playerInfo.getUuid()).setPartyName(partyName);
        userParty.get(playerInfo.getUuid()).setHasParty(true);
        partyData.getInvites().remove(playerInfo.getUuid());
        partyData.writeInfo();
        CobbleUtils.server.getPlayerList().getPlayer(playerInfo.getUuid())
          .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyJoin()));
      }
    });
  }

  public void joinParty(String partyname, PlayerInfo player) {
    PartyData partyData = parties.get(partyname);
    if (userParty.get(player.getUuid()).isHasParty()) {
      CobbleUtils.server.getPlayerList().getPlayer(player.getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyAlreadyInParty()));
      return;
    }
    if (partyData == null) {
      CobbleUtils.server.getPlayerList().getPlayer(player.getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyNotExists()));
    } else {
      if (partyData.getMembers().size() == CobbleUtils.partyConfig.getMaxPartySize()) {
        userParty.get(player.getUuid()).setPartyName("");
        userParty.get(player.getUuid()).setHasParty(false);
        CobbleUtils.server.getPlayerList().getPlayer(player.getUuid())
          .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyFull()));
        return;
      }
      partyData.getMembers().add(player);
      userParty.put(player.getUuid(), new UserParty(partyname, true));
      partyData.writeInfo();
      CobbleUtils.server.getPlayerList().getPlayer(player.getUuid())
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyJoin()));
    }
  }

  public void kickPlayer(String partyname, PlayerInfo owner, PlayerInfo playerInfo) {
    PartyData partyData = parties.get(partyname);
    Player playerowner = CobbleUtils.server.getPlayerList().getPlayer(owner.getUuid());
    Player playermember = CobbleUtils.server.getPlayerList().getPlayer(playerInfo.getUuid());
    if (partyData == null) {
      playerowner
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyNotExists()));
      return;
    }

    if (!partyData.getOwner().equals(owner)) {
      playerowner
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyNotOwner()));
      return;
    }

    if (partyData.getOwner().equals(playerInfo)) {
      playerowner
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyKickYourSelf()));
      return;
    }

    if (!partyData.getMembers().contains(playerInfo)) {
      playerowner
        .sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartynotInParty()));
      return;
    }

    if (partyData.getMembers().remove(playerInfo)) {
      userParty.put(playerInfo.getUuid(), new UserParty("", false));
      partyData.writeInfo();
      playerowner.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyKick()
        .replace("%player%", playermember.getGameProfile().getName())));
      playermember.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyKickOther()));
    }
  }

  public boolean isOwner(UUID playeruuid) {
    return parties.values().stream().anyMatch(partyData -> partyData.getOwner().getUuid().equals(playeruuid));
  }

  public boolean isOwner(Player player) {
    return isOwner(player.getUUID());
  }

  public boolean isOwner(String playername) {
    return parties.values().stream().anyMatch(partyData -> partyData.getOwner().getName().equals(playername));
  }

  public List<UUID> getMembers(String partyname) {
    List<UUID> members = parties.get(partyname).getMembers().stream().map(PlayerInfo::getUuid).toList();
    if (members.isEmpty())
      return new ArrayList<>();
    return members;
  }

  public List<String> getMembersName(String partyname) {
    List<String> members = parties.get(partyname).getMembers().stream().map(PlayerInfo::getName).toList();
    if (members.isEmpty())
      return new ArrayList<>();
    return members;
  }

  public UserParty getUserParty(UUID playeruuid) {
    return userParty.get(playeruuid);
  }

  public UserParty getUserParty(String playername) {
    return userParty.get(Utils.getUUID(playername));
  }

  public PartyData getPartyData(String partyname) {
    return parties.get(partyname);
  }

  public boolean isPlayerInParty(UUID playeruuid) {
    UserParty userinfo = userParty.get(playeruuid);
    if (userinfo == null) return false;
    return userinfo.isHasParty();
  }

  public boolean isPlayerInParty(String playername) {
    return userParty.get(Utils.getUUID(playername)).isHasParty();
  }

  public boolean isPlayerInParty(Player player) {
    return isPlayerInParty(player.getUUID());
  }

}
