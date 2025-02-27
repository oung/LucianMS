package com.lucianms.command.executors;

import com.lucianms.client.MapleCharacter;
import com.lucianms.client.MapleDisease;
import com.lucianms.client.inventory.Equip;
import com.lucianms.client.inventory.Item;
import com.lucianms.client.inventory.MapleInventoryType;
import com.lucianms.client.meta.ForcedStat;
import com.lucianms.command.Command;
import com.lucianms.command.CommandArgs;
import com.lucianms.command.CommandEvent;
import com.lucianms.constants.ItemConstants;
import com.lucianms.constants.ServerConstants;
import com.lucianms.features.FollowTheLeader;
import com.lucianms.features.GenericEvent;
import com.lucianms.features.ManualPlayerEvent;
import com.lucianms.features.coconut.CoconutEvent;
import com.lucianms.features.controllers.HotPotatoController;
import com.lucianms.lang.GProperties;
import com.lucianms.server.MapleItemInformationProvider;
import com.lucianms.server.channel.MapleChannel;
import com.lucianms.server.life.*;
import com.lucianms.server.maps.MapleMap;
import com.lucianms.server.maps.MapleMapItem;
import com.lucianms.server.world.MapleParty;
import com.lucianms.server.world.MaplePartyCharacter;
import com.lucianms.server.world.MapleWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This can get messy so I'm separating event related commands from regular GM commands
 *
 * @author izarooni
 */
