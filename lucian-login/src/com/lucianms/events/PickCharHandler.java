package com.lucianms.events;

import com.lucianms.client.MapleClient;
import com.lucianms.nio.receive.MaplePacketReader;
import com.lucianms.server.channel.MapleChannel;
import tools.Randomizer;

import java.util.List;

/**
 * @author izarooni
 */
public class PickCharHandler extends UserTransferEvent {

    private int playerID;
    private int worldID;
    private String macs;

    @Override
    public void processInput(MaplePacketReader reader) {
        playerID = reader.readInt();
        worldID = reader.readInt();
        macs = reader.readMapleAsciiString();
        if (getClient().hasBannedMac() || !getClient().isPlayerBelonging(playerID)) {
            getClient().getSession().close();
            setCanceled(true);
        }

        checkLoginAvailability();
    }

    @Override
    public Object onPacket() {
        MapleClient client = getClient();
        MapleChannel cserv = client.getChannelServer();

        client.setWorld(worldID);
        client.updateMacs(macs);
        List<MapleChannel> channels = client.getWorldServer().getChannels();
        client.setChannel(Randomizer.nextInt(channels.size()) + 1);

        issueConnect(cserv.getNetworkAddress(), cserv.getPort(), playerID);
        return null;
    }
}
