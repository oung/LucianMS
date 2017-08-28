package server.events.custom.bpq;

import server.events.custom.BossPQ;

import java.awt.*;

/**
 * @author izarooni
 * @author lucasdieswagger
 */
public class BHellMode extends BossPQ {

    public static final int[] bosses = new int[]{8170000, 8220012, 8820001, 8820002, 8820003, 8820004, 8820005, 8820006, 8830000, 8830001, 8830002, 9001014, 9895253};

    private static final Point mSpawnPoint = new Point(-391, -386);

    public BHellMode(int channel) {
        super(channel, 803, bosses);
        setCashWinnings(20 * bosses.length);
        setDamageMultiplier(10);
        setHealthMultiplier(10);
        setPoints(75);
    }

    @Override
    public int getMinimumLevel() {
        return 150;
    }

    @Override
    public Point getMonsterSpawnPoint() {
        return mSpawnPoint;
    }
}
