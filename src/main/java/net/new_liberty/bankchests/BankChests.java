/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.new_liberty.bankchests;

import net.new_liberty.nlcore.database.Database;
import net.new_liberty.nlcore.module.Module;

/**
 * Handles the single big ender chest at spawn.
 */
public class BankChests extends Module {

    private BCListener l;

    private BCManager chests;

    @Override
    public void onEnable() {
        l = new BCListener(this);
        addListener(l);

        chests = new BCManager(this);

        Database.i().update("CREATE TABLE IF NOT EXISTS ecb_inventories ("
                + "owner VARCHAR(16) NOT NULL,"
                + "contents TEXT,"
                + "PRIMARY KEY (owner));");

        addPermission("bankchests.admin", "Allows placement of ender chests.");
    }

    public BCManager getChests() {
        return chests;
    }

}