public class EventCommands extends CommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventCommands.class);
    private static ArrayList<String> HELP_LIST;
    private static final Point[] OX_QUIZ_O = {
            new Point(-719, -26), new Point(-637, -26), new Point(-547, -26), // top
            new Point(-454, 34), new Point(-454, 94), new Point(-454, 154), // right
            new Point(-816, 34), new Point(-816, 94), new Point(-816, 154), // left
            new Point(-719, 214), new Point(-637, 214), new Point(-547, 214) // bottom
    };
    private static final Point[] OX_QUIZ_X = {
            new Point(-8, -26), new Point(366, -26),
            new Point(82, 34), new Point(262, 34),
            new Point(175, 94),
            new Point(82, 154), new Point(262, 154),
            new Point(-8, 214), new Point(366, 214),
    };

    public EventCommands() {
        addCommand("eventcmds", this::CommandList, "View a list of event commands");
        addCommand("help", this::CommandHelp, "View a list of help commands for each GM level");
        addCommand("event", this::CommandEvent, "View commands to create a GM event");

        addCommand("lock", this::CommandLock, "Gives the SEAL debuff to specified players");
        addCommand("reverse", this::CommandReverse, "Gives the CONFUSE debuff to specified players");
        addCommand("seduce", this::CommandSeduce, "Gives the SEDUCE debuff to specified players");
        addCommand("stun", this::CommandStun, "Gives the STUN debuff to specified players");

        addCommand("lockm", this::CommandLock, "Gives the SEAL debuff to all players in the map");
        addCommand("reversem", this::CommandReverse, "Gives the CONFUSE debuff to all players in the map");
        addCommand("seducem", this::CommandSeduce, "Gives the SEDUCE debuff to all players in the map");
        addCommand("stunm", this::CommandStun, "Gives the STUN debuff to all players in the map");

        addCommand("pcheck", this::CommandPartyCheck, "Check for all parties in the map (shows leaders and memebers)");
        addCommand("ak", this::CommandAutoKill, "Configure auto-kill position or mob for the map");
        addCommand("bomb", this::CommandBomb, "Spawn a bomb at your position (Default 1.5s delay for explosion)");
        addCommand("bod", this::CommandBoxOfDoom, "Spawn a box with randomized HP");
        addCommand("warpoxleft", this::CommandOXWarp, "Warp all players on the left side of the OX Quiz map");
        addCommand("warpoxright", this::CommandOXWarp, "Warp all players on the right side of the OX Quiz map");
        addCommand("warpoxmiddle", this::CommandOXWarp, "Warp all players in the middle of the OX Quiz map");
        addCommand("warpout", this::CommandWarpout, "Warp specified players to the specified map");
        addCommand("potato", this::CommandPotato, "Begin a Hot Potato minigame for all players in the map");
        addCommand("weenie", this::CommandWeenie, "Begin a Follow the Weenie minigame for all players in the map");
        addCommand("fstat", this::CommandForceStat, "Enable temporary stats for yourself");
        addCommand("rules", this::CommandEventRules, "Automate event rule messages for a specified event");
        addCommand("fstatm", this::CommandForceStatMap, "Enable temporary stats for all players in the map");
        addCommand("coconut", this::CommandCoconutEvent, "Begin the coconut event (if in the proper map)");
        addCommand("bombo", this::BombOXQuiz, "Spawn bombs randomly on the O side of the OX Quiz map");
        addCommand("bombx", this::BombOXQuiz, "Spawn bombs randomly on thet X side of the OX Quiz map");
        addCommand("nearest", this::NearestPlayers, "List players in order of proximity");
        addCommand("rnum", this::RandomNumber, "Announces a random number");
        addCommand("nti", this::NameTheItem, "Drops an item and detects for name call-out");
        addCommand("ttm", this::ToggleTagMap, "Toggles the tag command for all players in the map");
        addCommand("tbm", this::ToggleBombMap, "Toggles the bomb command for all players in the map");

        Map<String, Pair<CommandEvent, String>> commands = getCommands();
        HELP_LIST = new ArrayList<>(commands.size());
        for (Map.Entry<String, Pair<CommandEvent, String>> e : commands.entrySet()) {
            HELP_LIST.add(String.format("!%s - %s", e.getKey(), e.getValue().getRight()));
        }
        HELP_LIST.sort(String::compareTo);
        reloadEventRules();
    }

    private void ToggleBombMap(MapleCharacter player, Command cmd, CommandArgs args) {
        MapleMap map = player.getMap();
        boolean enabled = args.length() == 0 ? !((boolean) map.getVariables().checkProperty(PlayerCommands.BOMB_TOGGLE, false))
                : args.get(0).equalsIgnoreCase("on");
        map.getVariables().put(PlayerCommands.BOMB_TOGGLE, enabled);
        map.sendMessage(6, " The @bomb command has been {}", (enabled ? "enabled" : "disabled"));
    }

    private void ToggleTagMap(MapleCharacter player, Command cmd, CommandArgs args) {
        MapleMap map = player.getMap();
        boolean enabled = args.length() == 0 ? !((boolean) map.getVariables().checkProperty(PlayerCommands.TAG_TOGGLE, false))
                : args.get(0).equalsIgnoreCase("on");
        map.getVariables().put(PlayerCommands.TAG_TOGGLE, enabled);
        map.sendMessage(6, " The @tag command has been {}", (enabled ? "enabled" : "disabled"));
    }

    private void NameTheItem(MapleCharacter player, Command cmd, CommandArgs args) {
        if (args.length() != 1) {
            player.sendMessage("Syntax: !{} <item ID>", cmd.getName());
            return;
        }
        Number n = args.parseNumber(0, int.class);
        if (n == null) {
            player.sendMessage(args.getFirstError());
            return;
        }
        String name = MapleItemInformationProvider.getInstance().getName(n.intValue());
        if (name != null) {
            Item item;
            if (ItemConstants.getInventoryType(n.intValue()) != MapleInventoryType.EQUIP) {
                item = new Item(n.intValue());
            } else {
                item = new Equip(n.intValue());
            }
            MapleMapItem dropItem = player.getMap().spawnItemDrop(player, player, item, player.getPosition(), true, true);
            dropItem.setPickedUp(true); // prevent looting
            player.getMap().getVariables().put("nti", new Pair<>(name, dropItem));
            player.sendMessage(6, "Name That Item detection started");
        } else {
            player.sendMessage(5, "The item {} has no name availble on the server", n.intValue());
        }
    }

    private void RandomNumber(MapleCharacter player, Command cmd, CommandArgs args) {
        if (args.length() != 1) {
            player.sendMessage(5, "Syntax: !{} <max number>", cmd.getName());
            return;
        }
        Number n = args.parseNumber(0, int.class);
        if (n == null) {
            player.sendMessage(args.getFirstError());
            return;
        }
        int selected = Randomizer.nextInt(n.intValue());
        player.getMap().sendMessage(6, "Random number chosen: {}", selected);
    }

    private void NearestPlayers(MapleCharacter player, Command cmd, CommandArgs args) {
        List<MapleCharacter> players = player.getMap().getPlayers(p -> p.getGMLevel() == 0 || p.isDebug());
        players.sort(new Comparator<MapleCharacter>() {
            @Override
            public int compare(MapleCharacter o1, MapleCharacter o2) {
                double d1 = o1.getPosition().distance(player.getPosition());
                double d2 = o2.getPosition().distance(player.getPosition());
                return Double.compare(d1, d2);
            }
        });
        MapleCharacter[] arr = players.toArray(new MapleCharacter[0]);
        player.sendMessage(6, "Here are the nearest {} players", arr.length);
        for (int i = arr.length - 1; i >= 0; i--) {
            MapleCharacter target = arr[i];
            double distance = target.getPosition().distance(player.getPosition());
            player.sendMessage(6, "{} is {} units away", target.getName(), new BigDecimal(distance).round(new MathContext(7)));
        }
        arr = null;
        players.clear();
    }

    private void BombOXQuiz(MapleCharacter player, Command cmd, CommandArgs args) {
        if (player.getMapId() != 109020001) {
            player.sendMessage(5, "This command can only be used in the OX Quiz map");
            return;
        } else if (args.length() != 1) {
            player.sendMessage(5, "Syntax: !{} <number>", cmd.getName());
            return;
        }
        Number n = args.parseNumber(0, int.class);
        if (n == null) {
            player.sendMessage(5, args.getFirstError());
            return;
        }
        Point[] src = cmd.equals("bombo") ? OX_QUIZ_O : OX_QUIZ_X;
        Point[] dest = new Point[src.length];
        int platforms = Math.min(n.intValue(), dest.length);
        System.arraycopy(src, 0, dest, 0, src.length);
        player.getMap().sendMessage(5, "Bombing {} platforms", platforms);
        for (int i = 0; i < platforms; i++) {
            MapleMonster bomb = MapleLifeFactory.getMonster(ServerConstants.BOMB_MOB);
            if (bomb != null) {
                int selected = Randomizer.nextInt(dest.length);
                if (dest[selected] == null) {
                    // always pick a unique platform
                    i--;
                    continue;
                }
                Point pos = dest[selected].getLocation();
                dest[selected] = null;
                bomb.setPosition(pos);
                bomb.getStats().getSelfDestruction().setRemoveAfter(1000);
                player.getMap().spawnMonsterOnGroudBelow(bomb, pos);
            }
        }
        dest = null;
    }

    private void CommandCoconutEvent(MapleCharacter player, Command cmd, CommandArgs args) {
        CoconutEvent event = player.getMap().getCoconut();
        if (event == null) {
            player.sendMessage(5, "You are not in a coconut event map");
            return;
        }
        event.begin(player.getMap());
    }

    private void CommandList(MapleCharacter player, Command cmd, CommandArgs args) {
        boolean npc = args.length() == 1 && args.get(0).equalsIgnoreCase("npc");
        if (npc) {
            StringBuilder sb = new StringBuilder();
            for (String s : HELP_LIST) {
                String[] split = s.split(" - ");
                sb.append("\r\n#b").append(split[0]).append("#k - #r").append(split[1]);
            }
            player.announce(MaplePacketCreator.getNPCTalk(2007, (byte) 0, sb.toString(), "00 00", (byte) 0));
            sb.setLength(0);
        } else {
            HELP_LIST.forEach(player::sendMessage);
        }
    }

    private void CommandHelp(MapleCharacter player, Command command, CommandArgs args) {
        player.sendMessage(6, "!eventcmds - Event commands");
        if (player.getGMLevel() >= 2) player.sendMessage(6, "!gmcmds - Level 2 GM commands");
        if (player.getGMLevel() >= 3) player.sendMessage(6, "!hgmcmds - Level 3 Head-GM commands");
        if (player.getGMLevel() >= 6) player.sendMessage(6, "!admincmds - Level 6 Administrator commands");
    }

    private void CommandForceStatMap(MapleCharacter player, Command cmd, CommandArgs args) {
        Collection<MapleCharacter> players = player.getMap().getPlayers();
        if (args.length() == 0) {
            for (MapleCharacter target : players) {
                target.setForcedStat(null);
                target.announce(MaplePacketCreator.getForcedStatReset());
            }
            player.getMap().sendMessage(5, "Forced stats cleared");
        } else if (args.length() == 1) {
            Number n = args.parseNumber(0, int.class);
            if (n == null) {
                player.sendMessage("{} is not a number", args.get(0));
                return;
            }
            for (MapleCharacter target : players) {
                ForcedStat fstat = new ForcedStat();
                fstat.enableAll(n.intValue());
                target.setForcedStat(fstat);
                target.announce(MaplePacketCreator.getForcedStats(fstat));
            }
            player.getMap().sendMessage(5, "Forced stats set");
        } else {
            ForcedStat fstat = new ForcedStat();
            for (ForcedStat.Type type : ForcedStat.Type.values()) {
                int typeIdx = args.findArg(type.name().toLowerCase());
                if (typeIdx > -1) {
                    Number n = args.parseNumber(typeIdx, int.class);
                    if (n == null) {
                        player.sendMessage("{} is not a number", args.get(0));
                        return;
                    }
                    fstat.enable(type, n.intValue());
                }
            }
            for (MapleCharacter target : players) {
                target.setForcedStat(fstat);
                target.announce(MaplePacketCreator.getForcedStats(fstat));
            }
            player.getMap().sendMessage(5, "Forced stats set");
        }
        players.clear();
    }

    private static TreeMap<String, List<String>> EVENT_RULES = new TreeMap<>();

    private static boolean reloadEventRules() {
        EVENT_RULES.clear();
        File file = new File("resources/event-rules.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String title = null;
                String text;
                while ((text = reader.readLine()) != null) {
                    if (!text.isEmpty()) {
                        if (title == null) {
                            EVENT_RULES.put((title = text), new ArrayList<>());
                        } else {
                            EVENT_RULES.get(title).add(text);
                        }
                    } else {
                        title = null;
                    }
                }
                return true;
            } catch (IOException e) {
                LOGGER.error("Error while reading {}", file.getName(), e);
            }
        }
        return false;
    }

    private void CommandEventRules(MapleCharacter player, Command cmd, CommandArgs args) {
        if (args.length() == 0) {
            EVENT_RULES.keySet().forEach(player::sendMessage);
            player.sendMessage("syntax: !{} <event name/reload>", cmd.getName());
            player.sendMessage("Available event rules are listed above");
            return;
        }
        String input = args.concatFrom(0);
        if (input.equalsIgnoreCase("reload")) {
            if (reloadEventRules()) {
                player.sendMessage("Found {} event rules", EVENT_RULES.size());
            } else {
                player.sendMessage("Failed to reload event rules");
            }
        } else {
            Collection<String> found = EVENT_RULES.keySet().stream()
                    .filter(k -> Pattern.compile(input.replaceAll(" |[^a-zA-Z0-9]", ".*"), Pattern.CASE_INSENSITIVE).matcher(k).find())
                    .collect(Collectors.toList());
            if (!found.isEmpty()) {
                if (found.size() == 1) {
                    String eventName = found.iterator().next();
                    List<String> rules = EVENT_RULES.get(eventName);
                    for (String line : rules) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception ignore) {
                            // oh well, it's just for visual effect; continue displaying the message
                        }
                        player.getMap().sendPacket(MaplePacketCreator.getChatText(player.getId(), line, true, false));
                    }
                    player.announce(MaplePacketCreator.serverNotice(2, player.getClient().getChannel(), String.format("[Rules] : Showing rules for event '%s'", eventName)));
                } else {
                    player.sendMessage("Found rules matching event name '{}'", input);
                    found.forEach(player::sendMessage);
                }
            } else {
                player.sendMessage("Unable to find any rules for an event named '{}'", input);
            }
            found.clear();
        }
    }

    private void CommandForceStat(MapleCharacter player, Command cmd, CommandArgs args) {
        if (args.length() == 0) {
            player.setForcedStat(null);
            player.announce(MaplePacketCreator.getForcedStatReset());
            player.sendMessage("Forced stats cleared");
        } else if (args.length() == 1) {
            Number n = args.parseNumber(0, int.class);
            if (n == null) {
                player.sendMessage("{} is not a number", args.get(0));
                return;
            }
            ForcedStat fstat = new ForcedStat();
            fstat.enableAll(n.intValue());
            player.setForcedStat(fstat);
            player.announce(MaplePacketCreator.getForcedStats(fstat));
            player.sendMessage("Forced stats set");
        } else {
            ForcedStat fstat = new ForcedStat();
            for (ForcedStat.Type type : ForcedStat.Type.values()) {
                int typeIdx = args.findArg(type.name().toLowerCase());
                if (typeIdx > -1) {
                    Number n = args.parseNumber(typeIdx, int.class);
                    if (n == null) {
                        player.sendMessage("{} is not a number", args.get(0));
                        return;
                    }
                    fstat.enable(type, n.intValue());
                }
            }
            player.setForcedStat(fstat);
            player.announce(MaplePacketCreator.getForcedStats(fstat));
            player.sendMessage("Forced stats set");
        }
    }

    private void CommandWeenie(MapleCharacter player, Command cmd, CommandArgs args) {
        Optional<GenericEvent> first = player.getGenericEvents().stream().filter(g -> g instanceof FollowTheLeader).findFirst();
        if (first.isPresent()) {
            first.get().dispose();
            player.sendMessage(5, "Follow the Weenie has stopped");
        } else {
            FollowTheLeader leader = new FollowTheLeader(player);
            player.getMap().sendMessage(5, "{} has started Follow the Weenie", player.getName());
        }
    }

    private void CommandPotato(MapleCharacter player, Command command, CommandArgs args) {
        MapleCharacter target;
        MapleMap map = player.getMap();

        if (args.length() == 1) {
            target = map.getCharacterByName(args.get(0));
            if (target == null) {
                player.sendMessage("Unable to find any player named '{}'", args.get(0));
                return;
            }
        } else {
            target = map.getCharacters().stream().filter(p -> !(p instanceof FakePlayer)).findAny().get();
        }
        HotPotatoController potato = new HotPotatoController();
        potato.setMap(map);
        potato.registerPlayer(target);
        potato.start();
    }

    private void CommandEvent(MapleCharacter player, Command command, CommandArgs args) {
        if (args.length() == 0) {
            player.sendMessage("Use: '!event new' to begin configuring your event.");
            player.sendMessage("Use: '!event help' for a list of relevant commands.");
            player.sendMessage("To quick-start a basic event use the following commands in this order:");
            player.sendMessage("!event new - Create a new event");
            player.sendMessage("!event \"your event name\" - Assign a name to your event to display to the world");
            player.sendMessage("!event start - Creates an announcement for your event and allows players to join");
            return;
        }
        MapleWorld world = player.getClient().getWorldServer();
        MapleChannel ch = player.getClient().getChannelServer();
        ManualPlayerEvent playerEvent = player.getClient().getWorldServer().getPlayerEvent();

        switch (args.get(0)) {
            case "help": {
                ArrayList<String> list = new ArrayList<>(8);
                list.add("!event info - View configuration of the current event");
                list.add("!event start - Open your event publicly");
                list.add("!event cancel - Cancel configurations and closes the gates");
                list.add("!event [name] - View or change the name of the current event");
                list.add("!event sp - Set the spawn point of the event");
                list.add("!event gate <time> - Set the delay (in seconds) before the gate automatically closes");
                list.add("!event winners <add/remove> <usernames...> - Add or remove winners from the list of winners");
                list.add("!event winners view - View all current winners and their points");
                list.forEach(player::sendMessage);
                list.clear();
                break;
            }
            case "new":
                if (playerEvent == null) {
                    world.setPlayerEvent((playerEvent = new ManualPlayerEvent(player)));
                    playerEvent.setMap(player.getMap());
                    playerEvent.setChannel(ch);
                    player.sendMessage("Event creation started. To set the name of your event, use: '!event name <event_name>'");
                    player.sendMessage("If you would rather immediately start the event with default values, use: '!event start'");
                    player.sendMessage("You may also abort this event creation via '!event cancel'");
                } else {
                    player.sendMessage("An event is already being hosted in this channel!");
                    player.sendMessage("Use < !event info > for more information");
                }
                break;
            case "cancel":
                if (playerEvent != null) {
                    world.setPlayerEvent(null);
                    playerEvent.garbage();
                    player.sendMessage("You have cancelled the event");
                } else {
                    player.sendMessage("There is no event on this channel right now");
                }
                break;
        }

        if (playerEvent != null) {
            String action = args.get(0).toLowerCase();
            switch (action) {
                case "info":
                    player.sendMessage("------------------------------");
                    player.sendMessage("Host: " + playerEvent.getHost().getName());
                    player.sendMessage("Name: " + playerEvent.getName());
                    player.sendMessage(6, "Map: <{}> {}", player.getMapId(), player.getMap().getMapName());
                    player.sendMessage("Gates: " + (playerEvent.isOpen() ? "open" : "closed"));
                    player.sendMessage("Gate delay: " + playerEvent.getGateTime());
                    player.sendMessage("Winners: " + playerEvent.getWinners().keySet());
                    break;
                case "start": {
                    playerEvent.openGates(playerEvent.getGateTime(), 90, 75, 60, 30, 15, 5, 3, 2, 1);
                    String eventName = (playerEvent.getName() == null) ? "event" : playerEvent.getName();
                    playerEvent.broadcastMessage(String.format("%s is hosting a(n) %s in channel %d, use @joinevent to join!", player.getName(), eventName, playerEvent.getChannel().getId()));
                    break;
                }
                case "name":
                    if (args.length() > 1) {
                        String name = args.concatFrom(1);
                        playerEvent.setName(name);
                        player.sendMessage("Event name changed to " + name);
                    } else {
                        player.sendMessage(6, "Current event name: '{}'", playerEvent.getName());
                    }
                    break;
                case "sp":
                case "spawnpoint":
                    playerEvent.setSpawnPoint(player.getPosition());
                    player.sendMessage("Spawn point has been set to your position");
                    break;
                case "gate": {
                    if (args.length() > 1) {
                        Integer time = args.parseNumber(1, int.class);
                        String error = args.getFirstError();
                        if (error != null) {
                            player.sendMessage(5, error);
                            break;
                        }
                        playerEvent.setGateTime(time);
                        player.sendMessage(String.format("Event time is now set to %d seconds", time));
                    } else {
                        player.sendMessage(5, "You must specify a time (in seconds) to set your gate timer");
                    }
                    break;
                }
                case "close": {
                    if (playerEvent.isOpen()) {
                        playerEvent.setOpen(false);
                        world.sendMessage(6, "[Event] The gates are now closed");
                        if (playerEvent.getGateTime() == 0) {
                            // manual gate closing
                            playerEvent.broadcastMessage("The gate is now closed");
                        } else {
                            player.sendMessage("You have closed the gate");
                        }
                    } else {
                        player.sendMessage("The gate is already closed");
                    }
                    break;
                }
                case "end": {
                    if (!playerEvent.getWinners().isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Congrats to ");
                        for (Map.Entry<String, Integer> entry : playerEvent.getWinners().entrySet()) {
                            sb.append(entry.getKey()).append("(").append(entry.getValue()).append(")").append(", ");
                        }
                        sb.setLength(sb.length() - 2);
                        String name = (playerEvent.getName() == null) ? "the event" : playerEvent.getName();
                        sb.append(" on winning ").append(name);
                        playerEvent.broadcastMessage(sb.toString());
                    }
                    world.setPlayerEvent(null);
                    playerEvent.garbage();
                    player.sendMessage("Event ended");
                    break;
                }
                case "winners": {
                    if (args.length() == 0) {
                        player.sendMessage(5, "Remove players from the winner list regardless of how many points they have via: '!event winners remove <usernames>");
                        player.sendMessage(5, "Append players to the winner list via: '!event winners add <usernames...>'");
                        player.sendMessage(5, "Player names are split with a space e.g.: '!event winners add {}'", player.getName());
                        break;
                    }
                    // !event winners add/remove <usernames>
                    switch (args.get(1).toLowerCase()) {
                        case "add": {
                            if (args.length() > 2) {
                                String[] usernames = args.concatFrom(2).split(" ");
                                playerEvent.addWinners(usernames);
                                if (usernames.length == 1) {
                                    player.sendMessage(usernames[0] + " added to list of winners");
                                } else {
                                    player.sendMessage("Specified players are now winners");
                                }
                            } else {
                                player.sendMessage(5, "You must specify at least 1 username");
                            }
                            break;
                        }
                        case "remove": {
                            if (args.length() > 2) {
                                String[] usernames = args.concatFrom(2).split(" ");
                                playerEvent.removeWinners(usernames);
                                if (usernames.length == 1) {
                                    player.sendMessage(usernames[0] + " removed from list of winners");
                                } else {
                                    player.sendMessage("Specified players are now longer winners");
                                }
                            } else {
                                player.sendMessage(5, "You must specify at least 1 username");
                            }
                            break;
                        }
                        case "list":
                        case "view": {
                            Map<String, Integer> w = playerEvent.getWinners();
                            if (w.isEmpty()) {
                                player.sendMessage("There are no winners right now");
                            } else {
                                player.sendMessage("Here are the current winners");
                                StringBuilder sb = new StringBuilder();
                                for (Map.Entry<String, Integer> entry : w.entrySet()) {
                                    sb.append(entry.getKey()).append("(").append(entry.getValue()).append(")").append(", ");
                                }
                                sb.setLength(sb.length() - 2);
                                player.sendMessage(sb.toString());
                            }
                            break;
                        }
                    }
                    player.sendMessage("There are now " + playerEvent.getWinners().size() + " in the winner list");
                    break;
                }
            }
            return;
        }
    }

    private void CommandBoxOfDoom(MapleCharacter player, Command command, CommandArgs args) {
        if (args.length() == 2) {
            Integer lowHP = args.parseNumber(0, int.class);
            Integer maxHP = args.parseNumber(1, int.class);
            if (lowHP == null || maxHP == null) {
                player.sendMessage(args.getFirstError());
                return;
            }
            MapleMonster monster = MapleLifeFactory.getMonster(9500365);
            if (monster != null) {
                MapleMonsterStats stats = new MapleMonsterStats(monster.getStats());
                stats.setExp(0);
                stats.setHp(Randomizer.rand(lowHP, maxHP));
                monster.setOverrideStats(stats);
                monster.getListeners().add(new MonsterListener() {
                    @Override
                    public void monsterKilled(MapleMonster monster, MapleCharacter player) {
                        if (player != null) {
                            monster.getMap().broadcastMessage(5, "Box killed by '{}'", player.getName());
                        }
                    }
                });
                player.getMap().spawnMonsterOnGroudBelow(monster, player.getPosition());
                player.getMap().broadcastGMMessage(5, "Box spawned with {} HP", stats.getHp());
            } else {
                player.sendMessage(5, "Invalid monster");
            }
        } else {
            player.sendMessage(5, "syntax: !{} <min health> <max health>", command.getName());
        }
    }

    private void CommandLock(MapleCharacter player, Command command, CommandArgs args) {
        giveDebuff(player, command, args, MobSkillFactory.getMobSkill(120, 1));
    }

    private void CommandReverse(MapleCharacter player, Command command, CommandArgs args) {
        giveDebuff(player, command, args, MobSkillFactory.getMobSkill(132, 1));
    }

    private void CommandSeduce(MapleCharacter player, Command command, CommandArgs args) {
        giveDebuff(player, command, args, MobSkillFactory.getMobSkill(128, 1));
    }

    private void CommandStun(MapleCharacter player, Command command, CommandArgs args) {
        giveDebuff(player, command, args, MobSkillFactory.getMobSkill(123, 1));
    }

    private void CommandDispel(MapleCharacter player, Command command, CommandArgs args) {
        if (command.equals("dispelm")) {
            for (MapleCharacter players : player.getMap().getCharacters()) {
                if (!players.isGM() || player.isDebug()) {
                    players.dispelDebuffs();
                }
            }
        } else {
            if (args.length() > 0) {
                for (int i = 0; i < args.length(); i++) {
                    String s = args.get(i);
                    MapleCharacter target = player.getMap().getCharacterByName(s);
                    if (target != null) {
                        target.dispelDebuffs();
                    } else {
                        player.sendMessage("Unable to find any player named '{}'", s);
                    }
                }
            } else {
                player.dispelDebuffs();
            }
        }
    }

    private void CommandPartyCheck(MapleCharacter player, Command command, CommandArgs args) {
        ArrayList<Integer> parties = new ArrayList<>();
        ArrayList<String> solo = new ArrayList<>(); // character not in a party
        // get parties and solo players
        for (MapleCharacter players : player.getMap().getCharacters()) {
            if (players.getParty() != null) {
                if (players.getParty().getLeaderPlayerID() == players.getId() && !parties.contains(players.getPartyID())) {
                    parties.add(players.getPartyID());
                }
            } else {
                solo.add(players.getName());
            }
        }
        // get characters from each party
        for (int partyId : parties) {
            StringBuilder sb = new StringBuilder();
            MapleParty party = player.getClient().getWorldServer().getParty(partyId);
            sb.append(party.getLeader().getUsername()).append("'s members: ");
            for (MaplePartyCharacter members : party.values()) {
                if (members.getPlayerID() != party.getLeaderPlayerID()) {
                    sb.append(members.getUsername()).append(", ");
                }
            }
            if (party.size() > 1) {
                sb.setLength(sb.length() - 2);
            }
            player.sendMessage(6, sb.toString());
        }
        if (!solo.isEmpty()) {
            player.sendMessage(6, "Characters NOT in party:");
            player.sendMessage(6, solo.toString());
        }
    }

    private void CommandAutoKill(MapleCharacter player, Command command, CommandArgs args) {
        if (args.length() == 0) {
            player.sendMessage(5, "Available directions: left, right, up and down");
            player.sendMessage(5, "To clear all auto-kill positions use: !{} reset", command.getName());
            player.sendMessage(5, "You may also declare a mob to auto-kill by specifying the mob ID");
            return;
        }
        final String action = args.get(0);
        try {
            int mobID = Integer.parseInt(action);
            player.getMap().getAutoKillMobs().put(action, true);
            player.sendMessage(6, "Enabled auto-kill for the monster {} for this map", mobID);
            return;
        } catch (NumberFormatException ignore) {
        }
        GProperties<Point> akp = player.getMap().getAutoKillPositions();
        switch (action) {
            case "enable":
            case "disable":
                boolean enabled = action.equalsIgnoreCase("enable");
                player.getMap().getVariables().put(MapleMap.AUTO_KILL_TOGGLE, enabled);
                player.sendMessage(6, "Auto kill is now {}", (enabled ? "enabled" : "disabled"));
                break;
            case "left":
            case "right":
            case "up":
            case "down":
                Point location = player.getPosition().getLocation();
                akp.put(action, location);
                player.sendMessage(6, "Auto-kill {} position set to [{}, {}]", action, location.x, location.y);
                break;
            case "reset":
            case "clear":
                akp.clear();
                player.getMap().getAutoKillMobs().clear();
                player.sendMessage(6, "Auto-kill positions and mobs cleared");
                break;
        }

    }

    private void CommandBomb(MapleCharacter player, Command command, CommandArgs args) {
        if (command.equals("bombm")) {
            for (MapleCharacter players : player.getMap().getCharacters()) {
                for (int i = -5; i < 5; i++) {
                    MapleMonster bomb = MapleLifeFactory.getMonster(ServerConstants.BOMB_MOB);
                    if (bomb == null) {
                        player.sendMessage(5, "An error occurred");
                        return;
                    }
                    Point pos = players.getPosition().getLocation();
                    pos.x += (i * 30);
                    player.getMap().spawnMonsterOnGroudBelow(bomb, pos);
                }
            }
        } else {
            final int timeIndex = args.findArg("-time");
            Float time = args.parseNumber(timeIndex, 1.5f, float.class);
            String error = args.getFirstError();
            if (error != null) {
                player.sendMessage(5, error);
                return;
            } else if (timeIndex > 0) {
                time = Math.max(0, time);
                player.sendMessage("Bomb timer set to {}s", time);
            }
            if (args.length() == 0 || (args.length() == 2 && timeIndex > 0)) {
                MapleMonster bomb = MapleLifeFactory.getMonster(ServerConstants.BOMB_MOB);
                if (bomb == null) {
                    player.sendMessage(5, "An error occurred");
                    return;
                }
                bomb.getStats().getSelfDestruction().setRemoveAfter((int) (time * 1000f));
                player.getMap().spawnMonsterOnGroudBelow(bomb, player.getPosition());
            } else {
                for (int i = 0; i < args.length(); i++) {
                    MapleMonster bomb = MapleLifeFactory.getMonster(ServerConstants.BOMB_MOB);
                    if (bomb == null) {
                        player.sendMessage(5, "An error occurred");
                        return;
                    }
                    String username = args.get(i);
                    MapleCharacter target = player.getMap().getCharacterByName(username);
                    if (target != null) {
                        bomb.getStats().getSelfDestruction().setRemoveAfter((int) (time * 1000f));
                        target.getMap().spawnMonsterOnGroudBelow(bomb, target.getPosition());
                    } else {
                        player.sendMessage(5, "Unable to find any player named '{}'", username);
                    }
                }
            }
        }
    }

    private void CommandOXWarp(MapleCharacter player, Command command, CommandArgs args) {
        if (player.getMapId() != 109020001) {
            player.sendMessage(5, "You cannot use this command here");
            return;
        }
        Collection<MapleCharacter> characters = new ArrayList<>(player.getMap().getCharacters());
        for (MapleCharacter players : characters) {
            if (!players.isGM() || player.isDebug()) {
                Point location = players.getPosition().getLocation();
                if (location.x >= -142 && command.equals("warpoxright")) {
                    players.changeMap(ServerConstants.HOME_MAP);
                } else if (location.x >= -307 && location.x <= -143 && command.equals("warpoxmiddle")) {
                    players.changeMap(ServerConstants.HOME_MAP);
                } else if (location.x <= -308 && command.equals("warpoxleft")) {
                    players.changeMap(ServerConstants.HOME_MAP);
                }
            }
        }
        characters.clear();
    }

    private void CommandWarpout(MapleCharacter player, Command command, CommandArgs args) {
        if (args.length() == 0) {
            player.sendMessage(5, "syntax: !{} <map> <usernames...>", command.getName());
            return;
        }
        Integer fieldID = args.parseNumber(0, int.class);
        if (fieldID == null) {
            if (args.get(0).equalsIgnoreCase("home")) {
                fieldID = ServerConstants.HOME_MAP;
            } else {
                player.sendMessage(5, args.getFirstError());
                return;
            }
        }
        for (int i = 1; i < args.length(); i++) {
            String s = args.get(i);
            MapleCharacter target = player.getMap().getCharacterByName(s);
            if (target != null) {
                target.changeMap(fieldID);
            } else {
                player.sendMessage(5, "Unable to find any player named '{}'", s);
            }
        }
    }

    private static void giveDebuff(MapleCharacter player, Command command, CommandArgs args, MobSkill skill) {
        boolean map = command.getName().endsWith("m");
        MapleDisease disease;
        switch (skill.getSkillId()) {
            default:
                return;
            case 120:
                disease = MapleDisease.SEAL;
                break;
            case 121:
                disease = MapleDisease.DARKNESS;
                break;
            case 122:
                disease = MapleDisease.WEAKEN;
                break;
            case 123:
                disease = MapleDisease.STUN;
                break;
            case 124:
                disease = MapleDisease.CURSE;
                break;
            case 125:
                disease = MapleDisease.POISON;
                break;
            case 126:
                disease = MapleDisease.SLOW;
                break;
            case 128:
                disease = MapleDisease.SEDUCE;
                if (args.length() > 1) {
                    String direction = args.get(1);
                    if (direction.equalsIgnoreCase("right")) {
                        skill = MobSkillFactory.getMobSkill(128, 2);
                    } else if (direction.equalsIgnoreCase("left")) {
                        skill = MobSkillFactory.getMobSkill(128, 1);
                    } else if (direction.equalsIgnoreCase("down")) {
                        skill = MobSkillFactory.getMobSkill(128, 11);
                    } else {
                        player.sendMessage(5, "The only seduces available are 'left', 'right' and 'down'");
                        return;
                    }
                } else {
                    player.sendMessage(5, "syntax: !{} <duration (seconds)> <direction> {}", command.getName(), (map ? "" : "<usernames...>"));
                    return;
                }
                break;
            case 132:
                disease = MapleDisease.CONFUSE;
                break;
        }
        skill.setDuration(5000); // default duration
        if (map) {
            if (args.length() > 0) {
                Integer number = args.parseNumber(0, int.class);
                if (number == null) {
                    player.sendMessage(5, args.getFirstError());
                    return;
                }
                skill.setDuration(number * 1000);
            }
            for (MapleCharacter players : player.getMap().getCharacters()) {
                if (!players.isGM() || players.isDebug()) {
                    if (skill.getSkillId() == 128) { // seduce
                        players.setChair(0);
                        players.announce(MaplePacketCreator.cancelChair(-1));
                        players.getMap().broadcastMessage(players, MaplePacketCreator.showChair(players.getId(), 0), false);
                    }

                    players.giveDebuff(disease, skill);
                }
            }
            return;
        } else if (args.length() > 0) {

            Integer duration = args.parseNumber(0, int.class);
            if (duration == null) {
                player.sendMessage(5, args.getFirstError() + " - please specify a duration for the debuff");
                return;
            }
            skill.setDuration(duration * 1000);

            for (int i = (disease == MapleDisease.SEDUCE ? 2 : 1); i < args.length(); i++) {
                String s = args.get(i);
                MapleCharacter target = player.getMap().getCharacterByName(s);
                if (target != null) {
                    if (!target.isGM() || target.isDebug()) {
                        if (skill.getSkillId() == 128) { // seduce
                            target.setChair(0);
                            target.announce(MaplePacketCreator.cancelChair(-1));
                            target.getMap().broadcastMessage(target, MaplePacketCreator.showChair(target.getId(), 0), false);
                        }
                        skill.apply(target);
                    } else {
                        player.sendMessage(5, "You cannot debuff the player '{}'", s);
                    }
                } else {
                    player.sendMessage(5, "Unable to find any player named '{}'", s);
                }
            }
            return;
        }
        player.sendMessage(5, "syntax: !{} [duration (seconds)] <usernames...>", command.getName());
        player.sendMessage(5, "example: '!{} 3 {}' - to stun for 3 seconds", command.getName(), player.getName());
    }
}
