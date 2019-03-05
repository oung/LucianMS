/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lucianms.events;

import com.lucianms.client.MapleCharacter;
import com.lucianms.client.MapleClient;
import com.lucianms.client.inventory.Item;
import com.lucianms.cquest.CQuestData;
import com.lucianms.cquest.requirement.CQuestItemRequirement;
import com.lucianms.io.scripting.item.ItemScriptManager;
import com.lucianms.nio.receive.MaplePacketReader;
import com.lucianms.server.MapleInventoryManipulator;
import com.lucianms.server.MapleItemInformationProvider;
import com.lucianms.server.MapleItemInformationProvider.scriptedItem;
import com.lucianms.server.maps.MapleMapItem;
import com.lucianms.server.maps.MapleMapObject;
import com.lucianms.server.world.MaplePartyCharacter;
import tools.MaplePacketCreator;

import java.util.Collection;

/**
 * @author Matze
 */
public final class
ItemPickupEvent extends PacketEvent {

    private int objectId;

    @Override
    public void processInput(MaplePacketReader reader) {
        reader.skip(4);
        reader.skip(1);
        reader.skip(4);
        objectId = reader.readInt();
    }

    @Override
    public Object onPacket() {
        MapleCharacter chr = getClient().getPlayer();
        MapleMapObject ob = chr.getMap().getMapObject(objectId);
        if (ob == null) {
            return null;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        if (ob instanceof MapleMapItem) {
            MapleMapItem mapitem = (MapleMapItem) ob;
            if (System.currentTimeMillis() - mapitem.getDropTime() < 900) {
                getClient().announce(MaplePacketCreator.enableActions());
                return null;
            }
            // un-obtainable item
            if (mapitem.getItem() != null && !mapitem.getItem().isObtainable()) {
                Item item = mapitem.getItem();
                if (item.getItemId() == 3990022) {
                    chr.dropMessage("Foothold ID: " + item.getOwner().split(":")[1]);
                }
                chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                chr.getMap().removeMapObject(ob);
                getClient().announce(MaplePacketCreator.enableActions());
                return null;
            }
            if (mapitem.getItemId() == 4031865 || mapitem.getItemId() == 4031866 || mapitem.getMeso() > 0 || ii.isConsumeOnPickup(mapitem.getItemId()) || MapleInventoryManipulator.checkSpace(getClient(), mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
                if ((chr.getMapId() > 209000000 && chr.getMapId() < 209000016) || (chr.getMapId() >= 990000500 && chr.getMapId() <= 990000502)) {//happyville trees and guild PQ
                    if (!mapitem.isPlayerDrop() || mapitem.getDropper().getObjectId() == chr.getObjectId()) {
                        if (mapitem.getMeso() > 0) {
                            chr.gainMeso(mapitem.getMeso(), true, true, false);
                            chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                            chr.getMap().removeMapObject(ob);
                            mapitem.setPickedUp(true);
                        } else if (MapleInventoryManipulator.addFromDrop(getClient(), mapitem.getItem(), false)) {
                            chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                            chr.getMap().removeMapObject(ob);
                            mapitem.setPickedUp(true);
                            if (chr.getArcade() != null) {
                                if (chr.getArcade().getId() == 4) {
                                    if (!chr.getArcade().fail()) {
                                        chr.getArcade().add();
                                    }
                                }
                            }
                        } else {
                            getClient().announce(MaplePacketCreator.enableActions());
                            return null;
                        }
                    } else {
                        getClient().announce(MaplePacketCreator.getInventoryFull());
                        getClient().announce(MaplePacketCreator.getShowInventoryFull());
                        return null;
                    }
                    getClient().announce(MaplePacketCreator.enableActions());
                    return null;
                }

                if (mapitem.getQuest() > 0 && !chr.needQuestItem(mapitem.getQuest(), mapitem.getItemId())) {
                    getClient().announce(MaplePacketCreator.showItemUnavailable());
                    getClient().announce(MaplePacketCreator.enableActions());
                    return null;
                }
                if (mapitem.isPickedUp()) {
                    getClient().announce(MaplePacketCreator.getInventoryFull());
                    getClient().announce(MaplePacketCreator.getShowInventoryFull());
                    return null;
                }
                if (mapitem.getMeso() > 0) {
                    if (chr.getParty() != null) {
                        int mesosamm = mapitem.getMeso();
                        if (mesosamm > 50000 * chr.getMesoRate()) {
                            return null;
                        }
                        int partynum = 0;
                        for (MaplePartyCharacter partymem : chr.getParty().getMembers()) {
                            if (partymem.isOnline() && partymem.getMapId() == chr.getMap().getId() && partymem.getChannel() == getClient().getChannel()) {
                                partynum++;
                            }
                        }
                        for (MaplePartyCharacter partymem : chr.getParty().getMembers()) {
                            if (partymem.isOnline() && partymem.getMapId() == chr.getMap().getId()) {
                                MapleCharacter somecharacter = getClient().getChannelServer().getPlayerStorage().get(partymem.getId());
                                if (somecharacter != null) {
                                    somecharacter.gainMeso(mesosamm / partynum, true, true, false);
                                }
                            }
                        }
                    } else {
                        chr.gainMeso(mapitem.getMeso(), true, true, false);
                    }
                } else if (mapitem.getItemId() / 10000 == 243) {
                    scriptedItem info = ii.getScriptedItemInfo(mapitem.getItemId());
                    if (info.runOnPickup()) {
                        ItemScriptManager ism = ItemScriptManager.getInstance();
                        String scriptName = info.getScript();
                        if (ism.scriptExists(scriptName)) {
                            ism.getItemScript(getClient(), scriptName);
                        }

                    } else {
                        if (!MapleInventoryManipulator.addFromDrop(getClient(), mapitem.getItem(), true)) {
                            getClient().announce(MaplePacketCreator.enableActions());
                            return null;
                        } else {
                            if (chr.getArcade() != null) {
                                if (!chr.getArcade().fail()) {
                                    chr.getArcade().add();
                                }
                            }
                        }
                    }
                } else if (mapitem.getItemId() == 4031865 || mapitem.getItemId() == 4031866) {
                    // Add NX to account, show effect and make item disapear
                    chr.getCashShop().gainCash(1, mapitem.getItemId() == 4031865 ? 100 : 250);
                } else if (useItem(getClient(), mapitem.getItemId())) {
                    if (mapitem.getItemId() / 10000 == 238) {
                        chr.getMonsterBook().addCard(getClient(), mapitem.getItemId());
                    }
                } else if (MapleInventoryManipulator.addFromDrop(getClient(), mapitem.getItem(), true)) {
                    if (chr.getArcade() != null) {
                        if (!chr.getArcade().fail()) {
                            chr.getArcade().add();
                        }
                    }
                    for (CQuestData data : chr.getCustomQuests().values()) {
                        if (!data.isCompleted()) {
                            CQuestItemRequirement toLoot = data.getToCollect();
                            toLoot.incrementRequirement(mapitem.getItemId(), mapitem.getItem().getQuantity());
                            boolean checked = toLoot.isFinished(); // local bool before updating requirement checks; if false, quest is not finished
                            if (data.checkRequirements() && !checked) { // update requirement checks - it is important that checkRequirements is executed first
                                    /*
                                    If checkRequirements returns true, the quest is finished. If checked is also false, then
                                    this is check means the quest is finished. The quest completion notification should only
                                    happen once unless a progress variable drops below the requirement
                                     */
                                data.announceCompletion(getClient());
                            }
                            if (!data.isSilentComplete()) {
                                CQuestItemRequirement.CQuestItem p = toLoot.get(mapitem.getItemId());
                                if (p != null) {
                                    String name = ii.getName(mapitem.getItemId());
                                    name = (name == null) ? "NO-NAME" : name; // hmmm
                                    chr.announce(MaplePacketCreator.earnTitleMessage(String.format("[%s] Item Collection '%s' [%d / %d]", data.getName(), name, p.getProgress(), p.getRequirement())));
                                }
                            }
                        }
                    }
                } else if (mapitem.getItemId() == 4031868) {
                    chr.getMap().broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chr.getName(), chr.getItemQuantity(4031868, false), false));
                } else {
                    getClient().announce(MaplePacketCreator.enableActions());
                    return null;
                }
                mapitem.setPickedUp(true);
                chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                chr.getMap().removeMapObject(ob);
            }
        }
        getClient().announce(MaplePacketCreator.enableActions());
        return null;
    }

    public int getObjectId() {
        return objectId;
    }

    public static boolean useItem(final MapleClient c, final int id) {
        if (id / 1000000 == 2) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (ii.isConsumeOnPickup(id)) {
                MapleCharacter player = c.getPlayer();
                if (id > 2022430 && id < 2022434) {
                    Collection<MapleCharacter> characters = player.getMap().getCharacters();
                    try {
                        for (MapleCharacter mc : characters) {
                            if (mc.getParty() == player.getParty()) {
                                ii.getItemEffect(id).applyTo(mc);
                            }
                        }
                    } finally {
                        characters.clear();
                    }
                } else {
                    ii.getItemEffect(id).applyTo(player);
                }
                return true;
            }
        }
        return false;
    }
}
