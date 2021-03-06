package net.new_liberty.enderchestprotect;

import net.new_liberty.util.InventorySerializer;
import net.new_liberty.nlcore.database.DB;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.dbutils.ResultSetHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

/**
 * Manages Ender Chests.
 */
public class ECManager {
    private final EnderChestProtect plugin;

    /**
     * The Ender Chest inventories on the server.
     */
    private Map<Integer, Inventory> inventories = new HashMap<Integer, Inventory>();

    public ECManager(EnderChestProtect plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a chest with the given owner and location.
     *
     * @param owner
     * @param loc
     * @return
     */
    public EnderChest createChest(String owner, Location loc) {
        DB.i().update("INSERT INTO enderchests (owner, world, x, y, z, access_time) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                owner, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return getChest(loc);
    }

    /**
     * Gets a chest from its id.
     *
     * @param id
     * @return
     */
    public EnderChest getChest(final int id) {
        return DB.i().query("SELECT * FROM enderchests WHERE id = ?", new ResultSetHandler<EnderChest>() {
            @Override
            public EnderChest handle(ResultSet rs) throws SQLException {
                if (!rs.next()) {
                    return null;
                }

                EnderChest ec = new EnderChest(plugin, id);
                ec.setData(rs);
                return ec;
            }
        }, id);
    }

    /**
     * Loads a chest from its location.
     *
     * @param loc
     * @return
     */
    public EnderChest getChest(final Location loc) {
        return DB.i().query("SELECT * FROM enderchests WHERE world = ? AND x = ? AND y = ? AND z = ?", new ResultSetHandler<EnderChest>() {
            @Override
            public EnderChest handle(ResultSet rs) throws SQLException {
                if (!rs.next()) {
                    return null;
                }
                EnderChest ec = new EnderChest(plugin, rs.getInt("id"));
                ec.setData(rs);
                return ec;
            }
        }, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Gets all of the Ender Chests of a player.
     *
     * @param p
     * @return
     */
    public List<EnderChest> getChests(String p) {
        return DB.i().query("SELECT * FROM enderchests WHERE owner = ?", new ResultSetHandler<List<EnderChest>>() {
            @Override
            public List<EnderChest> handle(ResultSet rs) throws SQLException {
                List<EnderChest> ret = new ArrayList<EnderChest>();
                while (rs.next()) {
                    EnderChest ec = new EnderChest(plugin, rs.getInt("id"));
                    ec.setData(rs);
                    ret.add(ec);
                }
                return ret;
            }
        }, p);
    }

    /**
     * Gets the number of Ender Chests of a player.
     *
     * @param p
     * @return
     */
    public int getChestCount(String p) {
        return ((Number) DB.i().get("SELECT COUNT(*) AS chests FROM enderchests WHERE owner = ?", 0, p)).intValue();
    }

    public Inventory createInventory(EnderChest chest) {
        Inventory inv = Bukkit.createInventory(null, 27, "Ender Chest " + chest.getId());
        if (chest.getContents() != null) {
            try {
                InventorySerializer.loadFromString(chest.getContents(), inv);
            } catch (InvalidConfigurationException ex) {
                String locStr = chest.getLocation().getWorld().getName() + ", " + chest.getLocation().getBlockX() + ", " + chest.getLocation().getBlockY() + ", " + chest.getLocation().getBlockZ();
                plugin.getLogger().log(Level.SEVERE, "Corrupted Ender Chest at " + locStr + "! Fix soon or " + chest.getOwner() + " will be mad!");
            }
        }
        inventories.put(chest.getId(), inv);
        return inv;
    }

    /**
     * Gets an inventory.
     *
     * @param id
     * @return
     */
    public Inventory getInventory(int id) {
        return inventories.get(id);
    }

    /**
     * Deletes an inventory.
     *
     * @param id The inventory id.
     */
    public void deleteInventory(int id) {
        Inventory inv = inventories.remove(id);
        if (inv != null) {
            inv.clear();
        }
    }
}
