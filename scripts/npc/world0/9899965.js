/*
 
    Author: Lucasdieswagger @ discord
 
 */
 
let Arcade = Packages.com.lucianms.client.arcade.Arcade;
let BoxSpider = Java.type("com.lucianms.client.arcade.BoxSpider");
 
 var sections = {};
 var method = null;
 var status = 0;
 var text = "";

 
function start() {
    action(1, 0, 0);
}

sections["Play Box Spider (Current highscore: "] = function(mode, type, selection) {
    if(status >= 2) {
        cm.getPlayer().setArcade(new BoxSpider(cm.getPlayer()));
        var minigame = cm.getPlayer().getArcade();
        if(minigame != null) {
            minigame.start();
        }
    cm.dispose();
    }
};

sections["How to play"] = function(mode, type, selection) {
    if(status >= 2) {
        cm.sendOk("When playing this game there is 2 main objectives \r\n\r\n#r1. Do not get touched by the spiders coming down \r\n2. Destroy the boxes to get points#k\r\n\r\nIf your highscore is in the top 50, you'll be listed in the highscores.");
        cm.dispose();
    }
};

sections["Highscores"] = function(mode, type, selection) {
    if(status >= 2) {
        var fuck = Arcade.getTop(2);
        cm.sendOk("This is the current top 50 of Box Spider \r\n\r\n" + (fuck == null ? "#rThere are no highscores yet..#" : fuck));
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
            text = "Are you daring enough to test your luck in this Box Spider minigame?\r\n";
        var i = 0;
        for (var s in sections) {
            text += "\r\n#b#L" + (i++) + "#" + s + (i == 1 ? (Arcade.getHighscore(2, cm.getPlayer())) + ")" : "") + "#l#k";
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