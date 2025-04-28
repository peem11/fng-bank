package org.fng.playerFNGTransaction.infrastructure;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.wallet.FNGWallet;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class FNGWalletRepository {

    private Connection connection;
    private final JavaPlugin plugin;
    public FNGWalletRepository(JavaPlugin plugin) {
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

            //Wallet creation
            stmt.execute("CREATE TABLE IF NOT EXISTS PLAYER_FNG_WALLET (player_uuid TEXT PRIMARY KEY, fng FLOAT DEFAULT 0 CHECK(fng >= 0));");

            getLogger().info("Connected to SQLite.");
        } catch (SQLException e) {
            getLogger().severe("Could not connect to SQLite DB!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(plugin);
        }
    }
    public void close() throws SQLException {
        if(this.connection != null){
            this.connection.close();
        }
    }
    public void insertWallet(@NotNull UUID uuid, float amount) throws SQLException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement insert = this.connection.prepareStatement("INSERT INTO PLAYER_FNG_WALLET (player_uuid, fng) VALUES (?, ?)");
                insert.setString(1, uuid.toString());
                insert.setFloat(2, amount);
                insert.executeUpdate();
                insert.close();
            }catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public boolean hasWallet(UUID uuid) throws SQLException {
        this.checkIfPrimaryThread();
        String sql = "SELECT 1 FROM PLAYER_FNG_WALLET WHERE player_uuid = ?";

        try (PreparedStatement select = this.connection.prepareStatement(sql)) {
            select.setString(1, uuid.toString());

            try (ResultSet result = select.executeQuery()) {
                return result.next();
            }
        }
    }
    public float getPlayerFNG(@NotNull UUID uuid) throws SQLException {
        this.checkIfPrimaryThread();
        String sql = "SELECT fng FROM PLAYER_FNG_WALLET WHERE player_uuid = ?";

        try (PreparedStatement select = this.connection.prepareStatement(sql)) {
            select.setString(1, uuid.toString());

            try (ResultSet result = select.executeQuery()) {
                if (result.next()) {
                    return result.getFloat("fng"); // or result.getFloat(1)
                }
            }
        }
        throw new SQLException("Could not find player");
    }

    public void setAmount(@NotNull UUID uuid, Float amount) throws SQLException {
        this.checkIfPrimaryThread();
        PreparedStatement update = this.connection.prepareStatement("UPDATE PLAYER_FNG_WALLET SET fng = ? WHERE player_uuid = ?");
        update.setFloat(1, amount);
        update.setString(2, uuid.toString());
        update.executeUpdate();
        update.close();
    }

    private void checkIfPrimaryThread(){
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getLogger().warning("[FNG] ⚠️ function called on the main thread! This may cause lag.");
            // Optional: throw new IllegalStateException("SQL call on main thread!");
        }
    }

    public void remove(@NotNull UUID uuid, Float amount) throws SQLException {
        this.checkIfPrimaryThread();
        PreparedStatement update = this.connection.prepareStatement("UPDATE PLAYER_FNG_WALLET SET fng = fng - ? WHERE player_uuid = ?");
        update.setFloat(1, amount);
        update.setString(2, uuid.toString());
        update.executeUpdate();
        update.close();
    }

    public void add(@NotNull UUID uuid, Float amount) throws SQLException {
        this.checkIfPrimaryThread();
        PreparedStatement update = this.connection.prepareStatement("UPDATE PLAYER_FNG_WALLET SET fng = fng + ? WHERE player_uuid = ?");
        update.setFloat(1, amount);
        update.setString(2, uuid.toString());
        update.executeUpdate();
        update.close();
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}
