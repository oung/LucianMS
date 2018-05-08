package com.lucianms.server.events.login;

import com.lucianms.server.Whitelist;
import net.PacketEvent;
import net.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.Calendar;

/**
 * @author izarooni
 */
public final class AccountLoginEvent extends PacketEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountLoginEvent.class);

    private String username;
    private String password;

    @Override
    public boolean inValidState() {
        return !getClient().isLoggedIn();
    }

    @Override
    public void process(SeekableLittleEndianAccessor slea) {
        username = slea.readMapleAsciiString();
        password = slea.readMapleAsciiString();
    }

    @Override
    public Object onPacket() {
        getClient().setAccountName(username);
        int loginResult = getClient().login(username, password);
        if (Server.getInstance().getConfig().getBoolean("WhitelistEnabled")) {
            if (!Whitelist.hasAccount(getClient().getAccID())) {
                LOGGER.warn("Attempted non-whitelist account login username: '{}' , accountID: '{}'", username, getClient().getAccID());
                getClient().announce(MaplePacketCreator.getLoginFailed(5));
                getClient().announce(MaplePacketCreator.serverNotice(1, "The server is unavailable for regular players at this time\r\nPlease contact an administrator if this is a mistake"));
                return null;
            }
        }

        //region ban checks
        if (getClient().hasBannedIP() || getClient().hasBannedMac()) {
            getClient().announce(MaplePacketCreator.getLoginFailed(3));
            return null;
        }
        Calendar tempban = getClient().getTempBanCalendar();
        if (tempban != null) {
            if (tempban.getTimeInMillis() > System.currentTimeMillis()) {
                getClient().announce(MaplePacketCreator.getTempBan(tempban.getTimeInMillis(), getClient().getGReason()));
                return null;
            }
        }
        //endregion

        if (loginResult == 3) {
            getClient().announce(MaplePacketCreator.getPermBan(getClient().getGReason()));
        } else if (loginResult != 0) {
            getClient().announce(MaplePacketCreator.getLoginFailed(loginResult));
        } else if (getClient().finishLogin() == 0) {
            getClient().announce(MaplePacketCreator.getAuthSuccess(getClient()));
        } else {
            getClient().announce(MaplePacketCreator.getLoginFailed(7));
        }
        return null;
    }
}
