package tools;

import com.lucianms.client.MapleCharacter;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author izarooni
 */
public interface PacketAnnouncer {

    /**
     * This assumes a new collection is created per call.
     * <p>
     * Do not return collections directly as the collection is cleared afterwards for garbage collection.
     * </p>
     */
    Collection<MapleCharacter> getPlayers();

    default Collection<MapleCharacter> getPlayers(Predicate<MapleCharacter> condition) {
        // lul pls dont judge me
        Collection<MapleCharacter> players = getPlayers();
        List<MapleCharacter> filtered = players.stream().filter(condition).collect(Collectors.toList());
        players.clear();
        return filtered;
    }

    default void forEachPlayer(Consumer<MapleCharacter> action) {
        Collection<MapleCharacter> players = getPlayers();
        players.forEach(action);
        players.clear();
    }

    /**
     * Sends a packet to all players in the collection provided {@link PacketAnnouncer#getPlayers()}
     *
     * @see MapleCharacter#sendMessage(int, String, Object...)
     */
    default void sendMessage(int type, String content, Object... args) {
        Collection<MapleCharacter> players = getPlayers();
        players.forEach(p -> p.sendMessage(type, content, args));
        players.clear();
    }

    /**
     * Sends a packet to a collection of players if the predicate condition result is tested true
     *
     * @see MapleCharacter#sendMessage(int, String, Object...)
     */
    default void sendMessageIf(Predicate<MapleCharacter> cond, int type, String content, Object... args) {
        Collection<MapleCharacter> players = getPlayers();
        players.stream().filter(cond).forEach(p -> p.sendMessage(type, content, args));
        players.clear();
    }

    /**
     * Sends a packet to all players in the collection provided {@link PacketAnnouncer#getPlayers()}
     */
    default void sendPacket(byte[] packet) {
        Collection<MapleCharacter> players = getPlayers();
        players.forEach(p -> p.announce(packet));
        players.clear();
    }

    /**
     * Sends a packet to (1) all players if the specified player is not hidden, (2) the receiving players GM level is
     * equal to or higher than that of the specified player
     *
     * @param player the player that may be hidden
     */
    default void sendPacketCheckHidden(MapleCharacter player, byte[] packet) {
        Collection<MapleCharacter> players = getPlayers();
        players.stream().filter(p -> !player.isHidden() || p.getGMLevel() >= player.getGMLevel()).forEach(p -> p.announce(packet));
        players.clear();
    }

    /**
     * Sends a packet to (1) all players if the specified player is not hidden, (2) the receiving players GM level is
     * equal to or higher than that of the specified player and (3) receiving player's is not the specified player
     *
     * @param player the player that should not receive the packet and will be used as the subject in the above
     *               conditions
     */
    default void sendPacketCheckHiddenExclude(MapleCharacter player, byte[] packet) {
        Collection<MapleCharacter> players = getPlayers();
        players.stream().filter(p -> player.getId() != p.getId() && (!player.isHidden() || p.getGMLevel() >= player.getGMLevel())).forEach(p -> p.announce(packet));
        players.clear();
    }

    /**
     * Sends a packet to the collection of players excluding the single specified player
     */
    default void sendPacketExclude(byte[] packet, MapleCharacter exclude) {
        Collection<MapleCharacter> players = getPlayers();
        players.stream().filter(p -> p.getId() != exclude.getId()).forEach(p -> p.announce(packet));
        players.clear();
    }

    /**
     * Sends a packet to the collection of players if the predicate condition result is tested true
     */
    default void sendPacketIf(byte[] packet, Predicate<MapleCharacter> cond) {
        Collection<MapleCharacter> players = getPlayers();
        players.stream().filter(cond).forEach(p -> p.announce(packet));
        players.clear();
    }
}
