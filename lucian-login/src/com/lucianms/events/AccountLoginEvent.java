package com.lucianms.events;

import com.lucianms.Whitelist;
import com.lucianms.client.LoginState;
import com.lucianms.client.MapleClient;
import com.lucianms.nio.receive.MaplePacketReader;
import com.lucianms.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.MaplePacketCreator;

import java.util.Calendar;

/**
 * @author izarooni
 */
public class AccountLoginEvent extends PacketEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountLoginEvent.class);

    private String username;
    private String password;

    @Override
    public boolean inValidState() {
        return getClient().getLoginState() == LoginState.LogOut;
    }

    @Override
    public void processInput(MaplePacketReader reader) {
        username = reader.readMapleAsciiString();
        password = reader.readMapleAsciiString();
    }

    @Override
    public Object onPacket() {
        MapleClient client = getClient();
        client.setAccountName(username);
        int loginResult = client.getLoginResponse(username, password);
        if (Server.getConfig().getBoolean("WhitelistEnabled") && !Whitelist.hasAccount(client.getAccID())) {
            LOGGER.warn("Attempted non-whitelist account username: '{}' , accountID: '{}'", username, client.getAccID());
            client.announce(MaplePacketCreator.getLoginFailed(7));
            client.announce(MaplePacketCreator.serverNotice(1, "The server is in whitelist mode! Only certain users will have access to the game right now."));
            return null;
        }

        //region ban checks
        if (client.hasBannedIP() || client.hasBannedMac()) {
            client.announce(MaplePacketCreator.getLoginFailed(3));
            return null;
        }
        Calendar tempban = client.getTempBanCalendar();
        if (tempban != null) {
            if (tempban.getTimeInMillis() > System.currentTimeMillis()) {
                client.announce(MaplePacketCreator.getTempBan(tempban.getTimeInMillis(), client.getGReason()));
                return null;
            }
        }
        //endregion

        if (loginResult == 3) {
            client.announce(MaplePacketCreator.getPermBan(client.getGReason()));
        } else if (loginResult != 0) {
            client.announce(MaplePacketCreator.getLoginFailed(loginResult));
        } else {
            getClient().setLoginState(LoginState.Login);
            client.announce(MaplePacketCreator.getAuthSuccess(client));
        }
        return null;
    }
}
