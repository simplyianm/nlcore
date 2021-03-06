/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.new_liberty.itemconomy;

import net.new_liberty.nlcore.database.DB;
import org.bukkit.entity.Player;

/**
 * Represents a bank account of a player.
 */
public class BankAccount implements CurrencyHolder {

    private final String owner;

    /**
     * C'tor
     *
     * @param owner
     */
    public BankAccount(String owner) {
        this.owner = owner;
    }

    /**
     * C'tor
     *
     * @param owner
     */
    public BankAccount(Player owner) {
        this.owner = owner.getName();
    }

    @Override
    public int balance() {
        return (Integer) DB.i().get("SELECT balance FROM icbank WHERE player = ?", 0, owner);
    }

    @Override
    public boolean add(int amt) {
        DB.i().update("UPDATE icbank SET balance = balance + ? WHERE player = ?", amt, owner);
        return true;
    }

    @Override
    public int remove(int amt) {
        DB.i().update("UPDATE icbank SET balance = balance - ? WHERE player = ?", amt, owner);
        return 0;
    }

}
