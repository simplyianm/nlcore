package net.new_liberty.specialeggs;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;
import java.util.HashMap;
import java.util.Map;
import net.new_liberty.nlcore.module.Module;
import net.new_liberty.specialeggs.eggs.BlinkEgg;
import net.new_liberty.specialeggs.eggs.FreezeEgg;
import net.new_liberty.specialeggs.eggs.SpongeEgg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SpecialEggs extends Module {

    private CombatTagApi combatTag = null;

    private Map<String, EggCooldowns> cds = new HashMap<String, EggCooldowns>();

    private Map<String, SpecialEgg> eggs = new HashMap<String, SpecialEgg>();

    private WorldGuardPlugin wg;

    @Override
    public void onEnable() {
        addPermission("specialeggs.segive", "Allows access to the /segive command.");

        Plugin ctPlugin = Bukkit.getPluginManager().getPlugin("CombatTag");
        if (ctPlugin != null && ctPlugin.isEnabled()) {
            combatTag = new CombatTagApi((CombatTag) ctPlugin);
        }

        wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

        addEgg(new BlinkEgg());
        addEgg(new FreezeEgg());
        addEgg(new SpongeEgg());

        for (SpecialEgg egg : eggs.values()) {
            egg.initialize(this);
            egg.onEnable();
        }

        plugin.getCommand("segive").setExecutor(new SEGive(this));
    }

    @Override
    public void onDisable() {
        for (SpecialEgg egg : eggs.values()) {
            egg.onDisable();
        }
    }

    /**
     * Gets the cooldown timers of a player.
     *
     * @param player
     * @return
     */
    public EggCooldowns getCooldowns(String player) {
        EggCooldowns cd = cds.get(player);
        if (cd == null) {
            cd = new EggCooldowns(player);
            cds.put(player, cd);
        }
        return cd;
    }

    /**
     * Gets an egg from its name.
     *
     * @param name
     * @return
     */
    public SpecialEgg getEgg(String name) {
        return eggs.get(name);
    }

    public boolean isInCombat(Player player) {
        if (combatTag == null) {
            return false;
        }
        return combatTag.isInCombat(player);
    }

    public WorldGuardPlugin getWg() {
        return wg;
    }

    private void addEgg(SpecialEgg egg) {
        eggs.put(egg.getName(), egg);
        Bukkit.getPluginManager().registerEvents(egg, plugin);
    }

}
