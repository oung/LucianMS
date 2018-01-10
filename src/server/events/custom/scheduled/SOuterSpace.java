package server.events.custom.scheduled;

import client.MapleCharacter;
import net.server.channel.Channel;
import net.server.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scheduler.TaskExecutor;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MonsterListener;
import server.maps.MapleMap;
import server.maps.SavedLocationType;
import tools.MaplePacketCreator;
import tools.Randomizer;

import java.awt.*;
import java.util.Arrays;

/**
 * Should only ever been one instance per world
 *
 * @author izarooni
 */
public class SOuterSpace extends SAutoEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOuterSpace.class);

    private static final int MapId = 98;
    private static final int MonsterId = 9895259;
    private static final int[][] PortalPositions = {{156, 880}, {2352, 881}, {3124, 581}, {3831, 821}};

    private final World world;
    private final boolean[] finished;
    private boolean open = false;

    public SOuterSpace(World world) {
        this.world = world;
        this.finished = new boolean[world.getChannels().size()];
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isFinished() {
        for (boolean b : finished) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    public boolean isFinished(int i) {
        return finished[i];
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public long getInterval() {
        // 2 hours
        return 1000 * 60 * 60 * 2;
    }

    @Override
    public void run() {
        setOpen(true);
        for (Channel channel : world.getChannels()) {
            MapleMap eventMap = channel.getMapFactory().getMap(MapId);
            eventMap.killAllMonsters();
            eventMap.clearDrops();

            final int PPIndex = Randomizer.nextInt(PortalPositions.length);
            int[] ipos = PortalPositions[PPIndex];
            Point pos = new Point(ipos[0], ipos[1]);
            MapleMonster monster = MapleLifeFactory.getMonster(MonsterId);

            if (monster != null) {
                monster.addListener(new MonsterListener() {
                    @Override
                    public void monsterKilled(int aniTime) {
                        finished[channel.getId() - 1] = true;
                        channel.broadcastPacket(MaplePacketCreator.serverNotice(0, "The Space Slime has been defeated!"));
                        eventMap.broadcastMessage(MaplePacketCreator.serverNotice(6, "You will be warped momentarily"));
                        TaskExecutor.createTask(() -> eventMap.getCharacters().forEach(SOuterSpace.this::unregisterPlayer), 8000);
                    }
                });
                finished[channel.getId() - 1] = false;
                eventMap.spawnMonsterOnGroudBelow(monster, pos);
                channel.broadcastPacket(MaplePacketCreator.serverNotice(0, "The Space Slime has spawned in the Outer Space, Planet Lucian"));
                LOGGER.info("Space slime spawned at {}", Arrays.toString(ipos));
            } else {
                LOGGER.warn("Scheduled event 'Outer Space' was unable to spawn the monster " + MonsterId);
            }
        }
    }

    @Override
    public void end() {
        setOpen(false);
    }

    @Override
    public void registerPlayer(MapleCharacter player) {
        player.saveLocation("OTHER");
        player.changeMap(98);
        player.announce(MaplePacketCreator.showEffect("event/space/boss"));
    }

    @Override
    public void unregisterPlayer(MapleCharacter player) {
        if (player.getMapId() == MapId) {
            int returnMap = player.getSavedLocation("OTHER");
            if (returnMap == -1) {
                returnMap = 100000000;
                player.dropMessage("Your return map was obstructed");
            }
            player.changeMap(returnMap);
            player.clearSavedLocation(SavedLocationType.OTHER);
        }
    }
}
