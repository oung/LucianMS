/* izarooni */
var status = 0;

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
        return;
    } else {
        status++;
    }
    if (status == 1) {
        cm.sendNext("Are you ready to leave for #bPlanet Aura#k?\r\n\r\nPlanet Aura is a mysterious area located in space. The rumor says that the almighty #rKing Slime#k sometimes makes an appearance there. We will announce whenever we spot him making an appearance so look out in the chat.");
    } else if (status == 2) {
        var event = client.getWorldServer().getScheduledEvent("SOuterSpace");
        if (event != null) {
            if (event.isOpen()) {
                event.registerPlayer(player);
                cm.dispose();
            } else {
                cm.sendOk("Oh, well it seems the threat is no longer present on the planet. There's no need for you to go there right now");
            }
        } else {
            cm.sendOk("An error occurred");
        }
        cm.dispose();
    }
}
