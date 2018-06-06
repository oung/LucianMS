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
package net.server.handlers.login;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleSkinColor;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class CreateCharHandler extends AbstractMaplePacketHandler {

    private static int[] IDs = {
            1302000, 1312004, 1322005, 1442079,// weapons
            1040002, 1040006, 1040010, 1041002, 1041006, 1041010, 1041011, 1042167,// bottom
            1060002, 1060006, 1061002, 1061008, 1062115, // top
            1072001, 1072005, 1072037, 1072038, 1072383,// shoes
            30000, 30010, 30020, 30030, 31000, 31040, 31050,// hair
            20000, 20001, 20002, 21000, 21001, 21002, 21201, 20401, 20402, 21700, 20100  //face
            //#NeverTrustStevenCode
    };

    private static boolean isLegal(int toCompare) {
        for (int ID : IDs) {
            if (ID == toCompare) {
                return true;
            }
        }
        return false;
    }


    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String name = slea.readMapleAsciiString();
        if (!MapleCharacter.canCreateChar(name)) {
            return;
        }
        MapleCharacter newchar = MapleCharacter.getDefault(c);
        newchar.setWorld(c.getWorld());

        int job = slea.readInt();
        int face = slea.readInt();

        int hair = slea.readInt();
        int hairColor = slea.readInt();
        int skincolor = slea.readInt();

        newchar.setSkinColor(MapleSkinColor.getById(skincolor));
        int top = slea.readInt();
        int bottom = slea.readInt();
        int shoes = slea.readInt();
        int weapon = slea.readInt();
        newchar.setGender(slea.readByte());
        newchar.setName(name);
        newchar.setHair(hair + hairColor);
        newchar.setFace(face);

        int[] items = new int[]{weapon, top, bottom, shoes, hair, face};
        for (int item : items) {
            if (!isLegal(item)) {
                return;
            }
        }

        newchar.setMapId(90000000);
        if (job == 0) { // Knights of Cygnus
            newchar.setJob(MapleJob.NOBLESSE);
            newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161047, (short) 0, (short) 1));
        } else if (job == 1) { // Adventurer
            newchar.setJob(MapleJob.BEGINNER);
            newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (short) 0, (short) 1));
        } else if (job == 2) { // Aran
            newchar.setJob(MapleJob.LEGEND);
            newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161048, (short) 0, (short) 1));
        } else {
            c.announce(MaplePacketCreator.deleteCharResponse(0, 9));
            return;
        }

        MapleInventory equipped = newchar.getInventory(MapleInventoryType.EQUIPPED);

        Equip equip;

        equip = MapleItemInformationProvider.getInstance().getEquipById(top);
        equip.setPosition((byte) -5);
        equipped.addFromDB(equip);

        equip = MapleItemInformationProvider.getInstance().getEquipById(bottom);
        equip.setPosition((byte) -6);
        equipped.addFromDB(equip);

        equip = MapleItemInformationProvider.getInstance().getEquipById(shoes);
        equip.setPosition((byte) -7);
        equipped.addFromDB(equip);

        equip = MapleItemInformationProvider.getInstance().getEquipById(weapon);
        equip.setPosition((byte) -11);
        equip.setWatk((short) 30);
        equipped.addFromDB(equip);

        if (!newchar.insertNewChar()) {
            c.announce(MaplePacketCreator.deleteCharResponse(0, 9));
            return;
        }
        c.announce(MaplePacketCreator.addNewCharEntry(newchar));
    }
}