package net.new_liberty.enderchestprotect;

import net.new_liberty.util.InventorySerializer;
import net.new_liberty.nlcore.database.DB;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.apache.commons.dbutils.ResultSetHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * Object to manipulate an Ender Chest in an object-oriented fashion.
 */
public class EnderChest {
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMMM d, yyyy hh:mm aaa");

    private EnderChestProtect plugin;

    private final int id;

    /**
     * If true, getter operations will reload the data from the database.
     */
    private boolean dirty = false;

    private String owner;

    private Location loc;

    private String contents;

    private Timestamp lastAccessed;

    public EnderChest(EnderChestProtect plugin, int id) {
        this.plugin = plugin;
        this.id = id;
    }

    public void repopulate() {
        DB.i().query("SELECT * FROM enderchests WHERE id = ?", new ResultSetHandler<Object>() {
            @Override
            public Object handle(ResultSet rs) throws SQLException {
                rs.next();
                setData(rs);
                return null;
            }
        }, id);
    }

    void setData(ResultSet rs) throws SQLException {
        setData(rs.getString("owner"), rs.getString("world"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getString("contents"), rs.getTimestamp("access_time"));
    }

    void setData(String owner, Location loc, String contents, Timestamp lastAccessed) {
        this.owner = owner;
        this.loc = loc;
        this.contents = contents;
        this.lastAccessed = lastAccessed;
    }

    void setData(String owner, String world, int x, int y, int z, String contents, Timestamp lastAccessed) {
        World w = Bukkit.getWorld(world);
        loc = new Location(w, x, y, z);
        setData(owner, loc, contents, lastAccessed);
    }

    public int getId() {
        return id;
    }

    public String getOwner() {
        checkDirty();
        return owner;
    }

    public Location getLocation() {
        checkDirty();
        return loc;
    }

    public String getContents() {
        checkDirty();
        return contents;
    }

    public Timestamp getLastAccessed() {
        checkDirty();
        return lastAccessed;
    }

    /**
     * Gets the time this Ender Chest will expire.
     *
     * @return
     */
    public Timestamp getExpiryTime() {
        return new Timestamp(getLastAccessed().getTime() + plugin.getExpiryMillis());
    }

    /**
     * Checks if this Ender Chest's protection is expired.
     *
     * @return
     */
    public boolean isExpired() {
        return getExpiryTime().before(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Updates the expiry time of this Ender Chest.
     */
    public void updateAccessTime() {
        DB.i().update("UPDATE enderchests SET access_time = CURRENT_TIMESTAMP WHERE id = ?", id);
        dirty = true;
    }

    /**
     * Gets the expiry time of this Ender Chest as a string.
     *
     * @return
     */
    public String getExpiryTimeString() {
        return isExpired() ? "expired" : DATE_FORMAT.format(getExpiryTime());
    }

    /**
     * Gets the contents of a message that is sent when the chest expires.
     *
     * @return
     */
    public String getExpiryInfoMessage() {
        return ChatColor.YELLOW + "This chest's protection expires on " + ChatColor.AQUA + getExpiryTimeString() + " CST " + ChatColor.GREEN + "(" + ((getExpiryTime().getTime() - System.currentTimeMillis()) / (60 * 1000)) + " minutes from now)";
    }

    /**
     * Checks if the player can access the chest.
     *
     * @param p
     * @return
     */
    public boolean canAccess(Player p) {
        return getOwner().equals(p.getName()) || p.hasPermission("ecp.admin") || isExpired();
    }

    /**
     * Gets an Inventory for this chest.
     *
     * @return
     */
    public Inventory getInventory() {
        Inventory inv = plugin.getECManager().getInventory(id);
        if (inv == null) {
            inv = plugin.getECManager().createInventory(this);
        }
        return inv;
    }

    /**
     * Checks if this Ender Chest contains items.
     *
     * @return
     */
    public boolean hasItems() {
        return getContents() != null || "".equals(getContents());
    }

    /**
     * Saves the chest.
     *
     * @param inv
     */
    public void save() {
        Inventory inv = getInventory();
        String theContents = (inv == null ? null : InventorySerializer.writeToString(inv));
        DB.i().update("UPDATE enderchests SET contents = ? WHERE id = ?", theContents, id);
        dirty = true;
    }

    /**
     * Destroys the Ender Chest.
     */
    public void destroy() {
        DB.i().update("DELETE FROM enderchests WHERE id = ?", id);
        plugin.getECManager().deleteInventory(id);
    }

    /**
     * Performs a dirty check and repopulates if dirty.
     */
    private void checkDirty() {
        if (dirty) {
            repopulate();
            dirty = false;
        }
    }
}
