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
package net.server.channel.handlers;

import net.PacketEvent;
import client.MapleClient;
import tools.data.input.LittleEndianReader;

/**
 *
 * @author Quasar
 */
public class PartySearchRegisterHandler extends PacketEvent {
    public void handlePacket(LittleEndianReader slea, MapleClient c) {
    	return; //Disabling this for now.
/*    	MapleCharacter chr = c.getPlayer();
    	int min = slea.readInt();
    	int max = slea.readInt();
    	if (chr.getLevel() < min || chr.getLevel() > max || (max - min) > 30 || min > max) { // Client editing
    		return;
    	}*/
    }
}