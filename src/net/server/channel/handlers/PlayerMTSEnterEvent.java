package net.server.channel.handlers;

import client.MapleCharacter;
import com.lucianms.io.scripting.npc.NPCScriptManager;
import com.lucianms.nio.receive.MaplePacketReader;
import com.lucianms.server.events.PacketEvent;
import constants.ServerConstants;
import tools.MaplePacketCreator;

/**
 * @author izarooni
 */
public class PlayerMTSEnterEvent extends PacketEvent {

    @Override
    public void processInput(MaplePacketReader reader) {
    }

    @Override
    public Object onPacket() {
        MapleCharacter player = getClient().getPlayer();
        if (!ServerConstants.USE_MTS) {
            NPCScriptManager.start(getClient(), 2007, "f_multipurpose");
        }
        player.announce(MaplePacketCreator.enableActions());
        return null;
    }
}