/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lucianms.client.inventory;

import tools.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Flav
 */
public enum ItemFactory {

    INVENTORY(1, false),
    STORAGE(2, true),
    CASH_EXPLORER(3, true),
    CASH_CYGNUS(4, false),
    CASH_ARAN(5, false),
    MERCHANT(6, false);
    private int value;
    private boolean account;

    ItemFactory(int value, boolean account) {
        this.value = value;
        this.account = account;
    }

    public int getValue() {
        return value;
    }

    public List<Pair<Item, MapleInventoryType>> loadItems(Connection con, int id, boolean login) throws SQLException {
        List<Pair<Item, MapleInventoryType>> items = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `type` = ? AND `");
        query.append(account ? "accountid" : "characterid").append("` = ?");

        if (login) {
            query.append(" AND `inventorytype` = ").append(MapleInventoryType.EQUIPPED.getType());
        }

        try (PreparedStatement ps = con.prepareStatement(query.toString())) {
            ps.setInt(1, value);
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                    if (mit == MapleInventoryType.EQUIP || mit == MapleInventoryType.EQUIPPED) {
                        Equip equip = new Equip(rs.getInt("itemid"), (short) rs.getInt("position"));
                        equip.setOwner(rs.getString("owner"));
                        equip.setAcc((short) rs.getInt("acc"));
                        equip.setAvoid((short) rs.getInt("avoid"));
                        equip.setDex((short) rs.getInt("dex"));
                        equip.setHands((short) rs.getInt("hands"));
                        equip.setHp((short) rs.getInt("hp"));
                        equip.setInt((short) rs.getInt("int"));
                        equip.setJump((short) rs.getInt("jump"));
                        equip.setVicious((short) rs.getInt("vicious"));
                        equip.setFlag((byte) rs.getInt("flag"));
                        equip.setLuk((short) rs.getInt("luk"));
                        equip.setMatk((short) rs.getInt("matk"));
                        equip.setMdef((short) rs.getInt("mdef"));
                        equip.setMp((short) rs.getInt("mp"));
                        equip.setSpeed((short) rs.getInt("speed"));
                        equip.setStr((short) rs.getInt("str"));
                        equip.setWatk((short) rs.getInt("watk"));
                        equip.setWdef((short) rs.getInt("wdef"));
                        equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                        equip.setLevel(rs.getByte("level"));
                        equip.setItemExp(rs.getInt("itemexp"));
                        equip.setItemLevel(rs.getByte("itemlevel"));
                        equip.setExpiration(rs.getLong("expiration"));
                        equip.setGiftFrom(rs.getString("giftFrom"));
                        equip.setRingId(rs.getInt("ringid"));
                        equip.setEliminations(rs.getInt("eliminations"));
                        equip.setRegalia(rs.getInt("regalia") == 1);
                        items.add(new Pair<>(equip, mit));
                    } else {
                        Item item = new Item(rs.getInt("itemid"), (byte) rs.getInt("position"), (short) rs.getInt("quantity"), rs.getInt("petid"));
                        item.setOwner(rs.getString("owner"));
                        item.setExpiration(rs.getLong("expiration"));
                        item.setGiftFrom(rs.getString("giftFrom"));
                        item.setFlag((byte) rs.getInt("flag"));
                        items.add(new Pair<>(item, mit));
                    }
                }
            }
        }
        return items;
    }

    public void saveItems(List<Pair<Item, MapleInventoryType>> items, int id, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("delete from inventoryitems where type = ? and " + (account ? "accountid" : "characterid") + " = ?")) {
            ps.setInt(1, value);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO `inventoryitems` VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            if (!items.isEmpty()) {
                ps.setInt(1, value);
                ps.setString(2, account ? null : String.valueOf(id));
                ps.setString(3, account ? String.valueOf(id) : null);
                for (Pair<Item, MapleInventoryType> pair : items) {
                    Item item = pair.getLeft();
                    MapleInventoryType mit = pair.getRight();

                    ps.setInt(4, item.getItemId());
                    ps.setInt(5, mit.getType());
                    ps.setInt(6, item.getPosition());
                    ps.setInt(7, item.getQuantity());
                    ps.setString(8, item.getOwner());
                    ps.setInt(9, item.getPetId());
                    ps.setInt(10, item.getFlag());
                    ps.setLong(11, item.getExpiration());
                    ps.setString(12, item.getGiftFrom());
                    ps.executeUpdate();
                    try (PreparedStatement pse = con.prepareStatement("INSERT INTO `inventoryequipment` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        if (mit == MapleInventoryType.EQUIP || mit == MapleInventoryType.EQUIPPED) {
                            try (ResultSet rs = ps.getGeneratedKeys()) {
                                if (!rs.next()) {
                                    throw new RuntimeException("Inserting item failed.");
                                }
                                pse.setInt(1, rs.getInt(1));
                            }
                            Equip equip = (Equip) item;
                            pse.setInt(2, equip.getUpgradeSlots());
                            pse.setInt(3, equip.getLevel());
                            pse.setInt(4, equip.getStr());
                            pse.setInt(5, equip.getDex());
                            pse.setInt(6, equip.getInt());
                            pse.setInt(7, equip.getLuk());
                            pse.setInt(8, equip.getHp());
                            pse.setInt(9, equip.getMp());
                            pse.setInt(10, equip.getWatk());
                            pse.setInt(11, equip.getMatk());
                            pse.setInt(12, equip.getWdef());
                            pse.setInt(13, equip.getMdef());
                            pse.setInt(14, equip.getAcc());
                            pse.setInt(15, equip.getAvoid());
                            pse.setInt(16, equip.getHands());
                            pse.setInt(17, equip.getSpeed());
                            pse.setInt(18, equip.getJump());
                            pse.setInt(19, 0);
                            pse.setInt(20, equip.getVicious());
                            pse.setInt(21, equip.getItemLevel());
                            pse.setInt(22, equip.getItemExp());
                            pse.setInt(23, equip.getRingId());
                            pse.setInt(24, equip.getEliminations());
                            pse.setInt(25, equip.isRegalia() ? 1 : 0);
                            pse.executeUpdate();
                        }
                    }
                }
            }
        }
    }
}