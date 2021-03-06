package net.new_liberty.votesuite;

import net.new_liberty.nlcore.database.DB;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents someone who votes.
 */
public class Voter {
    /**
     * The VoteSuite plugin.
     */
    private final VoteSuite plugin;

    /**
     * The player this Voter represents.
     */
    private final String player;

    /**
     * C'tor
     *
     * @param player
     */
    public Voter(VoteSuite plugin, String player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Gets the vote services this voter is missing.
     *
     * @return
     */
    public Set<VoteService> getMissingServices() {
        List<String> voteSvcIds = DB.i().query("SELECT service FROM votes_recent WHERE name = ?", new ColumnListHandler<String>(), player);
        Set<VoteService> missingServices = plugin.getServices();
        for (String voteSvcId : voteSvcIds) {
            VoteService rm = null;
            for (VoteService svc : missingServices) {
                if (svc.getId().equals(voteSvcId)) {
                    rm = svc;
                    break;
                }
            }
            missingServices.remove(rm);
        }
        return missingServices;
    }

    /**
     * Clears the recent votes of this Voter.
     */
    public void clearRecentVotes() {
        DB.i().update("DELETE FROM votes_recent WHERE name = ?", player);
    }

    /**
     * Counts the number of votes tallied up for this player in the past 24
     * hours.
     *
     * @return
     */
    public int countDayVotes() {
        String query = "SELECT COUNT(*) FROM votes WHERE name = ? AND time > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)";
        Object ret = DB.i().get(query, this, player);
        if (ret != null) {
            return ((Number) ret).intValue();
        }
        return 0;
    }

    /**
     * Gets this Voter's home.
     *
     * @return
     */
    public Location getHome() {
        String query = "SELECT * FROM votes_homes WHERE name = ?";
        return DB.i().query(query, new ResultSetHandler<Location>() {
            @Override
            public Location handle(ResultSet rs) throws SQLException {
                if (!rs.next()) {
                    return null;
                }

                String worldStr = rs.getString("world");
                World w = Bukkit.getWorld(worldStr);
                if (w == null) {
                    return null;
                }
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");

                return new Location(w, x, y, z, yaw, pitch);
            }
        }, player);
    }

    /**
     * Sets this Voter's home.
     *
     * @param loc
     */
    public void setHome(Location loc) {
        String query = "INSERT INTO votes_homes (name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?";
        DB.i().update(query, player, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(),
                loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
}
