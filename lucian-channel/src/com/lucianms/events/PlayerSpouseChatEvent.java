package com.lucianms.events;

import com.lucianms.client.MapleCharacter;
import com.lucianms.client.Relationship;
import com.lucianms.command.CommandWorker;
import com.lucianms.nio.receive.MaplePacketReader;
import tools.MaplePacketCreator;

/**
 * @author izarooni
 */
public class PlayerSpouseChatEvent extends PacketEvent {

    private String username;
    private String content;

    @Override
    public void processInput(MaplePacketReader reader) {
        username = reader.readMapleAsciiString();
        content = reader.readMapleAsciiString();
    }

    @Override
    public Object onPacket() {
        MapleCharacter player = getClient().getPlayer();
        Relationship rltn = player.getRelationship();
        if (CommandWorker.isCommand(content)) {
            if (CommandWorker.process(getClient(), content, false)) {
                return null;
            }
        }
        if (rltn.getStatus() == Relationship.Status.Married) {
            MapleCharacter target = getClient().getWorldServer().findPlayer(p -> p.getName().equalsIgnoreCase(username));
            if (target != null && target.getId() == player.getRelationship().getPartnerID(player)) {
                getClient().announce(MaplePacketCreator.sendSpouseChat(target, content, false));
                target.announce(MaplePacketCreator.sendSpouseChat(player, content, true));
            } else {
                player.dropMessage(5, "Your significant other is offline.");
            }
        } else {
            player.dropMessage(5, "You are not married.");
        }
        return null;
    }
}
