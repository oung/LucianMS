package server.events.custom.bosspq;

import java.awt.Point;

import client.MapleCharacter;
import server.events.custom.AbstractBossPQ;

public class EasyBossPQ extends AbstractBossPQ {

	int[] easyBosses = {100100, 100100, 100100, 100100, 100100, 100100};
	
	public EasyBossPQ(MapleCharacter partyleader) {
		super(partyleader);
		this.bosses = easyBosses;
		this.nxWinnings = 5 * easyBosses.length;
		this.nxWinningsMultiplier = 1;
		this.minLevel = 60;
		this.points = 15;
		this.map = 100000000; // the map of the boss pq
		this.monsterSpawnLoc = new Point(-246, 274);
		this.spawnLoc = new Point(-216, 274);
		this.damageMultipier = 1;
		this.healthMultiplier = 1;
	}
	
}
