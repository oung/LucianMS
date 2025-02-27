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
package com.lucianms.client;

import com.lucianms.scheduler.Task;
import com.lucianms.scheduler.TaskExecutor;
import tools.MaplePacketCreator;

/**
 * @author PurpleMadness Patrick :O
 */
public class MapleMount {

    private int itemid;
    private int skillid;
    private int tiredness;
    private int exp;
    private int level;
    private Task tirednessTask;
    private MapleCharacter owner;

    public MapleMount(MapleCharacter owner, int itemID, int skillID) {
        this.itemid = itemID;
        this.skillid = skillID;
        this.tiredness = 0;
        this.level = 1;
        this.exp = 0;
        this.owner = owner;
    }

    public int getItemId() {
        return itemid;
    }

    public int getSkillId() {
        return skillid;
    }

    /**
     * 1902000 - Hog
     * 1902001 - Silver Mane
     * 1902002 - Red Draco
     * 1902005 - Mimiana
     * 1902006 - Mimio
     * 1902007 - Shinjou
     * 1902008 - Frog
     * 1902009 - Ostrich
     * 1902010 - Frog
     * 1902011 - Turtle
     * 1902012 - Yeti
     *
     * @return the id
     */
    public int getId() {
        if (this.itemid < 1903000) {
            return itemid - 1901999;
        }
        return 5;
    }

    public int getTiredness() {
        return tiredness;
    }

    public int getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public void setTiredness(int newtiredness) {
        this.tiredness = newtiredness;
        if (tiredness < 0) {
            tiredness = 0;
        }
    }

    private void increaseTiredness() {
        if (owner != null) {
            this.tiredness++;
            owner.getMap().broadcastMessage(MaplePacketCreator.updateMount(owner.getId(), this, false));
            if (tiredness > 99) {
                this.tiredness = 95;
                owner.dispelSkill(owner.getJobType() * 10000000 + 1004);
            }
        } else {
            cancelSchedule();
        }
    }

    public void setExp(int newexp) {
        this.exp = newexp;
    }

    public void setLevel(int newlevel) {
        this.level = newlevel;
    }

    public void setItemId(int newitemid) {
        this.itemid = newitemid;
    }

    public void startSchedule() {
        this.tirednessTask = TaskExecutor.createRepeatingTask(this::increaseTiredness, 60000, 60000);
    }

    public void cancelSchedule() {
        tirednessTask = TaskExecutor.cancelTask(tirednessTask);
    }

    public void empty() {
        cancelSchedule();
        this.owner = null;
    }
}
