/* 
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program unader any cother version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lucianms.client;

import com.lucianms.client.arcade.Arcade;
import com.lucianms.client.arcade.RPSGame;
import com.lucianms.client.autoban.Cheater;
import com.lucianms.client.inventory.*;
import com.lucianms.client.meta.Achievement;
import com.lucianms.client.meta.Occupation;
import com.lucianms.constants.ExpTable;
import com.lucianms.constants.GameConstants;
import com.lucianms.constants.ItemConstants;
import com.lucianms.constants.ServerConstants;
import com.lucianms.constants.skills.*;
import com.lucianms.cquest.CQuestBuilder;
import com.lucianms.cquest.CQuestData;
import com.lucianms.events.MapleEvents;
import com.lucianms.events.gm.MapleFitness;
import com.lucianms.events.gm.MapleOla;
import com.lucianms.features.GenericEvent;
import com.lucianms.features.ManualPlayerEvent;
import com.lucianms.features.PlayerTitles;
import com.lucianms.io.scripting.Achievements;
import com.lucianms.io.scripting.event.EventInstanceManager;
import com.lucianms.lang.GProperties;
import com.lucianms.scheduler.Task;
import com.lucianms.scheduler.TaskExecutor;
import com.lucianms.server.*;
import com.lucianms.server.channel.MapleChannel;
import com.lucianms.server.guild.MapleGuild;
import com.lucianms.server.guild.MapleGuildCharacter;
import com.lucianms.server.life.FakePlayer;
import com.lucianms.server.life.MapleMonster;
import com.lucianms.server.life.MobSkill;
import com.lucianms.server.maps.*;
import com.lucianms.server.partyquest.PartyQuest;
import com.lucianms.server.quest.MapleQuest;
import com.lucianms.server.world.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import tools.*;

import java.awt.*;
import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class MapleCharacter extends AbstractAnimatedMapleMapObject {

    public static final int[] FISHING_MAPS = {749050500, 749050501, 749050502};
    public static final int[] FISHING_CHAIRS = {3011000, 3010151, 3010184};

    private static final Logger LOGGER = LoggerFactory.getLogger(MapleCharacter.class);
    private static final String LEVEL_200 = "[Congrats] %s has reached Level 200! Congratulate %s on such an amazing achievement!";
    private static final int[] DEFAULT_KEY = {18, 65, 2, 23, 3, 4, 5, 6, 16, 17, 19, 25, 26, 27, 31, 34, 35, 37, 38, 40, 43, 44, 45, 46, 50, 56, 59, 60, 61, 62, 63, 64, 57, 48, 29, 7, 24, 33, 41, 39};
    private static final int[] DEFAULT_TYPE = {4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 5, 6, 6, 6, 6, 6, 6, 5, 4, 5, 4, 4, 4, 4, 4};
    private static final int[] DEFAULT_ACTION = {0, 106, 10, 1, 12, 13, 18, 24, 8, 5, 4, 19, 14, 15, 2, 17, 11, 3, 20, 16, 9, 50, 51, 6, 7, 53, 100, 101, 102, 103, 104, 105, 54, 22, 52, 21, 25, 26, 23, 27};
    private static final String[] BLOCKED_NAMES = {"admin", "owner", "moderator", "intern", "donor", "administrator", "help", "helper", "alert", "notice", "maplestory", "LucianMS", "fuck", "wizet", "fucking", "negro", "fuk", "fuc", "penis", "pussy", "asshole", "gay", "nigger", "homo", "suck", "cum", "shit", "shitty", "condom", "security", "official", "rape", "nigga", "sex", "tit", "boner", "orgy", "clit", "asshole", "fatass", "bitch", "support", "gamemaster", "cock", "gaay", "gm", "operate", "master", "sysop", "party", "GameMaster", "community", "message", "event", "test", "meso", "Scania", "renewal", "yata", "AsiaSoft", "henesys"};
    private static String[] ariantroomleader = new String[3];
    private static int[] ariantroomslot = new int[3];
    private static int FRECEIVAL_ITEM = 2022323;

    private byte pendantExp = 0, lastmobcount = 0;

    private short combocounter = 0;

    //region integers
    private int world;
    private int accountid, id;
    private int rank, rankMove, jobRank, jobRankMove;
    private int level, str, dex, luk, int_, hp, maxhp, mp, maxmp;
    private int hpMpApUsed;
    private int hair;
    private int face;
    private int remainingAp;
    private int fame;
    private int initialSpawnPoint;
    private int mapid;
    private int gender;
    private int currentPage, currentType = 0, currentTab = 1;
    private int chair;
    private int itemEffect;
    private int guildid, guildrank, allianceRank;
    private int messengerposition = 4;
    private int slots = 0;
    private int energybar;
    private int gmLevel;
    private int ci = 0;
    private int familyId;
    private int bookCover;
    private int markedMonster = 0;
    private int battleshipHp = 0;
    private int mesosTraded = 0;
    private int possibleReports = 10;
    private int dojoPoints, vanquisherStage, dojoStage, dojoEnergy, vanquisherKills;
    private int warpToId;
    private int expRate = 1, mesoRate = 1, dropRate = 1;
    private int omokwins, omokties, omoklosses, matchcardwins, matchcardties, matchcardlosses;
    private int married;
    private int merchantmeso;
    private int linkedLevel = 0;
    private int donorLevel;
    private int hidingLevel = 1;

    //region points
    private int eventPoints = 0;
    private int rebirthPoints = 0;
    private int fishingPoints = 0;
    private int jumpQuestPoints = 0;
    //endregion

    private int rebirths = 0;
    private int goal, current;
    private int killType;
    private int riceCakes = 0;
    private transient int localmaxhp, localmaxmp, localstr, localdex, localluk, localint_, magic, watk;
    private int[] remainingSp = new int[10];
    //endregion

    private long lastEmergency = 0;
    private long immortalTimestamp = 0;
    private long portaldelay = 0, lastcombo = 0;
    private long dojoFinish, lastfametime, lastHealed;
    private long createDate;
    private boolean muted = false;
    private boolean debug = false;
    private boolean isbanned = false;
    private boolean autorebirthing = false;
    private boolean eyeScannersEquiped = false;
    private boolean finishedDojoTutorial, dojoParty;
    private boolean hidden, canDoor = true, berserk, hasMerchant, whiteChat = false;

    private String name = null;
    private String search = null;
    private String chalktext = null;
    private String dataString = null;
    private String linkedName = null;

    private AtomicInteger exp = new AtomicInteger();
    private AtomicInteger meso = new AtomicInteger();
    private AtomicInteger gachaexp = new AtomicInteger();

    //region collections
    private ArrayList<Task> tasks = new ArrayList<>();
    private ArrayList<String> blockedPortals = new ArrayList<>();
    private ArrayList<Integer> excluded = new ArrayList<>();
    private ArrayList<Integer> trockmaps = new ArrayList<>();
    private ArrayList<Integer> viptrockmaps = new ArrayList<>();
    private ArrayList<Integer> lastmonthfameids = new ArrayList<>(31);
    private ArrayList<MapleDoor> doors = new ArrayList<>();
    private ArrayList<MapleRing> crushRings = new ArrayList<>();
    private ArrayList<MapleRing> friendshipRings = new ArrayList<>();
    private ArrayList<GenericEvent> genericEvents = new ArrayList<>();

    private GProperties<Boolean> toggles = new GProperties<>();

    private HashMap<Skill, SkillEntry> skills = new HashMap<>();
    private Map<Short, String> area_info = new LinkedHashMap<>();
    private Map<Short, MapleQuestStatus> quests = new LinkedHashMap<>();
    private Map<Integer, String> entered = new LinkedHashMap<>();
    private Map<String, MapleEvents> events = new LinkedHashMap<>();
    private Map<String, Achievement> achievements = new HashMap<>();
    private Map<Integer, CQuestData> customQuests = new HashMap<>();
    private Map<Integer, MapleSummon> summons = new LinkedHashMap<>();
    private HashMap<Integer, MapleKeyBinding> keymap = new HashMap<>();
    private HashMap<Integer, MapleCoolDownValueHolder> coolDowns = new HashMap<>(50);
    //endregion

    private BuddyList buddylist = null;
    private MapleFamily family = null;
    private MapleClient client = null;
    private HiredMerchant hiredMerchant = null;
    private MapleGuildCharacter mgc = null;
    private MaplePartyCharacter mpc = null;
    private EventInstanceManager eventInstance = null;

    private Task[] fullnessSchedule = new Task[3];
    private MaplePet[] pets = new MaplePet[3];
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private MapleInventory[] creativeInventory;
    private MapleInventory[] inventory = new MapleInventory[MapleInventoryType.values().length];

    private MapleJob job = MapleJob.BEGINNER;
    private MapleMap map = null, dojoMap = null;
    private MapleShop shop = null;
    private MapleParty party = null;
    private MapleTrade trade = null;
    private MapleStorage storage = null;
    private MapleMount maplemount = null;
    private MapleMiniGame miniGame = null;
    private MapleMessenger messenger = null;
    private MaplePlayerShop playerShop = null;
    private MapleSkinColor skinColor = MapleSkinColor.NORMAL;
    private SavedLocation[] savedLocations = new SavedLocation[SavedLocationType.values().length];

    private LinkedHashSet<MapleMonster> controlled = new LinkedHashSet<>();
    private ConcurrentHashMap<Integer, MapleMapObject> visibleMapObjects = new ConcurrentHashMap<>();

    private EnumMap<MapleDisease, DiseaseValueHolder> diseases = new EnumMap<>(MapleDisease.class);
    private EnumMap<MapleBuffStat, MapleBuffStatValueHolder> effects = new EnumMap<>(MapleBuffStat.class);

    private Task recoveryTask = null;
    private Task hpDecreaseTask = null;
    private Task expirationTask = null;
    private Task spiritPendantTask = null; // 1122017
    private Task dragonBloodTask = null;
    private Task beholderHealingTask = null;
    private Task beholderBuffTask = null;
    private Task berserkTask = null;
    private Task fishingTask = null;

    private Cheater cheater = new Cheater();
    private CashShop cashshop;
    private MapleRing marriageRing = null;
    private PartyQuest partyQuest = null;
    private MonsterBook monsterbook;

    private Arcade arcade = null;
    private ChatType chatType = ChatType.NORMAL;
    private Timestamp daily = null;
    private FakePlayer fakePlayer = null;
    private MapleDragon dragon = null;
    private PlayerTitles title = null;
    private Relationship relationship = new Relationship();
    private RPSGame RPSGame = null;
    private Occupation occupation = null;
    private final SpamTracker spamTracker = new SpamTracker();

    // EVENTS
    private byte team = 0;
    private MapleFitness fitness;
    private MapleOla ola;
    private long snowballattack;

    // Monster Carnival
    private int carnivalPoints = 0;
    private int obtainedcp = 0;

    public MapleCharacter() {
        setStance(0);
        for (MapleInventoryType type : MapleInventoryType.values()) {
            inventory[type.ordinal()] = new MapleInventory(type, (byte) 96);
        }
        setPosition(new Point(0, 0));
    }

    public static MapleCharacter getDefault(MapleClient c) {
        MapleCharacter ret = new MapleCharacter();
        ret.client = c;
        ret.gmLevel = c.getGMLevel();
        ret.hp = 50;
        ret.maxhp = 50;
        ret.mp = 5;
        ret.maxmp = 5;
        ret.str = 12;
        ret.dex = 5;
        ret.int_ = 4;
        ret.luk = 4;
        ret.map = null;
        ret.job = MapleJob.BEGINNER;
        ret.level = 1;
        ret.accountid = c.getAccID();
        ret.buddylist = new BuddyList(20);
        ret.maplemount = null;
        ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.USE).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.ETC).setSlotLimit(24);
        for (int i = 0; i < DEFAULT_KEY.length; i++) {
            ret.keymap.put(DEFAULT_KEY[i], new MapleKeyBinding(DEFAULT_TYPE[i], DEFAULT_ACTION[i]));
        }
        // to fix the map 0 lol
        for (int i = 0; i < 5; i++) {
            ret.trockmaps.add(999999999);
        }
        for (int i = 0; i < 10; i++) {
            ret.viptrockmaps.add(999999999);
        }

        return ret;
    }

    public static boolean ban(String username, String reason, boolean isAccountName) {
        try (Connection con = Server.getConnection()) {
            int accountID;
            // get account id from character username
            try (PreparedStatement ps = con.prepareStatement("select accountid from characters where name = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        accountID = rs.getInt("accountid");
                    } else {
                        return false;
                    }
                }
            }

            // ban the associated account
            try (PreparedStatement ps = con.prepareStatement("update accounts set banned = 1, banreason = ? where id = ?")) {
                ps.setString(1, username);
                ps.setInt(2, accountID);
                if (ps.executeUpdate() == 0) {
                    return false;
                }
            }

            // ban the ips, hwid and macs associated with the account
            try (PreparedStatement ps = con.prepareStatement("select ip, hwid, macs from accounts where id = ?")) {
                ps.setInt(1, accountID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String IP = rs.getString("ip");
                        String hwid = rs.getString("hwid");
                        String mac = rs.getString("macs").split(", ")[0];

                        if (IP != null && !IP.isEmpty() && !IP.equals("127.0.0.1")) {
                            try (PreparedStatement ip = con.prepareStatement("insert into ipbans values (?)")) {
                                ip.setString(1, IP);
                                ip.executeUpdate();
                            }
                        }
                        if (!hwid.isEmpty()) {
                            try (PreparedStatement h = con.prepareStatement("insert into hwidbans values (?)")) {
                                h.setString(1, hwid);
                                h.executeUpdate();
                            }
                        }
                        if (!mac.isEmpty()) {
                            try (PreparedStatement m = con.prepareStatement("insert into macbans values (?)")) {
                                m.setString(1, mac);
                                m.executeUpdate();
                            }
                        }
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            LOGGER.error("Unable to ban character '{}''s account", username);
        }
        return false;
    }

    public static boolean canCreateChar(String name) {
        for (String nameTest : BLOCKED_NAMES) {
            if (name.toLowerCase().contains(nameTest)) {
                return false;
            }
        }
        return getIdByName(name) < 0 && Pattern.compile("[a-zA-Z0-9]{4,12}").matcher(name).matches();
    }

    public static void deleteMapleCharacter(Connection con, int playerid) throws SQLException {
        if (MapleCharacter.getNameById(playerid) == null) {
            throw new NullPointerException(String.format("Attempt to delete non-existing player (%d)", playerid));
        }
        System.out.println("Deleting player " + MapleCharacter.getNameById(playerid));
        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM area_info WHERE charid = ?", playerid);
        System.out.println("\tDeleted area info");
        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ?", playerid);
        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM buddies WHERE buddyid = ?", playerid);
        System.out.println("\tDeleted buddies");
        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM characters WHERE id = ?", playerid);
        System.out.println("\tDeleted player packet");
        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM cooldowns WHERE charid = ?", playerid);
        System.out.println("\tDeleted cooldowns");

        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cquest WHERE characterid = ?")) {
            ps.setInt(1, playerid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM cquestdata WHERE qtableid = ?")) {
                        ps2.setInt(1, rs.getInt("id"));
                        ps2.executeUpdate();
                    }
                }
            }
        }
        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM cquest WHERE characterid = ?", playerid);
        System.out.println("\tDeleted custom quests");

        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM famelog WHERE characterid_to = ?", playerid);
        System.out.println("\tDeleted fame logs");
        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryitems WHERE characterid = ?", playerid);
        System.out.println("\tDeleted inventory itemes");
        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM jailtimes WHERE playerId = ?", playerid);
        System.out.println("\tDeleted jail packet");
        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?", playerid);
        System.out.println("\tDeleted key bindings");
        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?", playerid);
        System.out.println("\tDeleted saved locations");
        MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?", playerid);
        System.out.println("\tDeleted skill packet");
    }

    public static void deleteWhereCharacterId(Connection con, String sql, int cid) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cid);
            ps.executeUpdate();
        }
    }

    public static String getAriantRoomLeaderName(int room) {
        return ariantroomleader[room];
    }

    public static int getAriantSlotsRoom(int room) {
        return ariantroomslot[room];
    }

    public static Map<String, String> getCharacterFromDatabase(String name) {
        Map<String, String> character = new LinkedHashMap<>();

        try (Connection con = Server.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT `id`, `accountid`, `name` FROM `characters` WHERE `name` = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        character.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
                    }
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return character;
    }

    public static int getIdByName(String name) {
        try (Connection con = Server.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id FROM characters WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Unable to get ID of player '{}'", name);
        }
        return -1;
    }

    public static String getNameById(int id) {
        try (Connection con = Server.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Unable to get username of player {}", id);
        }
        return null;
    }

    public static MapleCharacter loadCharFromDB(Connection con, int charid, MapleClient client, boolean channelserver) throws SQLException {
        MapleCharacter ret = new MapleCharacter();
        ret.client = client;
        ret.id = charid;

        int partyid;
        int messengerid;
        int position;

        int mountexp;
        int mountlevel;
        int mounttiredness;

        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?")) {
            ps.setInt(1, charid);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Loading char failed (not found)");
                }
                ret.name = rs.getString("name");
                ret.level = rs.getInt("level");
                ret.fame = rs.getInt("fame");
                ret.str = rs.getInt("str");
                ret.dex = rs.getInt("dex");
                ret.int_ = rs.getInt("int");
                ret.luk = rs.getInt("luk");
                ret.exp.set(rs.getInt("exp"));
                ret.gachaexp.set(rs.getInt("gachaexp"));
                ret.hp = rs.getInt("hp");
                ret.maxhp = rs.getInt("maxhp");
                ret.mp = rs.getInt("mp");
                ret.maxmp = rs.getInt("maxmp");
                ret.hpMpApUsed = rs.getInt("hpMpUsed");
                ret.hasMerchant = rs.getInt("HasMerchant") == 1;
                String[] skillPoints = rs.getString("sp").split(",");
                for (int i = 0; i < ret.remainingSp.length; i++) {
                    ret.remainingSp[i] = Integer.parseInt(skillPoints[i]);
                }
                messengerid = rs.getInt("messengerid");
                position = rs.getInt("messengerposition");
                partyid = rs.getInt("party");

                ret.remainingAp = rs.getInt("ap");
                ret.meso.set(rs.getInt("meso"));
                ret.merchantmeso = rs.getInt("MerchantMesos");
                ret.gmLevel = rs.getInt("gm");
                ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
                ret.gender = rs.getInt("gender");
                ret.job = MapleJob.getById(rs.getInt("job"));
                ret.finishedDojoTutorial = rs.getInt("finishedDojoTutorial") == 1;
                ret.vanquisherKills = rs.getInt("vanquisherKills");
                ret.omokwins = rs.getInt("omokwins");
                ret.omoklosses = rs.getInt("omoklosses");
                ret.omokties = rs.getInt("omokties");
                ret.matchcardwins = rs.getInt("matchcardwins");
                ret.matchcardlosses = rs.getInt("matchcardlosses");
                ret.matchcardties = rs.getInt("matchcardties");
                ret.hair = rs.getInt("hair");
                ret.face = rs.getInt("face");
                ret.accountid = rs.getInt("accountid");
                ret.mapid = rs.getInt("map");
                ret.initialSpawnPoint = rs.getInt("spawnpoint");
                ret.world = rs.getByte("world");
                ret.rank = rs.getInt("rank");
                ret.rankMove = rs.getInt("rankMove");
                ret.jobRank = rs.getInt("jobRank");
                ret.jobRankMove = rs.getInt("jobRankMove");
                mountexp = rs.getInt("mountexp");
                mountlevel = rs.getInt("mountlevel");
                mounttiredness = rs.getInt("mounttiredness");
                ret.guildid = rs.getInt("guildid");
                ret.guildrank = rs.getInt("guildrank");
                ret.allianceRank = rs.getInt("allianceRank");
                ret.familyId = rs.getInt("familyId");
                ret.bookCover = rs.getInt("monsterbookcover");
                ret.monsterbook = new MonsterBook();
                ret.monsterbook.loadCards(charid);
                ret.vanquisherStage = rs.getInt("vanquisherStage");
                ret.dojoPoints = rs.getInt("dojoPoints");
                ret.dojoStage = rs.getInt("lastDojoStage");
                ret.dataString = rs.getString("dataString");
                if (channelserver) {
                    ret.createDate = rs.getTimestamp("createdate").getTime();
                    ret.fishingPoints = rs.getInt("fishingpoints");
                    ret.daily = rs.getTimestamp("daily");
                    ret.rebirths = rs.getInt("reborns");
                    ret.rebirthPoints = rs.getInt("rebirthpoints");
                    ret.eventPoints = rs.getInt("eventpoints");
                    ret.jumpQuestPoints = rs.getInt("jumpquestpoints");
                    ret.chatType = ChatType.values()[rs.getInt("chattype")];
                    int oOrdinal = rs.getInt("occupation");
                    if (oOrdinal > -1) {
                        ret.occupation = new Occupation(Occupation.Type.fromValue(oOrdinal));
                    }
                }
                if (ret.guildid > 0) {
                    ret.mgc = new MapleGuildCharacter(ret);
                }
                int buddyCapacity = rs.getInt("buddyCapacity");
                ret.buddylist = new BuddyList(buddyCapacity);
                ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equipslots"));
                ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("useslots"));
                ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setupslots"));
                ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etcslots"));
                for (Pair<Item, MapleInventoryType> item : ItemFactory.INVENTORY.loadItems(con, ret.id, !channelserver)) {
                    ret.getInventory(item.getRight()).addFromDB(item.getLeft());
                    Item itemz = item.getLeft();
                    if (itemz.getPetId() > -1) {
                        MaplePet pet = itemz.getPet();
                        if (pet != null && pet.isSummoned()) {
                            ret.addPet(pet);
                        }
                        continue;
                    }
                    if (item.getRight() == MapleInventoryType.EQUIP || item.getRight() == MapleInventoryType.EQUIPPED) {
                        Equip equip = (Equip) item.getLeft();
                        if (equip.getRingId() > -1) {
                            MapleRing ring = MapleRing.loadFromDb(equip.getRingId());
                            if (ring != null) {
                                if (item.getRight() == MapleInventoryType.EQUIPPED) {
                                    ring.equip();
                                }
                                if (ring.getItemId() >= 1112803 && ring.getItemId() <= 1112809) {
                                    ret.setMarriageRing(ring);
                                } else if (ring.getItemId() > 1112015 || ring.getItemId() == 1049000) {
                                    ret.addFriendshipRing(ring);
                                } else {
                                    ret.addCrushRing(ring);
                                }
                            } else {
                                LOGGER.warn("'{}' loaded invalid ring item({}) ringID({})", ret.name, equip.getItemId(), equip.getRingId());
                            }
                        }
                    }
                }
            }
        }
        if (channelserver) {
            ret.map = client.getChannelServer().getMap(ret.mapid);
            if (ret.map == null) {
                LOGGER.info("'{}' logged-in to an invalid map {}", ret.name, ret.mapid);
                ret.map = client.getChannelServer().getMap(ServerConstants.HOME_MAP);
                ret.dropMessage(5, "You were returned to the home map due to the map being obstructed");
            }
            MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
            if (portal == null) {
                portal = ret.map.getPortal(0);
                if (portal == null) {
                    LOGGER.info("'{}' logged-in to a map with no portals", ret.name);
                    ret.map = client.getChannelServer().getMap(ServerConstants.HOME_MAP);
                    portal = ret.map.getPortal(0);
                    ret.dropMessage(5, "You were returned to the home map due to the map being obstructed");
                }
                ret.initialSpawnPoint = 0;
            }
            ret.setPosition(portal.getPosition());
            MapleParty party = Server.getWorld(ret.world).getParty(partyid);
            if (party != null) {
                ret.mpc = new MaplePartyCharacter(ret);
                party.updateMember(ret.mpc);
                ret.party = party;
            }
            if (messengerid > 0 && position < 4 && position > -1) {
                MapleMessenger messenger = Server.getWorld(ret.world).getMessenger(messengerid);
                if (messenger != null) {
                    ret.messenger = messenger;
                    ret.messengerposition = position;
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT mapid,vip FROM trocklocations WHERE characterid = ? LIMIT 15")) {
                ps.setInt(1, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    byte v = 0;
                    byte r = 0;
                    while (rs.next()) {
                        if (rs.getInt("vip") == 1) {
                            ret.viptrockmaps.add(rs.getInt("mapid"));
                            v++;
                        } else {
                            ret.trockmaps.add(rs.getInt("mapid"));
                            r++;
                        }
                    }
                    while (v < 10) {
                        ret.viptrockmaps.add(999999999);
                        v++;
                    }
                    while (r < 5) {
                        ret.trockmaps.add(999999999);
                        r++;
                    }
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM marriages WHERE groom = ? OR bride = ?")) {
            ps.setInt(1, ret.id);
            ps.setInt(2, ret.id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret.relationship.load(con, rs.getInt("id"));
                    if (ret.getMarriageRing() == null) {
                        ret.relationship.setStatus(Relationship.Status.Single);
                    }
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT name FROM accounts WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ret.accountid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret.getClient().setAccountName(rs.getString("name"));
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT `area`,`info` FROM area_info WHERE charid = ?")) {
            ps.setInt(1, ret.id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ret.area_info.put(rs.getShort("area"), rs.getString("info"));
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT name, level FROM characters WHERE accountid = ? AND id != ? ORDER BY level DESC LIMIT 1")) {
            ps.setInt(1, ret.accountid);
            ps.setInt(2, charid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret.linkedName = rs.getString("name");
                    ret.linkedLevel = rs.getInt("level");
                }
            }
        }
        if (channelserver) {
            ret.cashshop = new CashShop(ret.accountid, ret.id, ret.getJobType());
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM achievements WHERE player_id = ?")) {
                ps.setInt(1, ret.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Achievement achievement = ret.getAchievement(rs.getString("achievement_name"));
                        achievement.setCompleted(rs.getInt("completed") == 1);
                        achievement.setMonstersKilled(rs.getInt("killed_monster"));
                        achievement.setCasino1Completed(rs.getInt("casino_one") == 1);
                        achievement.setCasino1Completed(rs.getInt("casino_two") == 1);
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?")) {
                ps.setInt(1, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    try (PreparedStatement pse = con.prepareStatement("SELECT * FROM questprogress WHERE queststatusid = ?")) {
                        try (PreparedStatement psf = con.prepareStatement("SELECT mapid FROM medalmaps WHERE queststatusid = ?")) {
                            while (rs.next()) {
                                MapleQuest q = MapleQuest.getInstance(rs.getShort("quest"));
                                MapleQuestStatus status = new MapleQuestStatus(q, MapleQuestStatus.Status.getById(rs.getInt("status")));
                                long cTime = rs.getLong("time");
                                if (cTime > -1) {
                                    status.setCompletionTime(cTime * 1000);
                                }
                                status.setForfeited(rs.getInt("forfeited"));
                                ret.quests.put(q.getId(), status);
                                pse.setInt(1, rs.getInt("queststatusid"));
                                try (ResultSet rsProgress = pse.executeQuery()) {
                                    while (rsProgress.next()) {
                                        status.setProgress(rsProgress.getInt("progressid"), rsProgress.getString("progress"));
                                    }
                                }
                                psf.setInt(1, rs.getInt("queststatusid"));
                                try (ResultSet medalmaps = psf.executeQuery()) {
                                    while (medalmaps.next()) {
                                        status.addMedalMap(medalmaps.getInt("mapid"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cquest WHERE characterid = ?")) {
                ps.setInt(1, ret.id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        CQuestData cQuest = CQuestBuilder.beginQuest(ret, rs.getInt("questid"), true);
                        if (cQuest != null) {
                            if (rs.getInt("completed") == 0) {
                                try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM cquestdata WHERE qtableid = ?")) {
                                    stmt.setInt(1, rs.getInt("id"));
                                    try (ResultSet res = stmt.executeQuery()) {
                                        while (res.next()) {
                                            cQuest.getToKill().incrementRequirement(res.getInt("monsterid"), res.getInt("kills"));
                                        }
                                    }
                                }
                            } else {
                                cQuest.setCompleted(true);
                                cQuest.setCompletion(rs.getLong("completion"));
                            }
                        }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM skills WHERE characterid = ?")) {
                ps.setInt(1, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int sid = rs.getInt("skillid");
                        Skill s = SkillFactory.getSkill(sid);
                        if (s == null) {
                            LOGGER.warn("Invalid skill {} for player {}", sid, ret.name);
                            continue;
                        }
                        SkillEntry sentry = new SkillEntry(rs.getByte("skilllevel"), rs.getInt("masterlevel"), rs.getLong("expiration"));
                        ret.skills.put(s, sentry);
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM cooldowns WHERE charid = ?")) {
                ps.setInt(1, ret.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final int skillid = rs.getInt("SkillID");
                        final long length = rs.getLong("length"), startTime = rs.getLong("StartTime");
                        if (skillid != 5221999 && (length + startTime < System.currentTimeMillis())) {
                            continue;
                        }
                        ret.giveCoolDowns(skillid, startTime, length);
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM cooldowns WHERE charid = ?")) {
                ps.setInt(1, ret.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?")) {
                ps.setInt(1, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int index = rs.getInt("position");
                        SkillMacro macro = new SkillMacro(
                                rs.getInt("skill1"),
                                rs.getInt("skill2"),
                                rs.getInt("skill3"),
                                rs.getString("name"),
                                rs.getInt("shout"), index);
                        ret.skillMacros[index] = macro;
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?")) {
                ps.setInt(1, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int key = rs.getInt("key");
                        int type = rs.getInt("type");
                        int action = rs.getInt("action");
                        ret.keymap.put(key, new MapleKeyBinding(type, action));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT `locationtype`,`map`,`portal` FROM savedlocations WHERE characterid = ?")) {
                ps.setInt(1, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ret.savedLocations[SavedLocationType.valueOf(rs.getString("locationtype")).ordinal()] = new SavedLocation(rs.getInt("map"), rs.getInt("portal"));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30")) {
                ps.setInt(1, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    ret.lastfametime = 0;
                    while (rs.next()) {
                        ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                        ret.lastmonthfameids.add(rs.getInt("characterid_to"));
                    }
                }
            }
            ret.buddylist.loadFromDb(charid);
            ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid, ret.world);
            ret.recalcLocalStats();
            // ret.resetBattleshipHp();
            ret.silentEnforceMaxHpMp();
            int mountid = ret.getJobType() * 10000000 + 1004;
            if (ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18) != null) {
                ret.maplemount = new MapleMount(ret, ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18).getItemId(), mountid);
            } else {
                ret.maplemount = new MapleMount(ret, 0, mountid);
            }
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);
        }
        return ret;
    }

    public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");
        return i;

    }

    public static void removeAriantRoom(int room) {
        ariantroomleader[room] = "";
        ariantroomslot[room] = 0;
    }

    public static void changeRow(String player, String row, String type, Object newval) {
        try (Connection c = Server.getConnection(); PreparedStatement statement = c.prepareStatement("UPDATE characters SET ? = ? WHERE name = ?")) {
            statement.setString(1, row);
            switch (type.toLowerCase()) {
                case "int":
                    statement.setInt(2, (int) newval);
                    break;
                case "string":
                    statement.setString(2, (String) newval);
                    break;
            }
            statement.setString(3, player);

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void setAriantRoomLeader(int room, String charname) {
        ariantroomleader[room] = charname;
    }

    public static void setAriantSlotRoom(int room, int slot) {
        ariantroomslot[room] = slot;
    }

    public static boolean wipe(String playerName) {
        int playerId = MapleCharacter.getIdByName(playerName);
        try (Connection con = Server.getConnection()) {
            Database.executeSingle(con, "delete from inventoryitems where characterid = ?", playerId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addCooldown(int skillId, long startTime, long length, Task task) {
        coolDowns.put(skillId, new MapleCoolDownValueHolder(skillId, startTime, length, task));
    }

    public void addCrushRing(MapleRing r) {
        crushRings.add(r);
    }

    public MapleRing getRingById(int id) {
        for (MapleRing ring : getCrushRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }
        for (MapleRing ring : getFriendshipRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }
        if (getMarriageRing() != null && getMarriageRing().getRingId() == id) {
            return getMarriageRing();
        }

        return null;
    }

    public int addDojoPointsByMap() {
        int pts = 0;
        if (dojoPoints < 17000) {
            pts = 1 + ((getMap().getId() - 1) / 100 % 100) / 6;
            if (!dojoParty) {
                pts++;
            }
            this.dojoPoints += pts;
        }
        return pts;
    }

    public void addDoor(MapleDoor door) {
        doors.add(door);
    }

    public void addExcluded(int x) {
        excluded.add(x);
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void addFriendshipRing(MapleRing r) {
        friendshipRings.add(r);
    }

    public void addHP(int delta) {
        setHp(hp + delta);
        updateSingleStat(MapleStat.HP, hp);
    }

    public void addMesosTraded(int gain) {
        this.mesosTraded += gain;
    }

    public void addMP(int delta) {
        setMp(mp + delta);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        setHp(hp + hpDiff);
        setMp(mp + mpDiff);
        updateSingleStat(MapleStat.HP, getHp());
        updateSingleStat(MapleStat.MP, getMp());
    }

    public void addPet(MaplePet pet) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                pets[i] = pet;
                return;
            }
        }
    }

    public void addStat(int type, int up) {
        if (type == 1) {
            this.str += up;
            updateSingleStat(MapleStat.STR, str);
        } else if (type == 2) {
            this.dex += up;
            updateSingleStat(MapleStat.DEX, dex);
        } else if (type == 3) {
            this.int_ += up;
            updateSingleStat(MapleStat.INT, int_);
        } else if (type == 4) {
            this.luk += up;
            updateSingleStat(MapleStat.LUK, luk);
        }
        recalcLocalStats();
    }

    public int addHP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleJob jobtype = player.getJob();
        int MaxHP = player.getMaxHp();
        if (player.getHpMpApUsed() > 9999 || MaxHP >= 30000) {
            return MaxHP;
        }
        if (jobtype.isA(MapleJob.BEGINNER)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.WARRIOR) || jobtype.isA(MapleJob.DAWNWARRIOR1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(10000000) : SkillFactory.getSkill(1000001)) > 0) {
                MaxHP += 20;
            } else {
                MaxHP += 8;
            }
        } else if (jobtype.isA(MapleJob.MAGICIAN) || jobtype.isA(MapleJob.BLAZEWIZARD1)) {
            MaxHP += 6;
        } else if (jobtype.isA(MapleJob.BOWMAN) || jobtype.isA(MapleJob.WINDARCHER1)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.THIEF) || jobtype.isA(MapleJob.NIGHTWALKER1)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.PIRATE) || jobtype.isA(MapleJob.THUNDERBREAKER1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(15100000) : SkillFactory.getSkill(5100000)) > 0) {
                MaxHP += 18;
            } else {
                MaxHP += 8;
            }
        }
        return MaxHP;
    }

    public int addMP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        int MaxMP = player.getMaxMp();
        if (player.getHpMpApUsed() > 9999 || player.getMaxMp() >= 30000) {
            return MaxMP;
        }
        if (player.getJob().isA(MapleJob.BEGINNER) || player.getJob().isA(MapleJob.NOBLESSE) || player.getJob().isA(MapleJob.LEGEND)) {
            MaxMP += 6;
        } else if (player.getJob().isA(MapleJob.WARRIOR) || player.getJob().isA(MapleJob.DAWNWARRIOR1) || player.getJob().isA(MapleJob.ARAN1)) {
            MaxMP += 2;
        } else if (player.getJob().isA(MapleJob.MAGICIAN) || player.getJob().isA(MapleJob.BLAZEWIZARD1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(12000000) : SkillFactory.getSkill(2000001)) > 0) {
                MaxMP += 18;
            } else {
                MaxMP += 14;
            }

        } else if (player.getJob().isA(MapleJob.BOWMAN) || player.getJob().isA(MapleJob.THIEF)) {
            MaxMP += 10;
        } else if (player.getJob().isA(MapleJob.PIRATE)) {
            MaxMP += 14;
        }

        return MaxMP;
    }

    public void addSummon(int id, MapleSummon summon) {
        summons.put(id, summon);
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.putIfAbsent(mo.getObjectId(), mo);
    }

    public int calculateMaxBaseDamage(int watk) {
        int maxbasedamage;
        Item weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
        if (weapon_item != null) {
            MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
            int mainstat;
            int secondarystat;
            if (getJob().isA(MapleJob.THIEF) && weapon == MapleWeaponType.DAGGER_OTHER) {
                weapon = MapleWeaponType.DAGGER_THIEVES;
            }

            if (weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW || weapon == MapleWeaponType.GUN) {
                mainstat = localdex;
                secondarystat = localstr;
            } else if (weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER_THIEVES) {
                mainstat = localluk;
                secondarystat = localdex + localstr;
            } else {
                mainstat = localstr;
                secondarystat = localdex;
            }
            maxbasedamage = (int) (((weapon.getMaxDamageMultiplier() * mainstat + secondarystat) / 100.0) * watk);
        } else {
            if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
                double weapMulti = 3;
                if (job.getId() % 100 != 0) {
                    weapMulti = 4.2;
                }

                int attack = (int) Math.min(Math.floor((2 * getLevel() + 31) / 3), 31);

                maxbasedamage = (int) (localstr * weapMulti + localdex) * attack / 100;
            } else {
                maxbasedamage = 1;
            }
        }
        return Math.max(1, maxbasedamage);
    }

    public void cancelAllBuffs(boolean disconnect) {
        if (disconnect) {
            effects.clear();
        } else {
            for (MapleBuffStatValueHolder mbsvh : new ArrayList<>(effects.values())) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void cancelBuffStats(MapleBuffStat stat) {
        List<MapleBuffStat> buffStatList = Collections.singletonList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList);
    }

    public void maxSkills() {
        for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
            try {
                Skill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                if (skill != null && ((skill.getId() / 100) != 9 || isGM())) {
                    changeSkillLevel(skill, (byte) skill.getMaxLevel(), skill.getMaxLevel(), -1);
                }
            } catch (NumberFormatException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public short getCombo() {
        return combocounter;
    }

    public void setCombo(short count) {
        if (count < combocounter) {
            cancelEffectFromBuffStat(MapleBuffStat.ARAN_COMBO);
        }
        combocounter = (short) Math.min(30000, count);
        if (count > 0) {
            announce(MaplePacketCreator.showCombo(combocounter));
        }
    }

    public long getCreateDate() {
        return createDate;
    }

    public long getLastCombo() {
        return lastcombo;
    }

    public void setLastCombo(long time) {
        lastcombo = time;
    }

    public int getLastMobCount() { // Used for skills that have mobCount at 1.
        // (a/b)
        return lastmobcount;
    }

    public void setLastMobCount(byte count) {
        lastmobcount = count;
    }

    /**
     * Applies information from the old client to the new session client before
     * assigning the actual client object to the existing player
     *
     * @param client new client to assign to the player
     */
    public void newClient(MapleClient client) {
        client.setAccountName(this.client.getAccountName());
        this.client = client;

        map = this.client.getChannelServer().getMap(getMapId());
        MaplePortal portal = map.findClosestSpawnpoint(getPosition());
        if (portal == null) {
            portal = map.getPortal(0);
        }
        this.setPosition(portal.getPosition());
        this.initialSpawnPoint = portal.getId();
    }

    public void cancelBuffEffects() {
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            mbsvh.task.cancel();
            mbsvh.task = null;
        }
        effects.clear();
        effects = new EnumMap<>(MapleBuffStat.class);
    }

    public String getMedalText() {
        String medal = "";
        final Item medalItem = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
        if (medalItem != null) {
            medal = "<" + MapleItemInformationProvider.getInstance().getName(medalItem.getItemId()) + "> ";
        }
        return medal;
    }

    public void cancelEffect(int itemId) {
        cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(itemId), false, -1);
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        List<MapleBuffStat> buffstats;
        if (!overwrite) {
            buffstats = getBuffStats(effect, startTime);
        } else {
            List<Pair<MapleBuffStat, Integer>> statups = effect.getStatups();
            buffstats = new ArrayList<>(statups.size());
            for (Pair<MapleBuffStat, Integer> statup : statups) {
                buffstats.add(statup.getLeft());
            }
        }
        deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
            if (!getDoors().isEmpty()) {
                MapleDoor door = getDoors().iterator().next();
                for (MapleCharacter chr : door.getTarget().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleCharacter chr : door.getTown().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleDoor destroyDoor : getDoors()) {
                    door.getTarget().removeMapObject(destroyDoor);
                    door.getTown().removeMapObject(destroyDoor);
                }
                if (party != null) {
                    for (MaplePartyCharacter partyMembers : getParty().getMembers()) {
                        partyMembers.getPlayer().getDoors().remove(door);
                        partyMembers.getDoors().remove(door);
                    }
                    silentPartyUpdate();
                } else {
                    clearDoors();
                }
            }
        }
        if (effect.getSourceId() == Spearman.HYPER_BODY || effect.getSourceId() == SuperGM.HYPER_BODY) {
            List<Pair<MapleStat, Integer>> statup = new ArrayList<>(4);
            statup.add(new Pair<>(MapleStat.HP, Math.min(hp, maxhp)));
            statup.add(new Pair<>(MapleStat.MP, Math.min(mp, maxmp)));
            statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
            statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
            client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
        }
        if (effect.isMonsterRiding()) {
            if (effect.getSourceId() != Corsair.BATTLESHIP) {
                getMount().cancelSchedule();
                getMount().setActive(false);
            }
        }
        if (!overwrite) {
            cancelPlayerBuffs(buffstats);
        }
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        MapleBuffStatValueHolder effect = effects.get(stat);
        if (effect != null) {
            cancelEffect(effect.effect, false, -1);
        }
    }

    public void setHide(boolean hide) {
        this.hidden = hide;
    }

    public void setHidden(boolean hidden, boolean login) {
        if (isGM()) {
            setHide(hidden);
            announce(MaplePacketCreator.getGMEffect(0x10, (byte) (hidden ? 1 : 0)));
            if (!hidden) {
                List<MapleBuffStat> dsstat = Collections.singletonList(MapleBuffStat.DARKSIGHT);
                getMap().broadcastGMMessage(this, MaplePacketCreator.cancelForeignBuff(id, dsstat), false);
                getMap().broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                updatePartyMemberHP();
                getMap().getMonsters().forEach(getMap()::updateMonsterController);
                if (getFakePlayer() != null) {
                    getMap().addFakePlayer(getFakePlayer());
                }
            } else {
                if (!login) {
                    getMap().broadcastMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
                }
                List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<>(MapleBuffStat.DARKSIGHT, 0));
                getMap().broadcastGMMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                getMap().broadcastGMMessage(this, MaplePacketCreator.giveForeignBuff(id, dsstat), false);
                for (MapleMonster mon : getControlledMonsters()) {
                    mon.setController(null);
                    mon.setControllerHasAggro(false);
                    mon.setControllerKnowsAboutAggro(false);
                    mon.getMap().updateMonsterController(mon);
                }
                if (getFakePlayer() != null) {
                    getMap().removeFakePlayer(getFakePlayer());
                }
            }
            announce(MaplePacketCreator.enableActions());
        }
    }

    public void toggleHide() {
        setHidden(!isHidden(), false);
    }

    private void cancelFullnessSchedule(int petSlot) {
        if (fullnessSchedule[petSlot] != null) {
            fullnessSchedule[petSlot].cancel();
        }
    }

    public void cancelMagicDoor() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<>(effects.values())) {
            if (mbsvh.effect.isMagicDoor()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        if (client.getChannelServer().getPlayerStorage().get(getId()) != null) {
            recalcLocalStats();
            enforceMaxHpMp();
            client.announce(MaplePacketCreator.cancelBuff(buffstats));
            if (buffstats.size() > 0) {
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
            }
        }
    }

    public boolean canDoor() {
        return canDoor;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (gmLevel > 0) {
            return FameStatus.OK;
        } else if (lastfametime >= System.currentTimeMillis() - 3600000 * 24) {
            return FameStatus.NOT_TODAY;
        } else if (lastmonthfameids.contains(from.getId())) {
            return FameStatus.NOT_THIS_MONTH;
        } else {
            return FameStatus.OK;
        }
    }

    public void changeCI(int type) {
        this.ci = type;
    }

    public void setMasteries(int jobId) {
        List<Pair<Integer, Integer>> skills;
        switch (jobId) {
            default:
                return;
            case 112:
                skills = Arrays.asList(
                        new Pair<>(Hero.ACHILLES, 10),
                        new Pair<>(Hero.MONSTER_MAGNET, 10),
                        new Pair<>(Hero.BRANDISH, 10));
                break;
            case 132:
                skills = Arrays.asList(
                        new Pair<>(DarkKnight.BEHOLDER, 10),
                        new Pair<>(DarkKnight.ACHILLES, 10),
                        new Pair<>(DarkKnight.MONSTER_MAGNET, 10));
                break;
            case 212:
                skills = Arrays.asList(
                        new Pair<>(FPArchMage.BIG_BANG, 10),
                        new Pair<>(FPArchMage.MANA_REFLECTION, 10),
                        new Pair<>(FPArchMage.PARALYZE, 10));
                break;
            case 222:
                skills = Arrays.asList(
                        new Pair<>(ILArchMage.BIG_BANG, 10),
                        new Pair<>(ILArchMage.MANA_REFLECTION, 10),
                        new Pair<>(ILArchMage.CHAIN_LIGHTNING, 10));
                break;
            case 232:
                skills = Arrays.asList(
                        new Pair<>(Bishop.BIG_BANG, 10),
                        new Pair<>(Bishop.MANA_REFLECTION, 10),
                        new Pair<>(Bishop.HOLY_SHIELD, 10));
                break;
            case 312:
                skills = Arrays.asList(
                        new Pair<>(Bowmaster.BOW_EXPERT, 10),
                        new Pair<>(Bowmaster.HAMSTRING, 10),
                        new Pair<>(Bowmaster.SHARP_EYES, 10));
                break;
            case 322:
                skills = Arrays.asList(
                        new Pair<>(Marksman.MARKSMAN_BOOST, 10),
                        new Pair<>(Marksman.BLIND, 10),
                        new Pair<>(Marksman.SHARP_EYES, 10));
                break;
            case 412:
                skills = Arrays.asList(
                        new Pair<>(NightLord.SHADOW_STARS, 10),
                        new Pair<>(NightLord.SHADOW_SHIFTER, 10),
                        new Pair<>(NightLord.VENOMOUS_STAR, 10));
                break;
            case 422:
                skills = Arrays.asList(
                        new Pair<>(Shadower.SHADOW_SHIFTER, 10),
                        new Pair<>(Shadower.VENOMOUS_STAB, 10),
                        new Pair<>(Shadower.BOOMERANG_STEP, 10));
                break;
            case 512:
                skills = Arrays.asList(
                        new Pair<>(Buccaneer.BARRAGE, 10),
                        new Pair<>(Buccaneer.ENERGY_ORB, 10),
                        new Pair<>(Buccaneer.SPEED_INFUSION, 10),
                        new Pair<>(Buccaneer.DRAGON_STRIKE, 10));
                break;
            case 522:
                skills = Arrays.asList(
                        new Pair<>(Corsair.ELEMENTAL_BOOST, 10),
                        new Pair<>(Corsair.BULLSEYE, 10),
                        new Pair<>(Corsair.WRATH_OF_THE_OCTOPI, 10),
                        new Pair<>(Corsair.RAPID_FIRE, 10));
                break;
            case 2112:
                skills = Arrays.asList(
                        new Pair<>(Aran.OVER_SWING, 10),
                        new Pair<>(Aran.HIGH_MASTERY, 10),
                        new Pair<>(Aran.FREEZE_STANDING, 10));
                break;
            case 2217:
                skills = Arrays.asList(
                        new Pair<>(Evan.MAPLE_WARRIOR, 10),
                        new Pair<>(Evan.ILLUSION, 10));
                break;
            case 2218:
                skills = Arrays.asList(
                        new Pair<>(Evan.BLESSING_OF_THE_ONYX, 10),
                        new Pair<>(Evan.BLAZE, 10));
                break;
        }
        for (Pair<Integer, Integer> pair : skills) {
            if (pair.getLeft() != 0) {
                Skill skill = SkillFactory.getSkill(pair.getLeft());
                if (skill != null) {
                    changeSkillLevel(skill, (byte) 0, pair.getRight(), -1);
                } else {
                    LOGGER.info("Unable to set skill mastery {} for player {}. Does not exist", pair.getLeft(), getName());
                }
            }
        }
    }

    public void changeJob(MapleJob newJob) {
        if (newJob == null) {
            return;// the fuck you doing idiot!
        }
        this.job = newJob;
        int nGainSkillPoints = 1;
        if (GameConstants.hasSPTable(newJob) || newJob.getId() % 10 == 2) {
            nGainSkillPoints = 2;
        }
        gainSp(nGainSkillPoints);

        if (newJob.getId() % 10 > 1) {
            gainAp(5);
        }
        int job_ = job.getId() % 1000; // lame temp "fix"
        if (job_ == 100) {
            maxhp += Randomizer.rand(200, 250);
        } else if (job_ == 200) {
            maxmp += Randomizer.rand(100, 150);
        } else if (job_ % 100 == 0) {
            maxhp += Randomizer.rand(100, 150);
            maxhp += Randomizer.rand(25, 50);
        } else if (job_ > 0 && job_ < 200) {
            maxhp += Randomizer.rand(300, 350);
        } else if (job_ < 300) {
            maxmp += Randomizer.rand(450, 500);
        } else {
            maxhp += Randomizer.rand(300, 350);
            maxmp += Randomizer.rand(150, 200);
        }
        if (maxhp >= 30000) {
            maxhp = 30000;
        }
        if (maxmp >= 30000) {
            maxmp = 30000;
        }
        if (!isGM()) {
            for (byte i = 1; i < 5; i++) {
                gainSlots(i, 4, true);
            }
        }
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(5);
        statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
        statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<>(MapleStat.AVAILABLESP, getRemainingSp()));
        statup.add(new Pair<>(MapleStat.JOB, job.getId()));
        if (dragon != null) {
            getMap().broadcastMessage(MaplePacketCreator.removeDragon(dragon.getObjectId()));
            dragon = null;
        }
        recalcLocalStats();
        client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
        silentPartyUpdate();
        if (this.guildid > 0) {
            getGuild().broadcast(MaplePacketCreator.jobMessage(0, job.getId(), name), this.getId());
        }
        setMasteries(this.job.getId());
        guildUpdate();
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 8), false);
        if (GameConstants.hasSPTable(newJob) && newJob.getId() != 2001) {
            if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
                cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
            }
            createDragon();
        }
    }

    public void changeKeybinding(int key, MapleKeyBinding keybinding) {
        if (keybinding.getType() != 0) {
            keymap.put(key, keybinding);
        } else {
            keymap.remove(key);
        }
    }

    public void changeMap(int map) {
        changeMap(map, 0);
    }

    public void changeMap(int map, int portal) {
        MapleMap dest = client.getChannelServer().getMap(map);
        changeMap(dest, dest.getPortal(portal));
    }

    public void changeMap(int map, String portal) {
        MapleMap dest = client.getChannelServer().getMap(map);
        changeMap(dest, dest.getPortal(portal));
    }

    public void changeMap(int map, MaplePortal portal) {
        changeMap(client.getChannelServer().getMap(map), portal);
    }

    public void changeMap(MapleMap to) {
        changeMap(to, to.getPortal(0));
    }

    public void changeMap(final MapleMap to, final MaplePortal pto) {
        changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to.getId(), pto.getId(), this, null));
    }

    public void changeMap(final MapleMap to, final Point pos) {
        changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to.getId(), 0x80, this, pos));
    }

    public void changeMapBanish(int mapid, String portal, String msg) {
        List<GenericEvent> gEvents = getGenericEvents();
        for (GenericEvent generic : gEvents) {
            if (!generic.banishPlayer(this, mapid)) {
                return;
            }
        }
        dropMessage(5, msg);
        MapleMap map_ = client.getChannelServer().getMap(mapid);
        changeMap(map_, map_.getPortal(portal));
    }

    private void changeMapInternal(final MapleMap to, final Point pos, final byte[] warpPacket) {
        if (getTrade() != null) {
            MapleTrade.cancelTrade(this);
        }

        if (getArcade() != null && to.getId() != getArcade().getMapId()) {
            getArcade().fail();
        }
        ManualPlayerEvent playerEvent = client.getWorldServer().getPlayerEvent();
        if (playerEvent != null) {
            if (to != playerEvent.getMap()) {
                ManualPlayerEvent.Participant remove = playerEvent.participants.remove(getId());
                if (remove != null) {
                    dispelDebuffs();
                    setMuted(false);
                    sendMessage(5, "You have been returned to your original map.");
                    changeMap(client.getChannelServer().getMap(remove.returnMapId));
                    return;
                }
            }
        }
        List<GenericEvent> gEvents = getGenericEvents();
        if (!gEvents.isEmpty()) {
            for (GenericEvent gEvent : gEvents) {
                if (!gEvent.onPlayerChangeMapInternal(this, to)) {
                    return;
                }
            }
        }
        if (getEventInstance() != null) {
            if (!eventInstance.movePlayer(this, to)) {
                return;
            }
        }
        if (getFakePlayer() != null) {
            map.removeFakePlayer(getFakePlayer());
        }

        client.announce(warpPacket);
        map.removePlayer(this);
        if (client.getChannelServer().getPlayerStorage().get(getId()) != null) {
            map = to;
            setPosition(pos);
            if (getFakePlayer() != null) {
                getFakePlayer().setMap(map);
                getFakePlayer().setPosition(pos.getLocation());
                map.addFakePlayer(getFakePlayer());
            }
            map.addPlayer(this);
            if (party != null) {
                mpc.setMapId(to.getId());
                silentPartyUpdate();
                client.announce(MaplePacketCreator.updateParty(client.getChannel(), party, PartyOperation.SILENT_UPDATE, null));
                updatePartyMemberHP();
            }
            if (getMap().getHPDec() > 0) {
                hpDecreaseTask = TaskExecutor.createTask(this::doHurtHp, 10000);
            }
        }
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void applyHiddenSkillFixes(Skill skill) {
        SkillEntry entry;
        switch (skill.getId()) {
            case Aran.FULL_SWING:
            case Aran.HIDDEN_FULL_SWING_DOUBLE:
            case Aran.HIDDEN_FULL_SWING_TRIPLE:
                entry = getSkills().get(SkillFactory.getSkill(Aran.FULL_SWING)); // one time index
                changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_FULL_SWING_DOUBLE), entry.skillevel, entry.masterlevel, entry.expiration);
                changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_FULL_SWING_TRIPLE), entry.skillevel, entry.masterlevel, entry.expiration);
                break;
            case Aran.OVER_SWING:
            case Aran.HIDDEN_OVER_SWING_DOUBLE:
            case Aran.HIDDEN_OVER_SWING_TRIPLE:
                entry = getSkills().get(SkillFactory.getSkill(Aran.OVER_SWING)); // one time index
                changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_OVER_SWING_DOUBLE), entry.skillevel, entry.masterlevel, entry.expiration);
                changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_OVER_SWING_TRIPLE), entry.skillevel, entry.masterlevel, entry.expiration);
                break;
        }
    }

    public void changeSkillLevel(Skill skill, byte newLevel, int newMasterlevel, long expiration) {
        if (newLevel > -1) {
            skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
            if (!GameConstants.isHiddenSkills(skill.getId())) {
                this.client.announce(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel, expiration));
            }
        } else {
            skills.remove(skill);
            this.client.announce(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel, -1)); // Shouldn't
            try (Connection con = client.getWorldServer().getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM skills WHERE skillid = ? AND characterid = ?")) {
                ps.setInt(1, skill.getId());
                ps.setInt(2, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                LOGGER.error("Unable to delete skill {} from player '{}':{}", skill.getId(), getName(), getId(), e);
            }
        }
    }

    public void changeTab(int tab) {
        this.currentTab = tab;
    }

    public void changeType(int type) {
        this.currentType = type;
    }

    public void checkBerserk() {
        if (berserkTask != null) {
            berserkTask.cancel();
            final MapleCharacter chr = this;
            if (job == MapleJob.DARKKNIGHT) {
                Skill BerserkX = SkillFactory.getSkill(DarkKnight.BERSERK);
                final int skilllevel = getSkillLevel(BerserkX);
                if (skilllevel > 0) {
                    berserk = chr.getHp() * 100 / chr.getMaxHp() < BerserkX.getEffect(skilllevel).getX();
                    berserkTask = TaskExecutor.createRepeatingTask(new Runnable() {
                        @Override
                        public void run() {
                            client.announce(MaplePacketCreator.showOwnBerserk(skilllevel, berserk));
                            getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBerserk(getId(), skilllevel, berserk), false);
                        }
                    }, 5000, 3000);
                }
            }
        }
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4 && messengerposition > -1) {
            MapleWorld worldz = Server.getWorld(world);
            worldz.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(this, messengerposition), messengerposition);
            worldz.updateMessenger(getMessenger().getId(), name, client.getChannel());
        }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (!monster.isControllerHasAggro()) {
            if (monster.getController() == this) {
                monster.setControllerHasAggro(true);
            } else {
                monster.switchController(this, true);
            }
        }
    }

    public void clearDoors() {
        doors.clear();
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = null;
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        monster.setController(this);
        controlled.add(monster);
        client.announce(MaplePacketCreator.controlMonster(monster, false, aggro));
    }

    public int countItem(int itemid) {
        return getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemid)).countById(itemid);
    }

    public void decreaseBattleshipHp(int decrease) {
        this.battleshipHp -= decrease;
        if (battleshipHp <= 0) {
            this.battleshipHp = 0;
            Skill battleship = SkillFactory.getSkill(Corsair.BATTLESHIP);
            int cooldown = battleship.getEffect(getSkillLevel(battleship)).getCooldown();
            announce(MaplePacketCreator.skillCooldown(Corsair.BATTLESHIP, cooldown));
            addCooldown(Corsair.BATTLESHIP, System.currentTimeMillis(), cooldown, TaskExecutor.createTask(new CancelCooldownAction(this, Corsair.BATTLESHIP), cooldown * 1000));
            removeCooldown(5221999);
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        } else {
            announce(MaplePacketCreator.skillCooldown(5221999, battleshipHp / 10)); // :D
            addCooldown(5221999, 0, battleshipHp, null);
        }
    }

    public void decreaseReports() {
        this.possibleReports--;
    }

    public void deleteGuild(int guildId) {
        try (Connection con = Server.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?")) {
                ps.setInt(1, guildId);
                ps.execute();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?")) {
                ps.setInt(1, id);
                ps.execute();
            }
        } catch (SQLException e) {
            LOGGER.error("Unable to delete guild '{}'", guildid, e);
        }
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void deregisterBuffStats(final List<MapleBuffStat> stats) {
        List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<>(stats.size());
        for (MapleBuffStat stat : stats) {
            MapleBuffStatValueHolder mbsvh = effects.get(stat);
            if (mbsvh != null) {
                effects.remove(stat);
                boolean addMbsvh = true;
                for (MapleBuffStatValueHolder contained : effectsToCancel) {
                    if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect) {
                        addMbsvh = false;
                    }
                }
                if (addMbsvh) {
                    effectsToCancel.add(mbsvh);
                }
                if (stat == MapleBuffStat.RECOVERY) {
                    recoveryTask = TaskExecutor.cancelTask(recoveryTask);
                } else if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET) {
                    int summonId = mbsvh.effect.getSourceId();
                    MapleSummon summon = summons.get(summonId);
                    if (summon != null) {
                        getMap().broadcastMessage(MaplePacketCreator.removeSummon(summon, true), summon.getPosition());
                        getMap().removeMapObject(summon);
                        removeVisibleMapObject(summon);
                        summons.remove(summonId);
                        if (summon.getSkill() == DarkKnight.BEHOLDER) {
                            beholderBuffTask = TaskExecutor.cancelTask(beholderBuffTask);
                            beholderHealingTask = TaskExecutor.cancelTask(beholderHealingTask);
                        }
                    }
                } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                    dragonBloodTask = TaskExecutor.cancelTask(dragonBloodTask);
                }
            }
        }
        for (MapleBuffStatValueHolder buffHolder : effectsToCancel) {
            if (buffHolder.task != null) {
                buffHolder.task.cancel();
                buffHolder.task = null;
                cancelEffect(buffHolder.effect, false, -1);
            }
        }
    }

    public void disableDoor() {
        canDoor = false;
        TaskExecutor.createTask(() -> canDoor = true, 5000);
    }

    public void disbandGuild() {
        if (guildid < 1 || guildrank != 1) {
            return;
        }
        Server.disbandGuild(guildid);
    }

    public void dispel() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<>(effects.values())) {
            if (mbsvh.effect.isSkill()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public final List<PlayerDiseaseValueHolder> getAllDiseases() {
        final List<PlayerDiseaseValueHolder> ret = new ArrayList<>(5);

        DiseaseValueHolder vh;
        for (Entry<MapleDisease, DiseaseValueHolder> disease : diseases.entrySet()) {
            vh = disease.getValue();
            ret.add(new PlayerDiseaseValueHolder(disease.getKey(), vh.startTime, vh.length));
        }
        return ret;
    }

    public final boolean hasDisease(final MapleDisease dis) {
        return diseases.containsKey(dis);
    }

    public void giveDebuff(final MapleDisease disease, MobSkill skill) {
        final List<Pair<MapleDisease, Integer>> debuff = Collections.singletonList(new Pair<>(disease, skill.getX()));

        if (!hasDisease(disease) && diseases.size() < 2) {
            if (!(disease == MapleDisease.SEDUCE || disease == MapleDisease.STUN)) {
                if (isActiveBuffedValue(Bishop.HOLY_SHIELD)) {
                    return;
                }
            }
            TaskExecutor.createTask(() -> dispelDebuff(disease), skill.getDuration());

            diseases.put(disease, new DiseaseValueHolder(System.currentTimeMillis(), skill.getDuration()));
            client.announce(MaplePacketCreator.giveDebuff(debuff, skill));
            map.broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(id, debuff, skill), false);
        }
    }

    public void dispelDebuff(MapleDisease debuff) {
        if (hasDisease(debuff)) {
            long mask = debuff.getValue();
            announce(MaplePacketCreator.cancelDebuff(mask));
            map.broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(id, mask), false);

            diseases.remove(debuff);
        }
    }

    public void dispelDebuffs() {
        for (MapleDisease disease : MapleDisease.values()) {
            dispelDebuff(disease);
        }
    }

    public void cancelAllDebuffs() {
        diseases.clear();
    }

    public void dispelSkill(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (skillid == 0) {
                if (mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() % 10000000 == 1004 || dispelSkills(mbsvh.effect.getSourceId()))) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            } else if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    private boolean dispelSkills(int skillid) {
        switch (skillid) {
            case DarkKnight.BEHOLDER:
            case FPArchMage.ELQUINES:
            case ILArchMage.IFRIT:
            case Priest.SUMMON_DRAGON:
            case Bishop.BAHAMUT:
            case Ranger.PUPPET:
            case Ranger.SILVER_HAWK:
            case Sniper.PUPPET:
            case Sniper.GOLDEN_EAGLE:
            case Hermit.SHADOW_PARTNER:
                return true;
            default:
                return false;
        }
    }

    public void doHurtHp() {
        if (this.getInventory(MapleInventoryType.EQUIPPED).findById(getMap().getHPDecProtect()) != null) {
            if (hpDecreaseTask != null) {
                hpDecreaseTask.cancel();
                hpDecreaseTask = null;
            }
            return;
        }
        addHP(-getMap().getHPDec());
        hpDecreaseTask = TaskExecutor.createTask(this::doHurtHp, 10000);
    }

    /**
     * @param message a message to send to the player
     * @param object  arguements that are formatted to the specified message
     */
    public void sendMessage(String message, Object... object) {
        sendMessage(6, message, object);
    }

    /**
     * fuck a method {@link String#format(String, Object...)}
     * <p>
     * Construct a formatted String using the formatter via the Logger library
     * </p>
     *
     * @param type    message color type
     * @param message a message to send to the player
     * @param object  arguements that are formatted to the specified message
     */
    public void sendMessage(int type, String message, Object... object) {
        dropMessage(type, MessageFormatter.arrayFormat(message, object).getMessage());
    }

    /**
     * @see #sendMessage(int, String, Object...)
     */
    public void sendDebugMessage(int type, String message, Object... object) {
        if (isGM()) {
            sendMessage(type, "[IMPORTANT] " + message, object);
        }
    }

    public void dropMessage(String message) {
        dropMessage(6, message);
    }

    public void dropMessage(int type, String message) {
        client.announce(MaplePacketCreator.serverNotice(type, message));
    }

    public String emblemCost() {
        return StringUtil.formatNumber(MapleGuild.CHANGE_EMBLEM_COST);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    private void enforceMaxHpMp() {
        List<Pair<MapleStat, Integer>> stats = new ArrayList<>(2);
        if (getMp() > getCurrentMaxMp()) {
            setMp(getMp());
            stats.add(new Pair<>(MapleStat.MP, getMp()));
        }
        if (getHp() > getCurrentMaxHp()) {
            setHp(getHp());
            stats.add(new Pair<>(MapleStat.HP, getHp()));
        }
        if (stats.size() > 0) {
            client.announce(MaplePacketCreator.updatePlayerStats(stats, this));
        }
    }

    public void enteredScript(String script, int mapid) {
        if (!entered.containsKey(mapid)) {
            entered.put(mapid, script);
        }
    }

    public void equipChanged() {
        getMap().broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        recalcLocalStats();
        enforceMaxHpMp();
        if (getMessenger() != null) {
            Server.getWorld(world).updateMessenger(getMessenger(), getName(), getWorld(), client.getChannel());
        }
        MapleInventory inventory = getInventory(MapleInventoryType.EQUIPPED);
        // DBZ Drop rate boost
        // Blue Eye Scanner
        // Green Eye Scanner
        // Pink EYe Scanner
        // Red Eye Scanner
        eyeScannersEquiped = inventory.findById(1022994) != null // Blue Eye Scanner
                && inventory.findById(1022995) != null // Green Eye Scanner
                && inventory.findById(1022996) != null // Pink Eye Scanner
                && inventory.findById(1022999) != null; // Red Eye Scanner
    }

    public void cancelExpirationTask() {
        if (expirationTask != null) {
            expirationTask.cancel();
            expirationTask = null;
        }
    }

    public void expirationTask() {
        if (expirationTask == null) {
            expirationTask = TaskExecutor.createRepeatingTask(new Runnable() {
                @Override
                public void run() {
                    long expiration, currenttime = System.currentTimeMillis();
                    Set<Skill> keys = getSkills().keySet();
                    for (Iterator<Skill> i = keys.iterator(); i.hasNext(); ) {
                        Skill key = i.next();
                        SkillEntry skill = getSkills().get(key);
                        if (skill.expiration != -1 && skill.expiration < currenttime) {
                            changeSkillLevel(key, (byte) -1, 0, -1);
                        }
                    }

                    List<Item> toberemove = new ArrayList<>();
                    for (MapleInventory inv : inventory) {
                        for (Item item : inv.list()) {
                            expiration = item.getExpiration();
                            if (expiration != -1 && (expiration < currenttime) && ((item.getFlag() & ItemConstants.LOCK) == ItemConstants.LOCK)) {
                                byte aids = item.getFlag();
                                aids &= ~(ItemConstants.LOCK);
                                item.setFlag(aids); // Probably need a check,
                                // else people can make
                                // expiring items into
                                // permanent items...
                                item.setExpiration(-1);
                                forceUpdateItem(item); // TEST :3
                            } else if (expiration != -1 && expiration < currenttime) {
                                client.announce(MaplePacketCreator.itemExpired(item.getItemId()));
                                toberemove.add(item);
                            }
                        }
                        for (Item item : toberemove) {
                            MapleInventoryManipulator.removeFromSlot(client, inv.getType(), item.getPosition(), item.getQuantity(), true);
                        }
                        toberemove.clear();
                    }
                }
            }, 60000);
        }
    }

    public void forceUpdateItem(Item item) {
        final List<ModifyInventory> mods = new LinkedList<>();
        mods.add(new ModifyInventory(3, item));
        mods.add(new ModifyInventory(0, item));
        client.announce(MaplePacketCreator.modifyInventory(true, mods));
    }

    public void gainGachaExp() {
        int expgain = 0;
        int currentgexp = gachaexp.get();
        if ((currentgexp + exp.get()) >= ExpTable.getExpNeededForLevel(level)) {
            expgain += ExpTable.getExpNeededForLevel(level) - exp.get();
            int nextneed = ExpTable.getExpNeededForLevel(level + 1);
            if ((currentgexp - expgain) >= nextneed) {
                expgain += nextneed;
            }
            this.gachaexp.set(currentgexp - expgain);
        } else {
            expgain = this.gachaexp.getAndSet(0);
        }
        gainExp(expgain, false, false);
        updateSingleStat(MapleStat.GACHAEXP, this.gachaexp.get());
    }

    public void gainGachaExp(int gain) {
        updateSingleStat(MapleStat.GACHAEXP, gachaexp.addAndGet(gain));
    }

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, 0, show, inChat, true);
    }

    public void gainExp(int gain, int party, boolean show, boolean inChat, boolean white) {
        if (hasDisease(MapleDisease.CURSE)) {
            gain *= 0.5;
            party *= 0.5;
        }

        int equip = (gain / 10) * pendantExp;
        int total = gain + equip + party;

        if (level < getMaxLevel()) {
            if ((long) this.exp.get() + (long) total > (long) Integer.MAX_VALUE) {
                int gainFirst = ExpTable.getExpNeededForLevel(level) - this.exp.get();
                total -= gainFirst + 1;
                this.gainExp(gainFirst + 1, party, false, inChat, white);
            }
            updateSingleStat(MapleStat.EXP, this.exp.addAndGet(total));
            if (show && gain != 0) {
                client.announce(MaplePacketCreator.getShowExpGain(gain, equip, party, inChat, white));
            }
            if (exp.get() >= ExpTable.getExpNeededForLevel(level)) {
                levelUp(true);
                while (exp.get() >= ExpTable.getExpNeededForLevel(level)) {
                    levelUp(true);
                }
                int need = ExpTable.getExpNeededForLevel(level);
                if (exp.get() >= need) {
                    setExp(need - 1);
                    updateSingleStat(MapleStat.EXP, need);
                }
            }
        } else {
            if (autorebirthing) {
                doRebirth();
            }
        }
    }

    public void gainFame(int delta) {
        this.addFame(delta);
        this.updateSingleStat(MapleStat.FAME, this.fame);
    }

    public int donorLevel() {
        return donorLevel;
    }

    public void setDonorLevel(int donorLevel) {
        this.donorLevel = donorLevel;
    }

    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
        if (meso.get() + gain < 0) {
            client.announce(MaplePacketCreator.enableActions());
            return;
        }
        updateSingleStat(MapleStat.MESO, meso.addAndGet(gain), enableActions);
        if (show) {
            client.announce(MaplePacketCreator.getShowMesoGain(gain, inChat));
        }
    }

    public void genericGuildMessage(int code) {
        this.client.announce(MaplePacketCreator.genericGuildMessage((byte) code));
    }

    public int getAccountID() {
        return accountid;
    }

    public ArrayList<PlayerBuffValueHolder> getAllBuffs() {
        ArrayList<PlayerBuffValueHolder> ret = new ArrayList<>();
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect));
        }
        return ret;
    }

    public List<Pair<MapleBuffStat, Integer>> getAllStatups() {
        List<Pair<MapleBuffStat, Integer>> ret = new ArrayList<>();
        for (MapleBuffStat mbs : effects.keySet()) {
            MapleBuffStatValueHolder mbsvh = effects.get(mbs);
            ret.add(new Pair<>(mbs, mbsvh.value));
        }
        return ret;
    }

    public List<PlayerCoolDownValueHolder> getAllCooldowns() {
        List<PlayerCoolDownValueHolder> ret = new ArrayList<>();
        for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
            ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
        }
        return ret;
    }

    public int getAllianceRank() {
        return this.allianceRank;
    }

    public void setAllianceRank(int rank) {
        allianceRank = rank;
        if (mgc != null) {
            mgc.setAllianceRank(rank);
        }
    }

    public int getAllowWarpToId() {
        return warpToId;
    }

    public void setAllowWarpToId(int id) {
        this.warpToId = id;
    }

    public int getBattleshipHp() {
        return battleshipHp;
    }

    public void setBattleshipHp(int battleshipHp) {
        this.battleshipHp = battleshipHp;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.startTime;
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.value;
    }

    public int getBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return -1;
        }
        return mbsvh.effect.getSourceId();
    }

    public MapleStatEffect getBuffEffect(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return null;
        } else {
            return mbsvh.effect;
        }
    }

    private List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
        List<MapleBuffStat> stats = new ArrayList<>();
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet()) {
            if (stateffect.getValue().effect.sameSource(effect) && (startTime == -1 || startTime == stateffect.getValue().startTime)) {
                stats.add(stateffect.getKey());
            }
        }
        return stats;
    }

    public int getChair() {
        return chair;
    }

    public void setChair(int chair) {
        this.chair = chair;
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
    }

    public MapleClient getClient() {
        return client;
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public Collection<MapleMonster> getControlledMonsters() {
        return Collections.unmodifiableCollection(controlled);
    }

    public List<MapleRing> getCrushRings() {
        Collections.sort(crushRings);
        return crushRings;
    }

    public int getCurrentCI() {
        return ci;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getCurrentMaxHp() {
        return localmaxhp;
    }

    public int getCurrentMaxMp() {
        return localmaxmp;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getCurrentType() {
        return currentType;
    }

    public int getDex() {
        return dex;
    }

    public void setDex(int dex) {
        this.dex = dex;
        recalcLocalStats();
    }

    public int getDojoEnergy() {
        return dojoEnergy;
    }

    public void setDojoEnergy(int x) {
        this.dojoEnergy = x;
    }

    public boolean getDojoParty() {
        return dojoParty;
    }

    public void setDojoParty(boolean b) {
        this.dojoParty = b;
    }

    public int getDojoPoints() {
        return dojoPoints;
    }

    public void setDojoPoints(int x) {
        this.dojoPoints = x;
    }

    public int getDojoStage() {
        return dojoStage;
    }

    public void setDojoStage(int x) {
        this.dojoStage = x;
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList<>(doors);
    }

    public int getDropRate() {
        return dropRate;
    }

    public int getEnergyBar() {
        return energybar;
    }

    public void setEnergyBar(int set) {
        energybar = set;
    }

    public EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public List<Integer> getExcluded() {
        return excluded;
    }

    public int getExp() {
        return exp.get();
    }

    public void setExp(int amount) {
        this.exp.set(amount);
    }

    public int getGachaExp() {
        return gachaexp.get();
    }

    public void setGachaExp(int amount) {
        this.gachaexp.set(amount);
    }

    public int getExpRate() {
        return expRate;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public int getFame() {
        return fame;
    }

    public void setFame(int fame) {
        this.fame = fame;
        if (this.fame > Short.MAX_VALUE) {
            this.fame = Short.MAX_VALUE;
        }
    }

    public MapleFamily getFamily() {
        return family;
    }

    public void setFamily(MapleFamily f) {
        this.family = f;
    }

    public int getFamilyId() {
        return familyId;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public boolean getFinishedDojoTutorial() {
        return finishedDojoTutorial;
    }

    public List<MapleRing> getFriendshipRings() {
        Collections.sort(friendshipRings);
        return friendshipRings;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public boolean isMale() {
        return getGender() == 0;
    }

    public MapleGuild getGuild() {
        try {
            return Server.getGuild(getGuildId(), getWorld(), null);
        } catch (Exception ex) {
            return null;
        }
    }

    public int getGuildId() {
        return guildid;
    }

    public void setGuildId(int _id) {
        guildid = _id;
        if (guildid > 0) {
            if (mgc == null) {
                mgc = new MapleGuildCharacter(this);
            } else {
                mgc.setGuildId(guildid);
            }
        } else {
            mgc = null;
        }
    }

    public int getGuildRank() {
        return guildrank;
    }

    public void setGuildRank(int _rank) {
        guildrank = _rank;
        if (mgc != null) {
            mgc.setGuildRank(_rank);
        }
    }

    public int getHair() {
        return hair;
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public HiredMerchant getHiredMerchant() {
        return hiredMerchant;
    }

    public void setHiredMerchant(HiredMerchant merchant) {
        this.hiredMerchant = merchant;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int newhp) {
        setHp(newhp, false);
    }

    public int getHpMpApUsed() {
        return hpMpApUsed;
    }

    public void setHpMpApUsed(int mpApUsed) {
        this.hpMpApUsed = mpApUsed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public int getInt() {
        return int_;
    }

    public void setInt(int int_) {
        this.int_ = int_;
        recalcLocalStats();
    }

    public MapleInventory[] getCreativeInventory() {
        return creativeInventory;
    }

    public void setCreativeInventory(MapleInventory[] creativeInventory) {
        this.creativeInventory = creativeInventory;
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return (creativeInventory == null) ? inventory[type.ordinal()] : creativeInventory[type.ordinal()];
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemid)).countById(itemid);
        if (checkEquipped) {
            possesed += getInventory(MapleInventoryType.EQUIPPED).countById(itemid);
        }
        return possesed;
    }

    public MapleJob getJob() {
        return job;
    }

    public void setJob(MapleJob job) {
        this.job = job;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public int getJobType() {
        return job.getId() / 1000;
    }

    public Map<Integer, MapleKeyBinding> getKeymap() {
        return keymap;
    }

    public long getLastHealed() {
        return lastHealed;
    }

    public void setLastHealed(long time) {
        this.lastHealed = time;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLuk() {
        return luk;
    }

    public void setLuk(int luk) {
        this.luk = luk;
        recalcLocalStats();
    }

    public int getFh() {
        Point pos = this.getPosition();
        pos.y -= 6;
        if (getMap().getFootholds().findBelow(pos) == null) {
            return 0;
        } else {
            return getMap().getFootholds().findBelow(pos).getY1();
        }
    }

    public MapleMap getMap() {
        return map;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }

    public void setMapId(int mapid) {
        this.mapid = mapid;
    }

    public int getMarkedMonster() {
        return markedMonster;
    }

    public void setMarkedMonster(int markedMonster) {
        this.markedMonster = markedMonster;
    }

    public MapleRing getMarriageRing() {
        return marriageRing;
    }

    public void setMarriageRing(MapleRing marriageRing) {
        this.marriageRing = marriageRing;
    }

    public int getMarried() {
        return married;
    }

    public int getMasterLevel(Skill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).masterlevel;
    }

    public int getMaxHp() {
        return maxhp;
    }

    public void setMaxHp(int hp) {
        this.maxhp = hp;
        recalcLocalStats();
    }

    public int getMaxLevel() {
        return 200;
    }

    public int getMaxMp() {
        return maxmp;
    }

    public void setMaxMp(int mp) {
        this.maxmp = mp;
        recalcLocalStats();
    }

    public int getMeso() {
        return meso.get();
    }

    public int getMerchantMeso() {
        return merchantmeso;
    }

    public void setMerchantMeso(int set) {
        try (Connection con = client.getWorldServer().getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, set);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            return;
        }
        merchantmeso = set;
    }

    public int getMesoRate() {
        return mesoRate;
    }

    public int getMesosTraded() {
        return mesosTraded;
    }

    public int getMessengerPosition() {
        return messengerposition;
    }

    public void setMessengerPosition(int position) {
        this.messengerposition = position;
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public MaplePartyCharacter getMPC() {
        if (mpc == null) {
            mpc = new MaplePartyCharacter(this);
        }
        return mpc;
    }

    public void setMPC(MaplePartyCharacter mpc) {
        this.mpc = mpc;
    }

    public MapleMiniGame getMiniGame() {
        return miniGame;
    }

    public void setMiniGame(MapleMiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public int getMiniGamePoints(String type, boolean omok) {
        if (omok) {
            switch (type) {
                case "wins":
                    return omokwins;
                case "losses":
                    return omoklosses;
                default:
                    return omokties;
            }
        } else {
            switch (type) {
                case "wins":
                    return matchcardwins;
                case "losses":
                    return matchcardlosses;
                default:
                    return matchcardties;
            }
        }
    }

    public MonsterBook getMonsterBook() {
        return monsterbook;
    }

    public int getMonsterBookCover() {
        return bookCover;
    }

    public void setMonsterBookCover(int bookCover) {
        this.bookCover = bookCover;
    }

    public MapleMount getMount() {
        return maplemount;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int newmp) {
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > localmaxmp) {
            tmp = localmaxmp;
        }
        this.mp = tmp;
    }

    public MapleMessenger getMessenger() {
        return messenger;
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNextEmptyPetIndex() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                return i;
            }
        }
        return 3;
    }

    public int getNoPets() {
        int ret = 0;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                ret++;
            }
        }
        return ret;
    }

    public int getNumControlledMonsters() {
        return controlled.size();
    }

    public MapleParty getParty() {
        return party;
    }

    public void setParty(MapleParty party) {
        if (party == null) {
            this.mpc = null;
        }
        this.party = party;
    }

    public int getPartyId() {
        return (party != null ? party.getId() : -1);
    }

    public MaplePlayerShop getPlayerShop() {
        return playerShop;
    }

    public void setPlayerShop(MaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public MaplePet[] getPets() {
        return pets;
    }

    public MaplePet getPet(int index) {
        if (index < 0 || index >= pets.length) {
            return null;
        }
        return pets[index];
    }

    public byte getPetIndex(int petId) {
        for (byte i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == petId) {
                    return i;
                }
            }
        }
        return -1;
    }

    public byte getPetIndex(MaplePet pet) {
        for (byte i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getPossibleReports() {
        return possibleReports;
    }

    public final byte getQuestStatus(final int quest) {
        for (final MapleQuestStatus q : quests.values()) {
            if (q.getQuest().getId() == quest) {
                return (byte) q.getStatus().getId();
            }
        }
        return 0;
    }

    public MapleQuestStatus getQuest(MapleQuest quest) {
        if (!quests.containsKey(quest.getId())) {
            return new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
        }
        return quests.get(quest.getId());
    }

    public boolean needQuestItem(int questid, int itemid) {
        if (questid <= 0) {
            return true; // For non quest items :3
        }
        MapleQuest quest = MapleQuest.getInstance(questid);
        return getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid) < quest.getItemAmountNeeded(itemid);
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public int getRemainingAp() {
        return remainingAp;
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }

    public int getRemainingSp() {
        return remainingSp[GameConstants.getSkillBook(job.getId())]; // default
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp[GameConstants.getSkillBook(job.getId())] = remainingSp; // default
    }

    public int getRemainingSpBySkill(final int skillbook) {
        return remainingSp[skillbook];
    }

    public int[] getRemainingSps() {
        return remainingSp;
    }

    public int getSavedLocation(String type) {
        SavedLocation sl = savedLocations[SavedLocationType.valueOf(type).ordinal()];
        if (sl == null) {
            return -1;
        }
        int m = sl.getMapId();
        if (!SavedLocationType.valueOf(type).equals(SavedLocationType.WORLDTOUR)) {
            clearSavedLocation(SavedLocationType.valueOf(type));
        }
        return m;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String find) {
        search = find;
    }

    public MapleShop getShop() {
        return shop;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public Map<Skill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public int getSkillLevel(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return 0;
        }
        return ret.skillevel;
    }

    public byte getSkillLevel(Skill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).skillevel;
    }

    public long getSkillExpiration(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return -1;
        }
        return ret.expiration;
    }

    public long getSkillExpiration(Skill skill) {
        if (skills.get(skill) == null) {
            return -1;
        }
        return skills.get(skill).expiration;
    }

    public MapleSkinColor getSkinColor() {
        return skinColor;
    }

    public void setSkinColor(MapleSkinColor skinColor) {
        this.skinColor = skinColor;
    }

    public int getSlot() {
        return slots;
    }

    public void setSlot(int slotid) {
        slots = slotid;
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public final int getStartedQuestsSize() {
        int i = 0;
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                if (q.getQuest().getInfoNumber() > 0) {
                    i++;
                }
                i++;
            }
        }
        return i;
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect;
    }

    public MapleStorage getStorage() {
        return storage;
    }

    public int getStr() {
        return str;
    }

    public void setStr(int str) {
        this.str = str;
        recalcLocalStats();
    }

    public Map<Integer, MapleSummon> getSummons() {
        return summons;
    }

    public int getTotalStr() {
        return localstr;
    }

    public int getTotalDex() {
        return localdex;
    }

    public int getTotalInt() {
        return localint_;
    }

    public int getTotalLuk() {
        return localluk;
    }

    public int getTotalMagic() {
        return magic;
    }

    public int getTotalWatk() {
        return watk;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public int getVanquisherKills() {
        return vanquisherKills;
    }

    public void setVanquisherKills(int x) {
        this.vanquisherKills = x;
    }

    public int getVanquisherStage() {
        return vanquisherStage;
    }

    public void setVanquisherStage(int x) {
        this.vanquisherStage = x;
    }

    public Collection<MapleMapObject> getVisibleMapObjects() {
        return visibleMapObjects.values();
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        if (skillid == 5221999) {
            this.battleshipHp = (int) length;
            addCooldown(skillid, 0, length, null);
        } else {
            int time = (int) ((length + starttime) - System.currentTimeMillis());
            addCooldown(skillid, System.currentTimeMillis(), time, TaskExecutor.createTask(new CancelCooldownAction(this, skillid), time));
        }
    }

    public int getGMLevel() {
        return gmLevel;
    }

    public String guildCost() {
        return StringUtil.formatNumber(MapleGuild.CREATE_GUILD_COST);
    }

    private void guildUpdate() {
        if (this.guildid < 1) {
            return;
        }
        mgc.setLevel(level);
        mgc.setJobId(job.getId());
        try {
            Server.memberLevelJobUpdate(this.mgc);
            // Server.getGuild(guildid, world, mgc).gainGP(40);
            int allianceId = getGuild().getAllianceId();
            if (allianceId > 0) {
                Server.allianceMessage(allianceId, MaplePacketCreator.updateAllianceJobLevel(this), getId(), -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleEnergyChargeGain() { // to get here energychargelevel has
        // to be > 0
        Skill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
        MapleStatEffect ceffect;
        ceffect = energycharge.getEffect(getSkillLevel(energycharge));
        if (energybar < 10000) {
            energybar += 102;
            if (energybar > 10000) {
                energybar = 10000;
            }
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.ENERGY_CHARGE, energybar));
            setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
            client.announce(MaplePacketCreator.giveBuff(energybar, 0, stat));
            client.announce(MaplePacketCreator.showOwnBuffEffect(energycharge.getId(), 2));
            getMap().broadcastMessage(this, MaplePacketCreator.showBuffeffect(id, energycharge.getId(), 2));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(energybar, stat));
        }
        if (energybar >= 10000 && energybar < 11000) {
            energybar = 15000;
            final MapleCharacter chr = this;
            TaskExecutor.createTask(new Runnable() {
                @Override
                public void run() {
                    energybar = 0;
                    List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.ENERGY_CHARGE, energybar));
                    setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
                    client.announce(MaplePacketCreator.giveBuff(energybar, 0, stat));
                    getMap().broadcastMessage(chr, MaplePacketCreator.giveForeignBuff(energybar, stat));
                }
            }, ceffect.getDuration());
        }
    }

    public void handleOrbconsume() {
        int skillid = isCygnus() ? DawnWarrior.COMBO_ATTACK : Crusader.COMBO_ATTACK;
        Skill combo = SkillFactory.getSkill(skillid);
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.COMBO, 1));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        client.announce(MaplePacketCreator.giveBuff(skillid, combo.getEffect(getSkillLevel(combo)).getDuration() + (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis())), stat));
        getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat), false);
    }

    public boolean hasEntered(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEntered(String script, int mapId) {
        if (entered.containsKey(mapId)) {
            return entered.get(mapId).equals(script);
        }
        return false;
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(to.getId());
        try (Connection con = client.getWorldServer().getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)")) {
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasMerchant() {
        return hasMerchant;
    }

    public boolean haveItem(int itemid) {
        return getItemQuantity(itemid, false) > 0;
    }

    public void increaseGuildCapacity() { // hopefully nothing is null
        if (getMeso() < getGuild().getIncreaseGuildCost(getGuild().getCapacity())) {
            dropMessage(1, "You don't have enough mesos.");
            return;
        }
        Server.increaseGuildCapacity(guildid);
        gainMeso(-getGuild().getIncreaseGuildCost(getGuild().getCapacity()), true, false, false);
    }

    public boolean isActiveBuffedValue(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                return true;
            }
        }
        return false;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public boolean isBuffFrom(MapleBuffStat stat, Skill skill) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return false;
        }
        return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
    }

    public boolean isCygnus() {
        return getJobType() == 1;
    }

    public boolean isAran() {
        return getJob().getId() >= 2000 && getJob().getId() <= 2112;
    }

    public boolean isBeginnerJob() {
        return (getJob().getId() == 0 || getJob().getId() == 1000 || getJob().getId() == 2000) && getLevel() < 11;
    }

    public boolean isGM() {
        return gmLevel > 0;
    }

    public void setGM(int level) {
        this.gmLevel = level;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        return visibleMapObjects.containsKey(mo.getObjectId());
    }

    public boolean isPartyLeader() {
        return party.getLeader() == party.getMemberById(getId());
    }

    public void leaveMap() {
        controlled.clear();
        controlled = new LinkedHashSet<>();

        visibleMapObjects.clear();
        visibleMapObjects = new ConcurrentHashMap<>();

        if (chair != 0) {
            chair = 0;
        }
        hpDecreaseTask = TaskExecutor.cancelTask(hpDecreaseTask);
    }

    public void levelUp(boolean takeexp) {
        Skill improvingMaxHP = null;
        Skill improvingMaxMP = null;
        int improvingMaxHPLevel = 0;
        int improvingMaxMPLevel = 0;

        gainAp(5);

        if (job == MapleJob.BEGINNER || job == MapleJob.NOBLESSE || job == MapleJob.LEGEND) {
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(10, 12);
        } else if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(DawnWarrior.MAX_HP_ENHANCEMENT) : SkillFactory.getSkill(Swordsman.IMPROVED_MAXHP_INCREASE);
            if (job.isA(MapleJob.CRUSADER)) {
                improvingMaxMP = SkillFactory.getSkill(1210000);
            } else if (job.isA(MapleJob.DAWNWARRIOR2)) {
                improvingMaxMP = SkillFactory.getSkill(11110000);
            }
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(24, 28);
            maxmp += Randomizer.rand(4, 6);
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            improvingMaxMP = isCygnus() ? SkillFactory.getSkill(BlazeWizard.INCREASING_MAX_MP) : SkillFactory.getSkill(Magician.IMPROVED_MAXMP_INCREASE);
            improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
            maxhp += Randomizer.rand(10, 14);
            maxmp += Randomizer.rand(22, 24);
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF) || (job.getId() > 1299 && job.getId() < 1500)) {
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(14, 16);
        } else if (job.isA(MapleJob.GM)) {
            maxhp = 30000;
            maxmp = 30000;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.IMPROVE_MAXHP) : SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(22, 28);
            maxmp += Randomizer.rand(18, 23);
        } else if (job.isA(MapleJob.ARAN1)) {
            maxhp += Randomizer.rand(44, 48);
            int aids = Randomizer.rand(4, 8);
            maxmp += aids + Math.floor(aids * 0.1);
        }
        if (improvingMaxHPLevel > 0 && (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.DAWNWARRIOR1))) {
            maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
        }
        if (improvingMaxMPLevel > 0 && (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.CRUSADER) || job.isA(MapleJob.BLAZEWIZARD1))) {
            maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
        }
        maxmp += localint_ / 10;
        if (takeexp) {
            exp.addAndGet(-ExpTable.getExpNeededForLevel(level));
            if (exp.get() < 0) {
                exp.set(0);
            }
        }
        level++;
        Achievements.testFor(this, -1);
        if (level >= getMaxLevel()) {
            exp.set(0);
            level = getMaxLevel(); // To prevent levels past 200
        }
        if (level % 50 == 0) { // For the drop + meso rate
            setRates();
        }
        maxhp = Math.min(30000, maxhp);
        maxmp = Math.min(30000, maxmp);
        if (level == 200) {
            exp.set(0);
        }
        hp = maxhp;
        mp = maxmp;
        recalcLocalStats();
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(10);
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<>(MapleStat.HP, localmaxhp));
        statup.add(new Pair<>(MapleStat.MP, localmaxmp));
        statup.add(new Pair<>(MapleStat.EXP, exp.get()));
        statup.add(new Pair<>(MapleStat.LEVEL, level));
        statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
        statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
        statup.add(new Pair<>(MapleStat.STR, str));
        statup.add(new Pair<>(MapleStat.DEX, dex));
        if (job.getId() % 1000 > 0) {
            remainingSp[GameConstants.getSkillBook(job.getId())] += 3;
            statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[GameConstants.getSkillBook(job.getId())]));
        }
        client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0), false);
        recalcLocalStats();
        setMPC(new MaplePartyCharacter(this));
        silentPartyUpdate();
        if (ServerConstants.PERFECT_PITCH && level >= 30) {
            if (MapleInventoryManipulator.checkSpace(client, 4310000, (short) 1, "")) {
                MapleInventoryManipulator.addById(client, 4310000, (short) 1);
            }
        }
        //        if (this.guildid > 0) {
        //            getGuild().broadcast(MaplePacketCreator.levelUpMessage(2, level, name), this.getId());
        //        }
        //        if (level == 200 && !isGM()) {
        //            final String names = (getMedalText() + name);
        //            client.getWorldServer().broadcastPacket(MaplePacketCreator.serverNotice(6, String.format(LEVEL_200, names, names)));
        //        }
        //        levelUpMessages();
        guildUpdate();
        if (level % 50 == 0) {
            if (MapleInventoryManipulator.checkSpace(client, 5220000, (short) 1, "")) {
                MapleInventoryManipulator.addById(client, 5220000, (short) 1);
            }
        }
    }

    public void gainAp(int amount) {
        int nAbilityPoints = Math.min(remainingAp + amount, Short.MAX_VALUE);
        remainingAp = nAbilityPoints;
        updateSingleStat(MapleStat.AVAILABLEAP, nAbilityPoints);
    }

    public void gainSp(int amount) {
        int skillPoints = remainingSp[GameConstants.getSkillBook(job.getId())];
        int nSkillPoints = Math.min(skillPoints + amount, Short.MAX_VALUE);
        updateSingleStat(MapleStat.AVAILABLESP, nSkillPoints);
    }

    public void message(String m) {
        dropMessage(5, m);
    }

    public void yellowMessage(String m) {
        announce(MaplePacketCreator.sendYellowTip(m));
    }

    public void mobKilled(int id) {
        // It seems nexon uses monsters that don't exist in the WZ (except
        // string) to merge multiple mobs together for these 3 monsters.
        // We also want to run mobKilled for both since there are some quest
        // that don't use the updated ID...
        if (id == 1110100 || id == 1110130) {
            mobKilled(9101000);
        } else if (id == 2230101 || id == 2230131) {
            mobKilled(9101001);
        } else if (id == 1140100 || id == 1140130) {
            mobKilled(9101002);
        }
        for (MapleQuestStatus q : quests.values()) {
            try {
                if (q.getStatus() == MapleQuestStatus.Status.COMPLETED || q.getQuest().canComplete(this, null)) {
                    continue;
                }
                String progress = q.getProgress(id);
                if (!progress.isEmpty() && Integer.parseInt(progress) >= q.getQuest().getMobAmountNeeded(id)) {
                    continue;
                }
                if (q.progress(id)) {
                    client.announce(MaplePacketCreator.updateQuest(q, false));
                }
            } catch (Exception e) {
                LOGGER.info("Error while processing quest {}", q.getQuestID(), e);
            }
        }
    }

    public void mount(int id, int skillid) {
        maplemount = new MapleMount(this, id, skillid);
    }

    public void createPlayerNPC(MapleCharacter v, int scriptId, String script) {
        try (Connection con = client.getWorldServer().getConnection()) {
            int pnpcid = 0;
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO playernpcs (name, scriptid, script, map, hair, face, skin, gender, foothold, dir, x, cy, rx0, rx1) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, v.getName());
                ps.setInt(2, scriptId);
                ps.setString(3, script);
                ps.setInt(4, getMapId());
                ps.setInt(5, v.getHair());
                ps.setInt(6, v.getFace());
                ps.setInt(7, v.getSkinColor().id);
                ps.setInt(8, v.getGender());
                ps.setInt(9, getMap().getFootholds().findBelow(getPosition()).getId());
                ps.setInt(10, isFacingLeft() ? 0 : 1);
                ps.setInt(11, getPosition().x);
                ps.setInt(12, getPosition().y);
                ps.setInt(13, getPosition().x + 50);
                ps.setInt(14, getPosition().x - 50);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        pnpcid = rs.getInt(1);
                    }
                }
            }
            if (pnpcid > 0) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO playernpcs_equip (npcid, equipid, type, equippos) VALUES (?, ?, 0, ?)")) {
                    ps.setInt(1, pnpcid);
                    for (Item item : v.getInventory(MapleInventoryType.EQUIPPED).list()) {
                        int slot = Math.abs(item.getPosition());
                        if ((slot < 12 && slot > 0) || (slot > 100 && slot < 112)) {
                            ps.setInt(2, item.getItemId());
                            ps.setInt(3, item.getPosition());
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs WHERE id = ?")) {
                    ps.setInt(1, pnpcid);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            PlayerNPC playerNPC = new PlayerNPC(con, rs);
                            for (MapleChannel channel : Server.getChannelsFromWorld(world)) {
                                MapleMap m = channel.getMap(getMapId());
                                m.broadcastMessage(MaplePacketCreator.spawnPlayerNPC(playerNPC));
                                m.broadcastMessage(MaplePacketCreator.getPlayerNPC(playerNPC));
                                m.addMapObject(playerNPC);
                            }
                        } else {
                            LOGGER.warn("Unable to find newly created player npc {}", pnpcid);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void playerDead() {
        getMap().setLastPlayerDiedInMap(getName());
        cancelAllBuffs(false);
        dispelDebuffs();
        ManualPlayerEvent playerEvent = getClient().getWorldServer().getPlayerEvent();
        if (playerEvent != null) {
            if (getMap() == playerEvent.getMap()) {
                return;
            }
        }
        if (getEventInstance() != null) {
            getEventInstance().playerKilled(this);
        }
        getGenericEvents().forEach(g -> g.onPlayerDeath(null, this));
        int[] charmID = {5130000, 4031283, 4140903};
        int possessed = 0;
        int i;
        for (i = 0; i < charmID.length; i++) {
            int quantity = getItemQuantity(charmID[i], false);
            if (quantity > 0) {
                possessed = quantity;
                break;
            }
        }
        if (possessed > 0) {
            message("You have used a safety charm, so your EXP points have not been decreased.");
            MapleInventoryManipulator.removeById(client, MapleItemInformationProvider.getInstance().getInventoryType(charmID[i]), charmID[i], 1, true, false);
        } else if (mapid > 925020000 && mapid < 925030000) {
            this.dojoStage = 0;
        } else if (mapid > 980000100 && mapid < 980000700) {
//            getMap().broadcastMessage(this, MCarnivalPacket.getMonsterCarnivalPlayerDeath(this));
        } else if (getJob() != MapleJob.BEGINNER) {
            if (getOccupation() != null && getOccupation().getType() == Occupation.Type.Undead) {
                return;
            }
            int XPdummy = ExpTable.getExpNeededForLevel(getLevel());
            if (getMap().isTown()) {
                XPdummy /= 100;
            }
            if (XPdummy == ExpTable.getExpNeededForLevel(getLevel())) {
                if (getLuk() <= 100 && getLuk() > 8) {
                    XPdummy *= (200 - getLuk()) / 2000;
                } else if (getLuk() < 8) {
                    XPdummy /= 10;
                } else {
                    XPdummy /= 20;
                }
            }
            if (getExp() > XPdummy) {
                gainExp(-XPdummy, false, false);
            } else {
                gainExp(-getExp(), false, false);
            }
        }
        if (getBuffedValue(MapleBuffStat.MORPH) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        }

        if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        }

        if (getChair() == -1) {
            setChair(0);
            client.announce(MaplePacketCreator.cancelChair(-1));
            getMap().broadcastMessage(this, MaplePacketCreator.showChair(getId(), 0), false);
        }
        client.announce(MaplePacketCreator.enableActions());
    }

    private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
        if (dragonBloodTask != null) {
            dragonBloodTask.cancel();
            dragonBloodTask = TaskExecutor.createRepeatingTask(new Runnable() {
                @Override
                public void run() {
                    addHP(-bloodEffect.getX());
                    client.announce(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), bloodEffect.getSourceId(), 5), false);
                    checkBerserk();
                }
            }, 4000, 4000);
        }
    }

    private void recalcLocalStats() {
        int oldmaxhp = localmaxhp;
        localmaxhp = getMaxHp();
        localmaxmp = getMaxMp();
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        int speed = 100, jump = 100;
        magic = localint_;
        watk = 0;

        for (Item item : getInventory(MapleInventoryType.EQUIPPED)) {
            Equip equip = (Equip) item;
            localmaxhp += equip.getHp();
            localmaxmp += equip.getMp();
            localdex += equip.getDex();
            localint_ += equip.getInt();
            localstr += equip.getStr();
            localluk += equip.getLuk();
            magic += equip.getMatk() + equip.getInt();
            watk += equip.getWatk();
            speed += equip.getSpeed();
            jump += equip.getJump();
        }
        magic = Math.min(magic, 2000);
        Integer hbhp = getBuffedValue(MapleBuffStat.HYPERBODYHP);
        if (hbhp != null) {
            localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
        }
        Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
        if (hbmp != null) {
            localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
        }
        localmaxhp = Math.min(30000, localmaxhp);
        localmaxmp = Math.min(30000, localmaxmp);
        Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
        if (watkbuff != null) {
            watk += watkbuff;
        }
        MapleStatEffect combo = getBuffEffect(MapleBuffStat.ARAN_COMBO);
        if (combo != null) {
            watk += combo.getX();
        }

        if (energybar == 15000) {
            Skill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
            MapleStatEffect ceffect = energycharge.getEffect(getSkillLevel(energycharge));
            watk += ceffect.getWatk();
        }

        Integer mwarr = getBuffedValue(MapleBuffStat.MAPLE_WARRIOR);
        if (mwarr != null) {
            localstr += getStr() * mwarr / 100;
            localdex += getDex() * mwarr / 100;
            localint_ += getInt() * mwarr / 100;
            localluk += getLuk() * mwarr / 100;
        }
        if (job.isA(MapleJob.BOWMAN)) {
            Skill expert = null;
            if (job.isA(MapleJob.MARKSMAN)) {
                expert = SkillFactory.getSkill(3220004);
            } else if (job.isA(MapleJob.BOWMASTER)) {
                expert = SkillFactory.getSkill(3120005);
            }
            if (expert != null) {
                int boostLevel = getSkillLevel(expert);
                if (boostLevel > 0) {
                    watk += expert.getEffect(boostLevel).getX();
                }
            }
        }
        Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
        if (matkbuff != null) {
            magic += matkbuff;
        }
        Integer speedbuff = getBuffedValue(MapleBuffStat.SPEED);
        if (speedbuff != null) {
            speed += speedbuff;
        }
        Integer jumpbuff = getBuffedValue(MapleBuffStat.JUMP);
        if (jumpbuff != null) {
            jump += jumpbuff;
        }

        Integer blessing = getSkillLevel(10000000 * getJobType() + 12);
        if (blessing > 0) {
            watk += blessing;
            magic += blessing * 2;
        }

        if (job.isA(MapleJob.THIEF) || job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.NIGHTWALKER1) || job.isA(MapleJob.WINDARCHER1)) {
            Item weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
            if (weapon_item != null) {
                MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
                boolean bow = weapon == MapleWeaponType.BOW;
                boolean crossbow = weapon == MapleWeaponType.CROSSBOW;
                boolean claw = weapon == MapleWeaponType.CLAW;
                boolean gun = weapon == MapleWeaponType.GUN;
                if (bow || crossbow || claw || gun) {
                    // Also calc stars into this.
                    MapleInventory inv = getInventory(MapleInventoryType.USE);
                    for (short i = 1; i <= inv.getSlotLimit(); i++) {
                        Item item = inv.getItem(i);
                        if (item != null) {
                            if ((claw && ItemConstants.isThrowingStar(item.getItemId())) || (gun && ItemConstants.isBullet(item.getItemId())) || (bow && ItemConstants.isArrowForBow(item.getItemId())) || (crossbow && ItemConstants.isArrowForCrossBow(item.getItemId()))) {
                                if (item.getQuantity() > 0) {
                                    // Finally there!
                                    watk += MapleItemInformationProvider.getInstance().getWatkForProjectile(item.getItemId());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            // Add throwing stars to dmg.
        }

        if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
            updatePartyMemberHP();
        }
    }

    public void receivePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapId() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = Server.getWorld(world).getChannel(channel).getPlayerStorage().find(p -> p.getName().equalsIgnoreCase(partychar.getName()));
                    if (other != null) {
                        client.announce(MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getHp(), other.getCurrentMaxHp()));
                    }
                }
            }
        }
    }

    public void registerEffect(MapleStatEffect effect, long starttime, Task task) {
        if (effect.isDragonBlood()) {
            prepareDragonBlood(effect);
        } else if (effect.isBerserk()) {
            checkBerserk();
        } else if (effect.isBeholder()) {
            final int beholder = DarkKnight.BEHOLDER;
            if (beholderHealingTask != null) {
                beholderHealingTask.cancel();
                beholderHealingTask = null;
            }
            if (beholderBuffTask != null) {
                beholderBuffTask.cancel();
                beholderBuffTask = null;
            }
            Skill bHealing = SkillFactory.getSkill(DarkKnight.AURA_OF_THE_BEHOLDER);
            int bHealingLvl = getSkillLevel(bHealing);
            if (bHealingLvl > 0) {
                final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                int healInterval = healEffect.getX() * 1000;
                beholderHealingTask = TaskExecutor.createRepeatingTask(new Runnable() {
                    @Override
                    public void run() {
                        addHP(healEffect.getHp());
                        client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, 5), true);
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showOwnBuffEffect(beholder, 2), false);
                    }
                }, healInterval, healInterval);
            }
            Skill bBuff = SkillFactory.getSkill(DarkKnight.HEX_OF_THE_BEHOLDER);
            if (getSkillLevel(bBuff) > 0 && summons.containsKey(DarkKnight.BEHOLDER)) {
                final MapleStatEffect buffEffect = bBuff.getEffect(getSkillLevel(bBuff));
                int buffInterval = buffEffect.getX() * 1000;
                beholderBuffTask = TaskExecutor.createRepeatingTask(new Runnable() {
                    @Override
                    public void run() {
                        buffEffect.applyTo(MapleCharacter.this);
                        client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, (int) (Math.random() * 3) + 6), true);
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), beholder, 2), false);
                    }
                }, buffInterval, buffInterval);
            }
        } else if (effect.isRecovery()) {
            final byte heal = (byte) effect.getX();
            recoveryTask = TaskExecutor.createRepeatingTask(new Runnable() {
                @Override
                public void run() {
                    addHP(heal);
                    client.announce(MaplePacketCreator.showOwnRecovery(heal));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showRecovery(id, heal), false);
                }
            }, 5000, 5000);
        }
        for (Pair<MapleBuffStat, Integer> statup : effect.getStatups()) {
            effects.put(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, task, statup.getRight()));
        }
        recalcLocalStats();
    }

    public void removeAllCooldownsExcept(int id, boolean packet) {
        for (MapleCoolDownValueHolder mcvh : coolDowns.values()) {
            if (mcvh.skillId != id) {
                coolDowns.remove(mcvh.skillId);
                if (packet) {
                    client.announce(MaplePacketCreator.skillCooldown(mcvh.skillId, 0));
                }
            }
        }
    }

    public void removeCooldown(int skillId) {
        coolDowns.remove(skillId);
    }

    public void removePet(MaplePet pet, boolean shift_left) {
        int slot = -1;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    pets[i] = null;
                    slot = i;
                    break;
                }
            }
        }
        if (shift_left) {
            if (slot > -1) {
                for (int i = slot; i < 3; i++) {
                    if (i != 2) {
                        pets[i] = pets[i + 1];
                    } else {
                        pets[i] = null;
                    }
                }
            }
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.remove(mo.getObjectId());
    }

    public void resetStats() {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(5);
        int tap = 0, tsp = 1;
        int tstr = 4, tdex = 4, tint = 4, tluk = 4;
        int levelap = (isCygnus() ? 6 : 5);
        switch (job.getId()) {
            case 100:
            case 1100:
            case 2100:// ?
                tstr = 35;
                tap = ((getLevel() - 10) * levelap) + 14;
                tsp += ((getLevel() - 10) * 3);
                break;
            case 200:
            case 1200:
                tint = 20;
                tap = ((getLevel() - 8) * levelap) + 29;
                tsp += ((getLevel() - 8) * 3);
                break;
            case 300:
            case 1300:
            case 400:
            case 1400:
                tdex = 25;
                tap = ((getLevel() - 10) * levelap) + 24;
                tsp += ((getLevel() - 10) * 3);
                break;
            case 500:
            case 1500:
                tdex = 20;
                tap = ((getLevel() - 10) * levelap) + 29;
                tsp += ((getLevel() - 10) * 3);
                break;
        }
        gainAp(tap);
        gainSp(tsp);
        this.dex = tdex;
        this.int_ = tint;
        this.str = tstr;
        this.luk = tluk;
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, tap));
        statup.add(new Pair<>(MapleStat.AVAILABLESP, tsp));
        statup.add(new Pair<>(MapleStat.STR, tstr));
        statup.add(new Pair<>(MapleStat.DEX, tdex));
        statup.add(new Pair<>(MapleStat.INT, tint));
        statup.add(new Pair<>(MapleStat.LUK, tluk));
        announce(MaplePacketCreator.updatePlayerStats(statup, this));
    }

    public void resetBattleshipHp() {
        this.battleshipHp = 4000 * getSkillLevel(SkillFactory.getSkill(Corsair.BATTLESHIP)) + ((getLevel() - 120) * 2000);
    }

    public void resetEnteredScript() {
        entered.remove(map.getId());
    }

    public void resetEnteredScript(int mapId) {
        entered.remove(mapId);
    }

    public void resetEnteredScript(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                entered.remove(mapId);
            }
        }
    }

    public void resetMGC() {
        this.mgc = null;
    }

    public synchronized void saveCooldowns() {
        if (getAllCooldowns().size() > 0) {
            try (Connection con = client.getWorldServer().getConnection()) {
                deleteWhereCharacterId(con, "DELETE FROM cooldowns WHERE charid = ?");
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, getId());
                    for (PlayerCoolDownValueHolder cooling : getAllCooldowns()) {
                        ps.setInt(2, cooling.skillId);
                        ps.setLong(3, cooling.startTime);
                        ps.setLong(4, cooling.length);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveGuildStatus() {
        try (Connection con = client.getWorldServer().getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, allianceRank = ? WHERE id = ?")) {
            ps.setInt(1, guildid);
            ps.setInt(2, guildrank);
            ps.setInt(3, allianceRank);
            ps.setInt(4, id);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveLocation(String type) {
        MaplePortal closest = map.findClosestPortal(getPosition());
        savedLocations[SavedLocationType.valueOf(type).ordinal()] = new SavedLocation(getMapId(), closest != null ? closest.getId() : 0);
    }

    public final boolean insertNewChar() {
        try (Connection con = Server.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO characters (str, dex, luk, `int`, gm, skincolor, gender, job, hair, face, map, meso, spawnpoint, accountid, name, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, 12);
                ps.setInt(2, 5);
                ps.setInt(3, 4);
                ps.setInt(4, 4);
                ps.setInt(5, gmLevel);
                ps.setInt(6, skinColor.getId());
                ps.setInt(7, gender);
                ps.setInt(8, getJob().getId());
                ps.setInt(9, hair);
                ps.setInt(10, face);
                ps.setInt(11, mapid);
                ps.setInt(12, Math.abs(meso.get()));
                ps.setInt(13, 0);
                ps.setInt(14, accountid);
                ps.setString(15, name);
                ps.setInt(16, world);

                int updateRows = ps.executeUpdate();
                if (updateRows < 1) {
                    LOGGER.error("No data received from new character data insert '{}'", name);
                    return false;
                }
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        this.id = rs.getInt(1);
                    } else {
                        LOGGER.info("No generated key received from character data insert");
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, id);
                for (int i = 0; i < DEFAULT_KEY.length; i++) {
                    ps.setInt(2, DEFAULT_KEY[i]);
                    ps.setInt(3, DEFAULT_TYPE[i]);
                    ps.setInt(4, DEFAULT_ACTION[i]);
                    ps.execute();
                }
            }

            final List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();

            for (MapleInventory iv : inventory) {
                for (Item item : iv.list()) {
                    itemsWithType.add(new Pair<>(item, iv.getType()));
                }
            }

            ItemFactory.INVENTORY.saveItems(itemsWithType, id, con);
            return true;
        } catch (Throwable t) {
            LOGGER.info("Error while creating character", t);
            return false;
        }
    }

    public void saveToDB() {
        final long beginTimestamp = System.currentTimeMillis();
        try (Connection con = client.getWorldServer().getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, gachaexp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpMpUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, mountlevel = ?, mountexp = ?, mounttiredness= ?, equipslots = ?, useslots = ?, setupslots = ?, etcslots = ?,  monsterbookcover = ?, vanquisherStage = ?, dojoPoints = ?, lastDojoStage = ?, finishedDojoTutorial = ?, vanquisherKills = ?, matchcardwins = ?, matchcardlosses = ?, matchcardties = ?, omokwins = ?, omoklosses = ?, omokties = ?, dataString = ?, fishingpoints = ?, daily = ?, reborns = ?, eventpoints = ?, rebirthpoints = ?, occupation = ?, jumpquestpoints = ?, chattype = ?, name = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, level);
                ps.setInt(2, fame);
                ps.setInt(3, str);
                ps.setInt(4, dex);
                ps.setInt(5, luk);
                ps.setInt(6, int_);
                ps.setInt(7, Math.abs(exp.get()));
                ps.setInt(8, Math.abs(gachaexp.get()));
                ps.setInt(9, hp);
                ps.setInt(10, mp);
                ps.setInt(11, maxhp);
                ps.setInt(12, maxmp);
                StringBuilder sps = new StringBuilder();
                for (int aRemainingSp : remainingSp) {
                    sps.append(aRemainingSp);
                    sps.append(",");
                }
                String sp = sps.toString();
                ps.setString(13, sp.substring(0, sp.length() - 1));
                ps.setInt(14, remainingAp);
                ps.setInt(15, gmLevel);
                ps.setInt(16, skinColor.getId());
                ps.setInt(17, gender);
                ps.setInt(18, job.getId());
                ps.setInt(19, hair);
                ps.setInt(20, face);
                if (map == null || (cashshop != null && cashshop.isOpened())) {
                    ps.setInt(21, mapid);
                } else {
                    if (map.getForcedReturnId() != 999999999) {
                        ps.setInt(21, map.getForcedReturnId());
                    } else {
                        ps.setInt(21, getHp() < 1 ? map.getReturnMapId() : map.getId());
                    }
                }
                ps.setInt(22, meso.get());
                ps.setInt(23, hpMpApUsed);
                if (map == null || map.getId() == 610020000 || map.getId() == 610020001) {
                    ps.setInt(24, 0);
                } else {
                    MaplePortal closest = map.findClosestSpawnpoint(getPosition());
                    if (closest != null) {
                        ps.setInt(24, closest.getId());
                    } else {
                        ps.setInt(24, 0);
                    }
                }
                if (party != null) {
                    ps.setInt(25, party.getId());
                } else {
                    ps.setInt(25, -1);
                }
                ps.setInt(26, buddylist.getCapacity());
                if (messenger != null) {
                    ps.setInt(27, messenger.getId());
                    ps.setInt(28, messengerposition);
                } else {
                    ps.setInt(27, 0);
                    ps.setInt(28, 4);
                }
                if (maplemount != null) {
                    ps.setInt(29, maplemount.getLevel());
                    ps.setInt(30, maplemount.getExp());
                    ps.setInt(31, maplemount.getTiredness());
                } else {
                    ps.setInt(29, 1);
                    ps.setInt(30, 0);
                    ps.setInt(31, 0);
                }
                for (int i = 1; i < 5; i++) {
                    ps.setInt(i + 31, getSlots(i));
                }

                monsterbook.saveCards(con, getId());

                ps.setInt(36, bookCover);
                ps.setInt(37, vanquisherStage);
                ps.setInt(38, dojoPoints);
                ps.setInt(39, dojoStage);
                ps.setInt(40, finishedDojoTutorial ? 1 : 0);
                ps.setInt(41, vanquisherKills);
                ps.setInt(42, matchcardwins);
                ps.setInt(43, matchcardlosses);
                ps.setInt(44, matchcardties);
                ps.setInt(45, omokwins);
                ps.setInt(46, omoklosses);
                ps.setInt(47, omokties);
                ps.setString(48, dataString);
                ps.setInt(49, fishingPoints);
                ps.setTimestamp(50, daily);
                ps.setInt(51, rebirths);
                ps.setInt(52, eventPoints);
                ps.setInt(53, rebirthPoints);
                ps.setInt(54, (occupation == null) ? -1 : occupation.getType().ordinal());
                ps.setInt(55, jumpQuestPoints);
                ps.setInt(56, chatType.ordinal());
                ps.setString(57, name);
                ps.setInt(58, id);

                int updateRows = ps.executeUpdate();
                if (updateRows < 1) {
                    throw new RuntimeException("Character not in database (" + id + ")");
                }

                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null) {
                        pets[i].saveToDb(con);
                    }
                }
            }

            deleteWhereCharacterId(con, "delete from achievements where player_id = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO achievements (`completed`, `player_id`, `achievement_name`, `killed_monster`, `casino_one`, `casino_two`) VALUES (?, ?, ?, ?, ?, ?)")) {
                for (Entry<String, Achievement> entry : achievements.entrySet()) {
                    String name = entry.getKey();
                    Achievement achievement = entry.getValue();
                    ps.setInt(1, achievement.isCompleted() ? 1 : 0);
                    ps.setInt(2, getId());
                    ps.setString(3, name);
                    ps.setInt(4, achievement.getMonstersKilled());
                    ps.setInt(5, achievement.isCasino1Completed() ? 1 : 0);
                    ps.setInt(6, achievement.isCasino2Completed() ? 1 : 0);
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
            }
            deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, id);
                for (Entry<Integer, MapleKeyBinding> keybinding : keymap.entrySet()) {
                    ps.setInt(2, keybinding.getKey());
                    ps.setInt(3, keybinding.getValue().getType());
                    ps.setInt(4, keybinding.getValue().getAction());
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
            }
            deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, getId());
                for (int i = 0; i < 5; i++) {
                    SkillMacro macro = skillMacros[i];
                    if (macro != null) {
                        ps.setInt(2, macro.getSkill1());
                        ps.setInt(3, macro.getSkill2());
                        ps.setInt(4, macro.getSkill3());
                        ps.setString(5, macro.getName());
                        ps.setInt(6, macro.getShout());
                        ps.setInt(7, i);
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
                con.commit();
            }

            List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
            for (MapleInventory iv : inventory) {
                for (Item item : iv.list()) {
                    itemsWithType.add(new Pair<>(item, iv.getType()));
                }
            }
            ItemFactory.INVENTORY.saveItems(itemsWithType, id, con);
            con.commit();

            relationship.save(con);

            deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO skills VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(2, id);
                for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
                    ps.setInt(1, skill.getKey().getId());
                    ps.setInt(3, skill.getValue().skillevel);
                    ps.setInt(4, skill.getValue().masterlevel);
                    ps.setLong(5, skill.getValue().expiration);
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
            }
            deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`, `portal`) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, id);
                for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                    if (savedLocations[savedLocationType.ordinal()] != null) {
                        ps.setString(2, savedLocationType.name());
                        ps.setInt(3, savedLocations[savedLocationType.ordinal()].getMapId());
                        ps.setInt(4, savedLocations[savedLocationType.ordinal()].getPortal());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 0)")) {
                for (int i = 0; i < getTrockSize(); i++) {
                    if (trockmaps.get(i) != 999999999) {
                        ps.setInt(1, getId());
                        ps.setInt(2, trockmaps.get(i));
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 1)")) {
                for (int i = 0; i < getVipTrockSize(); i++) {
                    if (viptrockmaps.get(i) != 999999999) {
                        ps.setInt(1, getId());
                        ps.setInt(2, viptrockmaps.get(i));
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`, `group`) VALUES (?, ?, 0, ?)")) {
                ps.setInt(1, id);
                for (BuddylistEntry entry : buddylist.getBuddies()) {
                    if (entry.isVisible()) {
                        ps.setInt(2, entry.getCharacterId());
                        ps.setString(3, entry.getGroup());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            deleteWhereCharacterId(con, "DELETE FROM area_info WHERE charid = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO area_info (id, charid, area, info) VALUES (DEFAULT, ?, ?, ?)")) {
                ps.setInt(1, id);
                for (Entry<Short, String> area : area_info.entrySet()) {
                    ps.setInt(2, area.getKey());
                    ps.setString(3, area.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement stmt = con.prepareStatement("SELECT count(*) AS total FROM cquest WHERE characterid = ?")) {
                stmt.setInt(1, getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt("total") > 0) {
                        deleteWhereCharacterId(con, "delete from cquest where characterid = ?");
                        deleteWhereCharacterId(con, "delete from cquestdata where qtableid = (select id from cquest where characterid = ?)");

                    }
                }
            }
            try (PreparedStatement stmt = con.prepareStatement("INSERT INTO cquest (questid, characterid, completed, completion) VALUES (?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                for (CQuestData data : customQuests.values()) {
                    stmt.setInt(1, data.getId());
                    stmt.setInt(2, getId());
                    stmt.setInt(3, data.isCompleted() ? 1 : 0);
                    stmt.setLong(4, data.getCompletion());
                    stmt.executeUpdate();
                    if (!data.isCompleted()) {
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                try (PreparedStatement stmt2 = con.prepareStatement("INSERT INTO cquestdata (qtableid, monsterid, kills) VALUES (?, ?, ?)")) {
                                    for (Entry<Integer, Pair<Integer, Integer>> entry : data.getToKill().getKills().entrySet()) {
                                        stmt2.setInt(1, rs.getInt(1));
                                        stmt2.setInt(2, entry.getKey());
                                        stmt2.setInt(3, entry.getValue().right);
                                        stmt2.addBatch();
                                    }
                                    stmt2.executeBatch();
                                }
                            }
                        }
                    }
                }
            }

            deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`) VALUES (DEFAULT, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                try (PreparedStatement pse = con.prepareStatement("INSERT INTO questprogress VALUES (DEFAULT, ?, ?, ?)")) {
                    try (PreparedStatement psf = con.prepareStatement("INSERT INTO medalmaps VALUES (DEFAULT, ?, ?)")) {
                        ps.setInt(1, id);
                        for (MapleQuestStatus q : quests.values()) {
                            ps.setInt(2, q.getQuest().getId());
                            ps.setInt(3, q.getStatus().getId());
                            ps.setInt(4, (int) (q.getCompletionTime() / 1000));
                            ps.setInt(5, q.getForfeited());
                            ps.executeUpdate();
                            try (ResultSet rs = ps.getGeneratedKeys()) {
                                rs.next();
                                for (int mob : q.getProgress().keySet()) {
                                    pse.setInt(1, rs.getInt(1));
                                    pse.setInt(2, mob);
                                    pse.setString(3, q.getProgress(mob));
                                    pse.addBatch();
                                }
                                for (int i = 0; i < q.getMedalMaps().size(); i++) {
                                    psf.setInt(1, rs.getInt(1));
                                    psf.setInt(2, q.getMedalMaps().get(i));
                                    psf.addBatch();
                                }
                                pse.executeBatch();
                                psf.executeBatch();
                            }
                        }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET gm = ? WHERE id = ?")) {
                ps.setInt(1, gmLevel);
                ps.setInt(2, client.getAccID());
                ps.executeUpdate();
            }

            if (cashshop != null) {
                cashshop.save(con);
            }
            if (storage != null) {
                storage.saveToDB(con);
            }
            con.commit();
            con.setAutoCommit(true);

            LOGGER.info("Saved Player({}) in {}s", getName(), ((System.currentTimeMillis() - beginTimestamp) / 1000d));
        } catch (SQLException | RuntimeException t) {
            LOGGER.error("Error while saving player '{}'", name, t);
        }
    }

    public void sendPolice(int greason, String reason, int duration) {
        announce(MaplePacketCreator.sendPolice(String.format("You have been blocked by the#b %s Police for %s.#k", "LucianMS", reason)));
        this.isbanned = true;
        TaskExecutor.createTask(new Runnable() {
            @Override
            public void run() {
                client.disconnect(false);
            }
        }, duration);
    }

    public void sendKeymap() {
        client.announce(MaplePacketCreator.getKeymap(keymap));
    }

    public void sendMacros() {
        // Always send the macro packet to fix a client side bug when switching characters.
        client.announce(MaplePacketCreator.getMacros(skillMacros));
    }

    public void sendNote(String to, String msg, byte fame) {
        try (Connection con = Server.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`, `fame`) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, to);
            ps.setString(2, this.getName());
            ps.setString(3, msg);
            ps.setLong(4, System.currentTimeMillis());
            ps.setByte(5, fame);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setBuddyCapacity(int capacity) {
        buddylist.setCapacity(capacity);
        client.announce(MaplePacketCreator.updateBuddyCapacity(capacity));
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }

    public void setDojoStart() {
        this.dojoMap = map;
        int stage = (map.getId() / 100) % 100;
        this.dojoFinish = System.currentTimeMillis() + (stage > 36 ? 15 : stage / 6 + 5) * 60000;
    }

    public void setRates() {
        MapleWorld w = Server.getWorld(world);

        expRate = w.getExpRate();
        mesoRate = w.getMesoRate();
        dropRate = w.getDropRate();

        if (occupation != null) {
            switch (occupation.getType()) {
                case Pharaoh:
                    expRate += 1;
                    mesoRate -= 1;
                    dropRate -= 1;
                    break;
                case Undead:
                    break;
                case Demon:
                    break;
                case Human:
                    dropRate += 1;
                    mesoRate += 1;
                    break;
            }
        }

        // lul just in case
        expRate = Math.max(1, expRate);
        mesoRate = Math.max(1, mesoRate);
        dropRate = Math.max(1, dropRate);
    }

    public void setFinishedDojoTutorial() {
        this.finishedDojoTutorial = true;
    }

    public void setHasMerchant(boolean set) {
        try (Connection con = client.getWorldServer().getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = ?")) {
            ps.setInt(1, set ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Unable to set has merchant {} for player '{}'", set, getName(), e);
            e.printStackTrace();
        }
        hasMerchant = set;
    }

    public void addMerchantMesos(int add) {
        try (Connection con = client.getWorldServer().getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, merchantmeso + add);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Unable to add merchant mesos {} to player '{}'", add, getName(), e);
            return;
        }
        merchantmeso += add;
    }

    public void setHp(int newhp, boolean silent) {
        int oldHp = hp;
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        this.hp = thp;
        if (!silent) {
            updatePartyMemberHP();
        }
        if (oldHp > hp && !isAlive()) {
            playerDead();
        }
    }

    public void setHpMp(int x) {
        setHp(x);
        setMp(x);
        updateSingleStat(MapleStat.HP, hp);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public void setMaxHp(int hp, boolean ap) {
        hp = Math.min(30000, hp);
        if (ap) {
            setHpMpApUsed(getHpMpApUsed() + 1);
        }
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp, boolean ap) {
        mp = Math.min(30000, mp);
        if (ap) {
            setHpMpApUsed(getHpMpApUsed() + 1);
        }
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setMiniGamePoints(MapleCharacter visitor, int winnerslot, boolean omok) {
        if (omok) {
            if (winnerslot == 1) {
                this.omokwins++;
                visitor.omoklosses++;
            } else if (winnerslot == 2) {
                visitor.omokwins++;
                this.omoklosses++;
            } else {
                this.omokties++;
                visitor.omokties++;
            }
        } else {
            if (winnerslot == 1) {
                this.matchcardwins++;
                visitor.matchcardlosses++;
            } else if (winnerslot == 2) {
                visitor.matchcardwins++;
                this.matchcardlosses++;
            } else {
                this.matchcardties++;
                visitor.matchcardties++;
            }
        }
    }

    public void changeName(String name) {
        this.name = name;
        try (Connection con = client.getWorldServer().getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE `characters` SET `name` = ? WHERE `id` = ?")) {
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRemainingSp(int remainingSp, int skillbook) {
        this.remainingSp[skillbook] = remainingSp;
    }

    public byte getSlots(int type) {
        return type == MapleInventoryType.CASH.getType() ? 96 : inventory[type].getSlotLimit();
    }

    public boolean gainSlots(int type, int slots) {
        return gainSlots(type, slots, true);
    }

    public boolean gainSlots(int type, int slots, boolean update) {
        slots += inventory[type].getSlotLimit();
        if (slots <= 96) {
            inventory[type].setSlotLimit(slots);

            saveToDB();
            if (update) {
                client.announce(MaplePacketCreator.updateInventorySlotLimit(type, slots));
            }

            return true;
        }

        return false;
    }

    public void shiftPetsRight() {
        if (pets[2] == null) {
            pets[2] = pets[1];
            pets[1] = pets[0];
            pets[0] = null;
        }
    }

    public void showDojoClock() {
        int stage = (map.getId() / 100) % 100;
        long time;
        if (stage % 6 == 1) {
            time = (stage > 36 ? 15 : stage / 6 + 5) * 60;
        } else {
            time = (dojoFinish - System.currentTimeMillis()) / 1000;
        }
        if (stage % 6 > 0) {
            client.announce(MaplePacketCreator.getClock((int) time));
        }
        boolean rightmap = true;
        int clockid = (dojoMap.getId() / 100) % 100;
        if (map.getId() > clockid / 6 * 6 + 6 || map.getId() < clockid / 6 * 6) {
            rightmap = false;
        }
        final boolean rightMap = rightmap; // lol
        TaskExecutor.createTask(new Runnable() {
            @Override
            public void run() {
                if (rightMap) {
                    client.getPlayer().changeMap(client.getChannelServer().getMap(925020000));
                }
            }
        }, time * 1000 + 3000); // let the TIMES UP display for 3 seconds, then
    }

    public void showNote() {
        try (Connection con = client.getWorldServer().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM notes WHERE `to`=? AND `deleted` = 0", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ps.setString(1, this.getName());
            try (ResultSet rs = ps.executeQuery()) {
                rs.last();
                int count = rs.getRow();
                rs.first();
                client.announce(MaplePacketCreator.showNotes(rs, count));
            }
        } catch (SQLException e) {
        }
    }

    private void silentEnforceMaxHpMp() {
        setMp(getMp());
        setHp(getHp(), true);
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
        }
    }

    public void silentPartyUpdate() {
        if (party != null) {
            Server.getWorld(world).updateParty(party.getId(), PartyOperation.SILENT_UPDATE, getMPC());
        }
    }

    public boolean skillisCooling(int skillId) {
        return coolDowns.containsKey(skillId);
    }

    public void startFullnessSchedule(final int decrease, final MaplePet pet, int petSlot) {
//        fullnessSchedule[petSlot] = TaskExecutor.createRepeatingTask(new Runnable() {
//            @Override
//            public void run() {
//                int newFullness = pet.getFullness() - decrease;
//                if (newFullness <= 5) {
//                    pet.setFullness(15);
//                    unequipPet(pet, true);
//                } else {
//                    pet.setFullness(newFullness);
//                    Item petz = getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
//                    if (petz != null) {
//                        forceUpdateItem(petz);
//                    }
//                }
//                pet.saveToDb();
//            }
//        }, 180000, 18000);
    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, 30000);
    }

    public void startMapEffect(String msg, int itemId, int duration) {
        final MapleMapEffect mapEffect = new MapleMapEffect(msg, itemId);
        getClient().announce(mapEffect.makeStartData());
        TaskExecutor.createTask(() -> getClient().announce(mapEffect.makeDestroyData()), duration);
    }

    public void stopControllingMonster(MapleMonster monster) {
        controlled.remove(monster);
    }

    public void unequipAllPets() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                unequipPet(pets[i], true);
            }
        }
    }

    public void unequipPet(MaplePet pet, boolean shift_left) {
        unequipPet(pet, shift_left, false);
    }

    public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger) {
        if (this.getPet(this.getPetIndex(pet)) != null) {
            this.getPet(this.getPetIndex(pet)).setSummoned(false);
            try (Connection con = getClient().getWorldServer().getConnection()) {
                this.getPet(this.getPetIndex(pet)).saveToDb(con);
            } catch (SQLException ignore) {
            }
        }
        cancelFullnessSchedule(getPetIndex(pet));
        getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pet, true, hunger), true);
        client.announce(MaplePacketCreator.petStatUpdate(this));
        client.announce(MaplePacketCreator.enableActions());
        removePet(pet, shift_left);
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
    }

    public void updatePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapId() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = Server.getWorld(world).getChannel(channel).getPlayerStorage().find(p -> p.getName().equalsIgnoreCase(partychar.getName()));
                    if (other != null) {
                        other.client.announce(MaplePacketCreator.updatePartyMemberHP(getId(), this.hp, maxhp));
                    }
                }
            }
        }
    }

    public String getQuestInfo(int quest) {
        MapleQuestStatus qs = getQuest(MapleQuest.getInstance(quest));
        return qs.getInfo();
    }

    public void updateQuestInfo(int quest, String info) {
        MapleQuest q = MapleQuest.getInstance(quest);
        MapleQuestStatus qs = getQuest(q);
        qs.setInfo(info);

        quests.put(q.getId(), qs);

        announce(MaplePacketCreator.updateQuest(qs, false));
        if (qs.getQuest().getInfoNumber() > 0) {
            announce(MaplePacketCreator.updateQuest(qs, true));
        }
        announce(MaplePacketCreator.updateQuestInfo(qs.getQuest().getId(), qs.getNpc()));
    }

    public void updateQuest(MapleQuestStatus quest) {
        quests.put(quest.getQuestID(), quest);
        if (quest.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
            announce(MaplePacketCreator.updateQuest(quest, false));
            if (quest.getQuest().getInfoNumber() > 0) {
                announce(MaplePacketCreator.updateQuest(quest, true));
            }
            announce(MaplePacketCreator.updateQuestInfo(quest.getQuest().getId(), quest.getNpc()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
            announce(MaplePacketCreator.completeQuest(quest.getQuest().getId(), quest.getCompletionTime()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)) {
            announce(MaplePacketCreator.updateQuest(quest, false));
            if (quest.getQuest().getInfoNumber() > 0) {
                announce(MaplePacketCreator.updateQuest(quest, true));
            }
        }
    }

    public void questTimeLimit(final MapleQuest quest, int time) {
        Task task = TaskExecutor.createTask(new Runnable() {
            @Override
            public void run() {
                announce(MaplePacketCreator.questExpire(quest.getId()));
                MapleQuestStatus newStatus = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
                newStatus.setForfeited(getQuest(quest).getForfeited() + 1);
                updateQuest(newStatus);
            }
        }, time * 60 * 1000);
        announce(MaplePacketCreator.addQuestTimeLimit(quest.getId(), time * 60 * 1000));
        tasks.add(task);
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    private void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
        announce(MaplePacketCreator.updatePlayerStats(Collections.singletonList(new Pair<>(stat, newval)), itemReaction, this));
    }

    public void announce(final byte[] packet) {
        client.announce(packet);
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    @Override
    public void setObjectId(int id) {
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (!this.isHidden() || client.getPlayer().getGMLevel() >= this.getHidingLevel()) {
            client.announce(MaplePacketCreator.spawnPlayerMapobject(this));
        }

        if (this.isHidden()) {
            List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<>(MapleBuffStat.DARKSIGHT, 0));
            getMap().broadcastGMMessage(this, MaplePacketCreator.giveForeignBuff(getId(), dsstat), false);
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    public int getLinkedLevel() {
        return linkedLevel;
    }

    public String getLinkedName() {
        return linkedName;
    }

    public CashShop getCashShop() {
        return cashshop;
    }

    public void portalDelay(long delay) {
        this.portaldelay = System.currentTimeMillis() + delay;
    }

    public long portalDelay() {
        return portaldelay;
    }

    public void blockPortal(String scriptName) {
        if (!blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.add(scriptName);
            client.announce(MaplePacketCreator.enableActions());
        }
    }

    public void unblockPortal(String scriptName) {
        if (blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.remove(scriptName);
        }
    }

    public List<String> getBlockedPortals() {
        return blockedPortals;
    }

    public boolean containsAreaInfo(int area, String info) {
        Short area_ = (short) area;
        if (area_info.containsKey(area_)) {
            return area_info.get(area_).contains(info);
        }
        return false;
    }

    public void updateAreaInfo(int area, String info) {
        area_info.put((short) area, info);
        announce(MaplePacketCreator.updateAreaInfo(area, info));
    }

    public GProperties<Boolean> getToggles() {
        return toggles;
    }

    public String getAreaInfo(int area) {
        return area_info.get((short) area);
    }

    public Map<Short, String> getAreaInfos() {
        return area_info;
    }

    public void block(int reason, int days, String desc) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        Timestamp TS = new Timestamp(cal.getTimeInMillis());
        try (Connection con = client.getWorldServer().getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banreason = ?, tempban = ?, greason = ? WHERE id = ?")) {
            ps.setString(1, desc);
            ps.setTimestamp(2, TS);
            ps.setInt(3, reason);
            ps.setInt(4, accountid);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBanned() {
        return isbanned;
    }

    public List<Integer> getTrockMaps() {
        return trockmaps;
    }

    public List<Integer> getVipTrockMaps() {
        return viptrockmaps;
    }

    public int getTrockSize() {
        int ret = trockmaps.indexOf(999999999);
        if (ret == -1) {
            ret = 5;
        }

        return ret;
    }

    public void deleteFromTrocks(int map) {
        trockmaps.remove(Integer.valueOf(map));
        while (trockmaps.size() < 10) {
            trockmaps.add(999999999);
        }
    }

    public void addTrockMap() {
        int index = trockmaps.indexOf(999999999);
        if (index != -1) {
            trockmaps.set(index, getMapId());
        }
    }

    public boolean isTrockMap(int id) {
        int index = trockmaps.indexOf(id);
        return index != -1;

    }

    public int getVipTrockSize() {
        int ret = viptrockmaps.indexOf(999999999);

        if (ret == -1) {
            ret = 10;
        }

        return ret;
    }

    public void deleteFromVipTrocks(int map) {
        viptrockmaps.remove(Integer.valueOf(map));
        while (viptrockmaps.size() < 10) {
            viptrockmaps.add(999999999);
        }
    }

    public void addVipTrockMap() {
        int index = viptrockmaps.indexOf(999999999);
        if (index != -1) {
            viptrockmaps.set(index, getMapId());
        }
    }

    public boolean isVipTrockMap(int id) {
        int index = viptrockmaps.indexOf(id);
        return index != -1;

    }

    public byte getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = (byte) team;
    }

    public MapleOla getOla() {
        return ola;
    }

    public void setOla(MapleOla ola) {
        this.ola = ola;
    }

    public MapleFitness getFitness() {
        return fitness;
    }

    public void setFitness(MapleFitness fit) {
        this.fitness = fit;
    }

    public long getLastSnowballAttack() {
        return snowballattack;
    }

    public void setLastSnowballAttack(long time) {
        this.snowballattack = time;
    }

    public int getCarnivalPoints() {
        return carnivalPoints;
    }

    public void setCarnivalPoints(int carnivalPoints) {
        this.carnivalPoints = carnivalPoints;
    }

    public int getCP() {
        return carnivalPoints;
    }

    public int getObtainedCP() {
        return obtainedcp;
    }

    public void setObtainedCP(int cp) {
        this.obtainedcp = cp;
    }

    public void addCP(int cp) {
        this.carnivalPoints += cp;
        this.obtainedcp += cp;
    }

    public void useCP(int cp) {
        this.carnivalPoints -= cp;
    }

    public int getAndRemoveCP() {
        int removed = Math.min(10, carnivalPoints);
        carnivalPoints -= removed;
        return removed;
    }

    public void scheduleSpiritPendant() {
        if (spiritPendantTask == null) {
            spiritPendantTask = TaskExecutor.createTask(new Runnable() {
                @Override
                public void run() {
                    if (pendantExp < 3) {
                        pendantExp++;
                        spiritPendantTask = null;
                        message("Pendant of the Spirit has been equipped for " + pendantExp + " hour(s), you will now receive " + pendantExp + "0% bonus exp.");
                        scheduleSpiritPendant();
                    }
                }
            }, 3600000); // 1 hour
        }
    }

    public void unequipPendantOfSpirit() {
        if (spiritPendantTask != null) {
            spiritPendantTask.cancel();
            spiritPendantTask = null;
        }
        pendantExp = 0;
    }

    public void increaseEquipExp(int mobexp) {
        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        for (Item item : getInventory(MapleInventoryType.EQUIPPED).list()) {
            Equip nEquip = (Equip) item;
            String itemName = mii.getName(nEquip.getItemId());
            if (itemName == null) {
                continue;
            }

            if ((itemName.contains("Reverse") && nEquip.getItemLevel() < 4) || itemName.contains("Timeless") && nEquip.getItemLevel() < 6) {
                nEquip.gainItemExp(client, mobexp, itemName.contains("Timeless"));
            }
        }
    }

    public Map<String, MapleEvents> getEvents() {
        return events;
    }

    public Map<String, Achievement> getAchievements() {
        return achievements;
    }

    public Achievement getAchievement(String achievementName) {
        Achievement ret = achievements.get(achievementName);
        if (ret == null) {
            ret = new Achievement();
            achievements.put(achievementName, ret);
        }
        return ret;
    }

    public PartyQuest getPartyQuest() {
        return partyQuest;
    }

    public void setPartyQuest(PartyQuest pq) {
        this.partyQuest = pq;
    }

    public final void empty(final boolean remove) {
        if (dragonBloodTask != null) {
            dragonBloodTask.cancel();
            dragonBloodTask = null;
        }
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel();
            hpDecreaseTask = null;
        }
        if (beholderHealingTask != null) {
            beholderHealingTask.cancel();
            beholderHealingTask = null;
        }
        if (beholderBuffTask != null) {
            beholderBuffTask.cancel();
            beholderBuffTask = null;
        }
        if (berserkTask != null) {
            berserkTask.cancel();
            berserkTask = null;
        }
        if (recoveryTask != null) {
            recoveryTask.cancel();
            recoveryTask = null;
        }
        cancelExpirationTask();
        new ArrayList<>(tasks).forEach(Task::cancel);
        tasks.clear();
        if (maplemount != null) {
            maplemount.empty();
            maplemount = null;
        }
        if (remove) {
            partyQuest = null;
            events = null;
            mpc = null;
            mgc = null;
            events = null;
            party = null;
            family = null;
            client = null;
            map = null;
            tasks = null;
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean getWhiteChat() {
        return isGM() && whiteChat;
    }

    public void toggleWhiteChat() {
        whiteChat = !whiteChat;
    }

    public String getPartyQuestItems() {
        return dataString;
    }

    public boolean gotPartyQuestItem(String partyquestchar) {
        return dataString.contains(partyquestchar);
    }

    public void removePartyQuestItem(String letter) {
        if (gotPartyQuestItem(letter)) {
            dataString = dataString.substring(0, dataString.indexOf(letter)) + dataString.substring(dataString.indexOf(letter) + letter.length());
        }
    }

    public void setPartyQuestItemObtained(String partyquestchar) {
        if (!dataString.contains(partyquestchar)) {
            this.dataString += partyquestchar;
        }
    }

    public void createDragon() {
        dragon = new MapleDragon(this);
    }

    public MapleDragon getDragon() {
        return dragon;
    }

    public void setDragon(MapleDragon dragon) {
        this.dragon = dragon;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public int getFishingPoints() {
        return fishingPoints;
    }

    public void setFishingPoints(int fishingPoints) {
        this.fishingPoints = fishingPoints;
    }

    public int getJumpQuestPoints() {
        return jumpQuestPoints;
    }

    public void setJumpQuestPoints(int jumpQuestPoints) {
        this.jumpQuestPoints = jumpQuestPoints;
    }

    public Task getFishingTask() {
        return fishingTask;
    }

    public void runFishingTask() {
        fishingTask = TaskExecutor.createTask(new Runnable() {
            @Override
            public void run() {
                if (ArrayUtil.contains(getChair(), FISHING_CHAIRS) && ArrayUtil.contains(getMapId(), FISHING_MAPS)) {
                    int receival = 0;
                    if (getChair() == FISHING_CHAIRS[0]) {
                        receival = 1;
                        dropMessage(5, "<Fisherman>Bull's Eye! Fish net with a catch!");
                    } else if (getChair() == FISHING_CHAIRS[1]) {
                        receival = 2;
                        dropMessage(5, "<Fisherman>Bull's Eye! Fish net with a big catch!");
                    } else if (getChair() == FISHING_CHAIRS[2]) {
                        receival = 3;
                        dropMessage(5, "<Fisherman>Bull's Eye! Fish net with a huge catch!");
                    }
                    MapleInventoryManipulator.addById(getClient(), FRECEIVAL_ITEM, (short) receival);

                    runFishingTask();
                }
            }

        }, 300000);
    }

    public int getEventPoints() {
        return eventPoints;
    }

    public void setEventPoints(int eventPoints) {
        this.eventPoints = eventPoints;
    }

    public PlayerTitles getTitleManager() {
        if (this.title == null) {
            title = new PlayerTitles(this);
        }
        return this.title;
    }

    public void doRebirth() {
        rebirths += 1;

        job = MapleJob.BEGINNER;
        updateSingleStat(MapleStat.JOB, job.getId());

        level = 1;

        exp.set(0);
        updateSingleStat(MapleStat.EXP, 0);

        setRebirthPoints(getRebirthPoints() + 50);
        dropMessage("You have received 50 rebirth points");

        announce(MaplePacketCreator.showEffect("breakthrough/One")); // effect
        announce(MaplePacketCreator.showEffect("breakthrough/Two")); // effect
        announce(MaplePacketCreator.showEffect("breakthrough/Three")); // effect
        announce(MaplePacketCreator.showEffect("breakthrough/Four")); // effect
        announce(MaplePacketCreator.showEffect("breakthrough/Five")); // effect
        announce(MaplePacketCreator.showEffect("breakthrough/Ten")); // effect
        announce(MaplePacketCreator.trembleEffect(0, 0)); // shake screen

        // damage all monsters in the screen
        for (MapleMapObject object : getMap().getMapObjectsInRange(getPosition(), 100000, Collections.singletonList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) object;
            int damage = monster.getMaxHp();
            getMap().broadcastMessage(MaplePacketCreator.damageMonster(monster.getObjectId(), damage));
            getMap().damageMonster(this, monster, damage);
        }
    }

    public boolean addPoints(String pointType, int amount) {
        switch (pointType) {
            case "jq": // jump quest points
                setJumpQuestPoints(getJumpQuestPoints() + amount);
                return true;
            case "fp": // fishing points
                setFishingPoints(getFishingPoints() + amount);
                return true;
            case "ep": // event points
                setEventPoints(getEventPoints() + amount);
                return true;
            case "dp": // donor points
                getClient().setDonationPoints(getClient().getDonationPoints() + amount);
                return true;
            case "vp": // vote points
                getClient().addVotePoints(amount);
                return true;
            case "nx": // nx cash
                getCashShop().gainCash(1, amount);
                return true;
            case "ap": // ability points
                setRemainingAp(amount);
                updateSingleStat(MapleStat.AVAILABLEAP, getRemainingAp());
            case "sp": // skill points
                setRemainingSp(amount);
                updateSingleStat(MapleStat.AVAILABLESP, getRemainingSp());
                return true;
            case "rp":
                setRebirthPoints(getRebirthPoints() + amount);
                return true;
        }
        return false;
    }

    public boolean tempban(Timestamp tempban) {
        try (Connection con = client.getWorldServer().getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET tempban = ? WHERE id = ?")) {
            ps.setTimestamp(1, tempban);
            ps.setInt(2, getAccountID());
            return ps.execute();
        } catch (SQLException e) {
            LOGGER.error("Unable to temp ban player '{}' timestamp '{}'", getName(), tempban.getTime());
        }
        return false;
    }

    public boolean canDaily() {
        return (System.currentTimeMillis()) >= daily.getTime() + 86400000;
    }

    public void writeDaily() {
        this.daily = new Timestamp(System.currentTimeMillis());
    }

    public int getRebirths() {
        return rebirths;
    }

    public void setRebirths(int rebirths) {
        this.rebirths = rebirths;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getKillType() {
        return killType;
    }

    public void setKillType(int killType) {
        this.killType = killType;
    }

    public Arcade getArcade() {
        return arcade;
    }

    public void setArcade(Arcade arcade) {
        this.arcade = arcade;
    }

    public Map<Integer, CQuestData> getCustomQuests() {
        return customQuests;
    }

    public CQuestData getCustomQuest(int id) {
        return customQuests.get(id);
    }

    public int getHidingLevel() {
        return this.hidingLevel;
    }

    public void setHidingLevel(int hidingLevel) {
        this.hidingLevel = hidingLevel;
    }

    public RPSGame getRPSGame() {
        return RPSGame;
    }

    public void setRPSGame(RPSGame RPSGame) {
        this.RPSGame = RPSGame;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(Occupation occupation) {
        this.occupation = occupation;
        setRates();
    }

    public SpamTracker.SpamData getSpamTracker(SpamTracker.SpamOperation operation) {
        return spamTracker.getData(operation);
    }

    public Cheater getCheater() {
        return cheater;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public FakePlayer getFakePlayer() {
        return fakePlayer;
    }

    public void setFakePlayer(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public void setChatType(ChatType chatType) {
        this.chatType = chatType;
    }

    public List<GenericEvent> getGenericEvents() {
        return new ArrayList<>(genericEvents);
    }

    public boolean addGenericEvent(GenericEvent event) {
        if (genericEvents.contains(event)) {
            return false;
        }
        return genericEvents.add(event);
    }

    public void removeGenericEvent(GenericEvent event) {
        genericEvents.remove(event);
    }

    public int getRiceCakes() {
        return riceCakes;
    }

    public void setRiceCakes(int riceCakes) {
        this.riceCakes = riceCakes;
    }

    public int getRebirthPoints() {
        return rebirthPoints;
    }

    public void setRebirthPoints(int rebirthPoints) {
        this.rebirthPoints = rebirthPoints;
    }

    public long getLastEmergency() {
        return lastEmergency;
    }

    public void setLastEmergency(long lastEmergency) {
        this.lastEmergency = lastEmergency;
    }

    public boolean isImmortal() {
        return System.currentTimeMillis() + 60000 >= immortalTimestamp && immortalTimestamp > 0;
    }

    public long getImmortalTimestamp() {
        return immortalTimestamp;
    }

    public void setImmortalTimestamp(long immortalTimestamp) {
        this.immortalTimestamp = immortalTimestamp;
    }

    public boolean isAutorebirthing() {
        return autorebirthing;
    }

    public void setAutorebirthing(boolean autorebirthing) {
        this.autorebirthing = autorebirthing;
    }

    public boolean isEyeScannersEquiped() {
        return eyeScannersEquiped;
    }

    public void setEyeScannersEquiped(boolean eyeScannersEquiped) {
        this.eyeScannersEquiped = eyeScannersEquiped;
    }

    public enum FameStatus {

        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public static class CancelCooldownAction implements Runnable {

        private int skillId;
        private WeakReference<MapleCharacter> target;

        public CancelCooldownAction(MapleCharacter target, int skillId) {
            this.target = new WeakReference<>(target);
            this.skillId = skillId;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.removeCooldown(skillId);
                realTarget.client.announce(MaplePacketCreator.skillCooldown(skillId, 0));
            }
        }
    }

    private static class MapleBuffStatValueHolder {

        public MapleStatEffect effect;
        public long startTime;
        public int value;
        public Task task;

        public MapleBuffStatValueHolder(MapleStatEffect effect, long startTime, Task task, int value) {
            super();
            this.effect = effect;
            this.startTime = startTime;
            this.task = task;
            this.value = value;
        }
    }

    public static class MapleCoolDownValueHolder {

        public int skillId;
        public long startTime, length;
        public Task task;

        public MapleCoolDownValueHolder(int skillId, long startTime, long length, Task task) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
            this.task = task;
        }
    }

    public static class SkillEntry {

        public int masterlevel;
        public byte skillevel;
        public long expiration;

        public SkillEntry(byte skillevel, int masterlevel, long expiration) {
            this.skillevel = skillevel;
            this.masterlevel = masterlevel;
            this.expiration = expiration;
        }

        @Override
        public String toString() {
            return skillevel + ":" + masterlevel;
        }
    }
}
