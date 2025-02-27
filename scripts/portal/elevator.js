function enter(pi) {
    try {
        let elevator = pi.getEventManager("elevator");
        if (elevator == null) {
            pi.getPlayer().dropMessage(5, "The elevator is currently moving.");
        } else if (elevator.getProperty(pi.getMapId() == 222020100 ? ("goingUp") : ("goingDown")).equals("false")) {
            pi.warp(pi.getMapId() == 222020100 ? 222020110 : 222020210, 0);
			elevator.getInvocable().invokeFunction(pi.getMapId() == 222020110 ? "goUp" : "goDown");
			return true;
        } else if (elevator.getProperty(pi.getMapId() == 222020100 ? ("goingUp") : ("goingDown")).equals("true")) {
            pi.getPlayer().dropMessage(5, "The elevator is currently moving.");
        }
    } catch(e) {
        pi.getPlayer().dropMessage(5, "Error: " + e);
    }
	return false;
}  