package net.new_liberty.nlcore;

import java.util.HashMap;
import java.util.Map;
import net.new_liberty.nlcore.module.Module;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import net.new_liberty.bankchests.BankChests;
import net.new_liberty.enderchestprotect.EnderChestProtect;
import net.new_liberty.horses.Horses;
import net.new_liberty.itemconomy.Itemconomy;
import net.new_liberty.nlcore.database.DatabaseModule;
import net.new_liberty.nlcore.player.PlayerModule;

import net.new_liberty.tweaks.ChatColorCommands;
import net.new_liberty.tweaks.EasySpawners;
import net.new_liberty.tweaks.HeadDrop;
import net.new_liberty.specialeggs.SpecialEggs;
import net.new_liberty.tweaks.ChatHandler;
import net.new_liberty.tweaks.NerfStrengthPots;
import net.new_liberty.tweaks.NoEnderpearls;
import net.new_liberty.tweaks.NoInvisibilityPots;
import net.new_liberty.tweaks.NoTNTMinecart;
import net.new_liberty.tweaks.PortalUnstuck;
import net.new_liberty.tweaks.StaffList;
import net.new_liberty.tweaks.VHomeInformer;
import net.new_liberty.tweaks.VersionReport;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This plugin contains all of the tweaks to New Liberty that don't warrant
 * their own plugin.
 */
public class NLCore extends JavaPlugin implements Listener {

    private static NLCore instance;

    private Map<String, Module> modules = new HashMap<String, Module>();

    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;

        // Vault integration
        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();

        // Core modules
        addModule(new DatabaseModule());
        addModule(new PlayerModule());

        // Modules
        addModule(new BankChests());
        addModule(new EnderChestProtect());
        addModule(new Horses());
        addModule(new Itemconomy());
        addModule(new SpecialEggs());

        // Tweaks
        addModule(new ChatHandler());
        addModule(new ChatColorCommands());
        addModule(new EasySpawners());
        addModule(new HeadDrop());
        addModule(new NerfStrengthPots());
        addModule(new NoEnderpearls());
        addModule(new NoInvisibilityPots());
        addModule(new NoTNTMinecart());
        addModule(new PortalUnstuck());
        addModule(new StaffList());
        addModule(new VersionReport());
        addModule(new VHomeInformer());
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    /**
     * Gets a module by its name.
     *
     * @param name
     * @return
     */
    public Module getModule(String name) {
        return modules.get(name);
    }

    private void addModule(Module module) {
        for (String dep : module.getDependencies()) {
            if (Bukkit.getPluginManager().getPlugin(dep) == null) {
                module.getLogger().log(Level.WARNING, "Module could not be loaded due to missing dependency: " + dep);
                return;
            }
        }
        module.initialize(this);
        module.onEnable();
        module.getLogger().log(Level.INFO, "Tweak enabled.");
        modules.put(module.getName(), module);
    }

    public Economy getEconomy() {
        return economy;
    }

    public static NLCore i() {
        return instance;
    }

}
