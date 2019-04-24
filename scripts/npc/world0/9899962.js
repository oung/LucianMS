/*
 
    Author: Lucasdieswagger @ discord
 
 */
 
let Arcade = Java.type("com.lucianms.client.arcade.Arcade");
let BobOnly = Java.type("com.lucianms.client.arcade.BobOnly");
 
 var sections = {};
 var method = null;
 var status = 0;
 var text = "";

 
function start() {
    action(1, 0, 0);
}

sections["Play Bob Only (Current highscore: "] = function(mode, type, selection) {
    if(status >= 2) {
        cm.getPlayer().setArcade(new BobOnly(cm.getPlayer()));
        var minigame = cm.getPlayer().getArcade();
        if(minigame != null) {
            minigame.start();
        }
    cm.dispose();
    }
};

sections["How to play"] = function(mode, type, selection) {
    if(status >= 2) {
        cm.sendOk("When playing this game there is 2 main objectives \r\n\r\n#r1. Do not kill the regular snails \r\n2. Kill bob to get points#k\r\n\r\nIf your highscore is in the top 50, you'll be listed in the highscores.");
        cm.dispose();
    }
};

sections["Highscores"] = function(mode, type, selection) {
    if(status >= 2) {
        var fuck = Arcade.getTop(1);
        cm.sendOk("This is the current top 50 of Bob Only \r\n\r\n" + (fuck == null ? "#rThere are no highscores yet.#" : fuck));
        cm.dispose();
    }
};


function action(mode, type, selection) {
    if (mode === -1) {
        cm.dispose();
        return;
    } else if (mode === 0) {
        status--;
        if (status === 0) {
            cm.dispose();
            return;
        }
    } else {
        status++;
    }
    if (status === 1) {
        method = null;
            text = "Are you daring enough to test your luck in this Bob Only minigame?\r\n";
        var i = 0;
        for (var s in sections) {
            text += "\r\n#b#L" + (i++) + "#" + s + (i == 1 ? (Arcade.getHighscore(1, cm.getPlayer())) + ")" : "") + "#l#k";
        }
        cm.sendSimple(text);
    } else {
        if (method == null) {
            method = sections[get(selection)];
        }
        method(mode, type, selection);
    }
}


function get(index) {
    var i = 0;
    for (var s in sections) {
        if (i === index)
            return s;
        i++;
    }
    return null;
}