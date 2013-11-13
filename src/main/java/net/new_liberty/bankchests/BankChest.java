/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.new_liberty.bankchests;

import com.simplyian.easydb.EasyDB;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Represents a player's ender chest inventory.
 */
public class BankChest {

    private final String owner;

    private String contents;

    public BankChest(String owner) {
        this.owner = owner;
    }

    public void populate() {
        EasyDB.getDb().query("SELECT * FROM bankchests WHERE owner = ?", new ResultSetHandler<Object>() {
            @Override
            public Object handle(ResultSet rs) throws SQLException {
                rs.next();
                setContents(rs.getString("contents"));
                return null;
            }

        }, owner);
    }

    public String getOwner() {
        return owner;
    }

    void setContents(String contents) {
        this.contents = contents;
    }

}
