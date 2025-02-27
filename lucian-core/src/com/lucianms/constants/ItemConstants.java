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
package com.lucianms.constants;

import com.lucianms.client.inventory.MapleInventoryType;

/**
 * @author Jay Estrella
 */
public final class ItemConstants {

    public static final int LOCK = 0x01;
    public static final int SPIKES = 0x02;
    public static final int COLD = 0x04;
    public static final int UNTRADEABLE = 0x08;
    public static final int KARMA = 0x10;
    public static final int PET_COME = 0x80;
    public static final int ACCOUNT_SHARING = 0x100;
    public static final int MAGICAL_MITTEN = 1472063;
    public static final float ITEM_ARMOR_EXP = 1 / 350000f;
    public static final float ITEM_WEAPON_EXP = 1 / 700000f;

    public static final boolean EXPIRING_ITEMS = true;

    public final static int ExpTicket = 2002031;
    public static final int SpiritPendant = 1122017;

    public static int getFlagByInt(int type) {
        if (type == 128) {
            return PET_COME;
        } else if (type == 256) {
            return ACCOUNT_SHARING;
        }
        return 0;
    }

    public static boolean isWeddingRing(int itemID) {
        return itemID == 1112803 || itemID == 1112806 || itemID == 1112807 || itemID == 1112809;
    }

    public static boolean isFriendshipEquip(int itemID) {
        return itemID / 100 == 11128 && itemID % 10 <= 2;
    }

    public static boolean isCoupleEquip(int itemID) {
        return itemID / 100 == 11120 && itemID != 1112000;
    }

    public static boolean isEyeScanner(int itemID) {
        return itemID >= 4011009 && itemID <= 4011015;
    }

    public static boolean isThrowingStar(int itemId) {
        return itemId / 10000 == 207;
    }

    public static boolean isBullet(int itemId) {
        return itemId / 10000 == 233;
    }

    public static boolean isRechargable(int itemId) {
        return isThrowingStar(itemId) || isBullet(itemId);
    }

    public static boolean isArrowForCrossBow(int itemId) {
        return itemId / 1000 == 2061;
    }

    public static boolean isArrowForBow(int itemId) {
        return itemId / 1000 == 2060;
    }

    public static boolean isPet(int itemId) {
        return itemId / 10000 == 500;
    }

    public static boolean isPetEquip(int itemId) {
        return itemId >= 1802000 && itemId < 1810000;
    }

    public static MapleInventoryType getInventoryType(final int itemId) {
        final byte type = (byte) (itemId / 1000000);
        if (type < 1 || type > 5) {
            return MapleInventoryType.UNDEFINED;
        }
        return MapleInventoryType.getByType(type);
    }

    public static boolean isWeapon(int itemId) {
        return itemId >= 1302000 && itemId < 1492024;
    }

    public static boolean isOverall(int itemId) {
        return itemId / 10000 == 105;
    }
}
