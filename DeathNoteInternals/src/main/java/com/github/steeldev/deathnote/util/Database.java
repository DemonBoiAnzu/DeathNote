package com.github.steeldev.deathnote.util;

import com.github.steeldev.deathnote.util.data.DNPlayerData;
import org.bukkit.entity.Player;

import java.sql.*;

import static com.github.steeldev.deathnote.util.Util.getMain;

public class Database {
    static Connection dbConnection;

    public static void getConnection() throws SQLException {
        dbConnection = DriverManager.getConnection(String.format("jdbc:sqlite:%s\\DeathNote.db", getMain().getDataFolder()));
    }

    public static void closeConnection() throws SQLException {
        dbConnection.close();
    }

    public static void create() throws SQLException {
        Statement statement = dbConnection.createStatement();
        statement.setQueryTimeout(30);

        statement.executeUpdate("create table if not exists players (id string, kills integer)");
    }

    public static void addPlayer(Player player) throws SQLException {
        Statement statement = dbConnection.createStatement();
        statement.setQueryTimeout(30);

        statement.executeUpdate(String.format("insert into players values('%s', 0)", player.getUniqueId()));
    }

    public static void updatePlayerData(DNPlayerData data) throws SQLException {
        Statement statement = dbConnection.createStatement();
        statement.setQueryTimeout(30);
        statement.executeUpdate(String.format("update players set kills = %d where id = '%s'", data.kills, data.player.getUniqueId()));
    }

    public static DNPlayerData getPlayerData(Player player) throws SQLException {
        Statement statement = dbConnection.createStatement();
        statement.setQueryTimeout(30);
        ResultSet rs = statement.executeQuery(String.format("select * from players where id = '%s'", player.getUniqueId()));
        DNPlayerData data = null;
        while (rs.next())
            data = new DNPlayerData(player, rs.getInt("kills"));
        return data;
    }
}
