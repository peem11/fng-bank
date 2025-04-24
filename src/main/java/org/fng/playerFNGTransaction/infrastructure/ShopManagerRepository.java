package org.fng.playerFNGTransaction.infrastructure;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class ShopManagerRepository {

    private Connection connection;
    private final JavaPlugin plugin;

    public ShopManagerRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        this.init();
    }
    private void init() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        try {
            String url = "jdbc:sqlite:" + new File(plugin.getDataFolder(), "data.db").getAbsolutePath();
            this.connection = DriverManager.getConnection(url);
            Statement stmt = connection.createStatement();

            
            //TODO
            stmt.execute("CREATE TABLE IF NOT EXISTS FNG_SHOP (owner_uuid TEXT PRIMARY KEY, ,fng_value FLOAT DEFAULT 0 CHECK(fng >= 0));");

            getLogger().info("Connected to SQLite.");
        } catch (SQLException e) {
            getLogger().severe("Could not connect to SQLite DB!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(plugin);
        }
    }
}
