package com.lucianms.discord.handlers;

import com.lucianms.client.inventory.MapleInventoryType;
import com.lucianms.constants.ItemConstants;
import com.lucianms.discord.DiscordConnection;
import com.lucianms.discord.Headers;
import com.lucianms.nio.receive.MaplePacketReader;
import com.lucianms.nio.send.MaplePacketWriter;
import com.lucianms.server.MapleItemInformationProvider;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author izarooni
 */
public class SearchRequest extends DiscordRequest {

    private static final int SearchLimit = 2000;

    @Override
    public void handle(MaplePacketReader reader) {
        final long channelID = reader.readLong();
        String type = reader.readMapleAsciiString();
        String search = reader.readMapleAsciiString().toLowerCase().trim();

        MaplePacketWriter w = new MaplePacketWriter();
        w.write(Headers.Search.value);
        w.writeLong(channelID);

        MapleData data;
        MapleDataProvider dataProvider = MapleDataProviderFactory.getWZ(new File(System.getProperty("wzpath") + "/" + "String.wz"));
        if (type.equalsIgnoreCase("NPC") || type.equalsIgnoreCase("NPCS")) {
            List<String> retNpcs = new ArrayList<>();
            data = dataProvider.getData("Npc.img");
            List<Pair<Integer, String>> npcPairList = new LinkedList<>();
            for (MapleData npcIdData : data.getChildren()) {
                int npcIdFromData = Integer.parseInt(npcIdData.getName());
                String npcNameFromData = MapleDataTool.getString(npcIdData.getChildByPath("name"), "NO-NAME");
                npcPairList.add(new Pair<>(npcIdFromData, npcNameFromData));
            }
            for (Pair<Integer, String> npcPair : npcPairList) {
                if (npcPair.getRight().toLowerCase().contains(search)) {
                    retNpcs.add(npcPair.getLeft() + " - " + npcPair.getRight());
                }
            }
            EncodeData(w, retNpcs);
            retNpcs.clear();
            npcPairList.clear();
        } else if (type.equalsIgnoreCase("MAP") || type.equalsIgnoreCase("MAPS")) {
            List<String> retMaps = new ArrayList<>();
            data = dataProvider.getData("Map.img");
            List<Pair<Integer, String>> mapPairList = new LinkedList<>();
            for (MapleData mapAreaData : data.getChildren()) {
                for (MapleData mapIdData : mapAreaData.getChildren()) {
                    int mapIdFromData = Integer.parseInt(mapIdData.getName());
                    String mapNameFromData = MapleDataTool.getString(mapIdData.getChildByPath("streetName"), "NO-NAME") + " - " + MapleDataTool.getString(mapIdData.getChildByPath("mapName"), "NO-NAME");
                    mapPairList.add(new Pair<>(mapIdFromData, mapNameFromData));
                }
            }
            for (Pair<Integer, String> mapPair : mapPairList) {
                if (mapPair.getLeft().toString().equals(search) || mapPair.getRight().toLowerCase().contains(search)) {
                    retMaps.add(mapPair.getLeft() + " - " + mapPair.getRight());
                }
            }
            EncodeData(w, retMaps);
            retMaps.clear();
            mapPairList.clear();
        } else if (type.equalsIgnoreCase("MOB") || type.equalsIgnoreCase("MOBS") || type.equalsIgnoreCase("MONSTER") || type.equalsIgnoreCase("MONSTERS")) {
            List<String> retMobs = new ArrayList<>();
            data = dataProvider.getData("Mob.img");
            List<Pair<Integer, String>> mobPairList = new LinkedList<>();
            for (MapleData mobIdData : data.getChildren()) {
                int mobIdFromData = Integer.parseInt(mobIdData.getName());
                String mobNameFromData = MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME");
                mobPairList.add(new Pair<>(mobIdFromData, mobNameFromData));
            }
            for (Pair<Integer, String> mobPair : mobPairList) {
                if (mobPair.getRight().toLowerCase().contains(search)) {
                    retMobs.add(mobPair.getLeft() + " - " + mobPair.getRight());
                }
            }
            EncodeData(w, retMobs);
            retMobs.clear();
            mobPairList.clear();
        } else if (type.equalsIgnoreCase("EQUIP") || type.equalsIgnoreCase("ITEM") || type.equalsIgnoreCase("ITEMS")) {
            List<String> retItems = new ArrayList<>();
            for (Pair<Integer, String> itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
                if (type.equalsIgnoreCase("EQUIP") && ItemConstants.getInventoryType(itemPair.getLeft()) != MapleInventoryType.EQUIP) {
                    continue;
                }
                if (itemPair.getRight().toLowerCase().contains(search)) {
                    retItems.add(itemPair.getLeft() + " - " + itemPair.getRight());
                }
            }
            EncodeData(w, retItems);
            retItems.clear();
        } else {
            w.writeInt(-1);
            w.writeMapleString("You must specify a search type");
        }
        DiscordConnection.sendPacket(w.getPacket());
        System.gc();
    }

    private void EncodeData(MaplePacketWriter w, List<String> array) {
        if (array.isEmpty()) {
            w.writeInt(-1);
            w.writeMapleString("No results found");
        } else if (array.stream().mapToInt(String::length).sum() >= SearchLimit) {
            w.writeInt(-1);
            w.writeMapleString("There are too many results to display. Please be more specific with your search query");
        } else {
            w.writeInt(array.size());
            array.forEach(w::writeMapleString);
        }
    }
}
