package org.fng.playerFNGTransaction.infrastructure;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.transaction.shop.FngShop;
import org.fng.playerFNGTransaction.domain.transaction.shop.FngShopItem;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            String url = "jdbc:sqlite:" + new File(plugin.getDataFolder(), "shop.db").getAbsolutePath();
            this.connection = DriverManager.getConnection(url);
            Statement stmt = connection.createStatement();

            //shop creation
            stmt.addBatch("CREATE TABLE IF NOT EXISTS SHOP ( name VARCHAR(45) PRIMARY KEY );");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS SHOP_ITEM ( item_uuid TEXT, shop_name VARCHAR(45), material VARCHAR(45), amount INT, display_name VARCHAR(45), price DECIMAL(10,2), PRIMARY KEY (shop_name, item_uuid), FOREIGN KEY (shop_name) REFERENCES SHOP(name) );");
            stmt.executeBatch();

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

    public void createShop(String name) throws SQLException {
        String sql = "INSERT INTO SHOP (name) VALUES (?);";
        PreparedStatement update = this.connection.prepareStatement(sql);
        update.setString(1, name);
        update.executeUpdate();
        update.close();
    }

    public void addItemToShop(FngShopItem item) throws SQLException {
        String sql = "INSERT INTO SHOP_ITEM (item_uuid, shop_name, material, amount, display_name, price) VALUES (?,?,?,?,?,?);";
        PreparedStatement update = this.connection.prepareStatement(sql);
        update.setString(1, item.getItem_uuid().toString());
        update.setString(2, item.getShop_name());
        update.setString(3, item.getType().toString());
        update.setInt(4, item.getAmount());
        update.setString(5, item.getDisplay_name());
        update.setFloat(6, item.getPrice());
        update.executeUpdate();
        update.close();
    }

    public @NotNull FngShop getShop(@NotNull String fngWorker) throws SQLException {
        String sql = "SELECT item_uuid, shop_name, material, amount, display_name, price FROM SHOP_ITEM WHERE shop_name = ?";

        try (PreparedStatement select = this.connection.prepareStatement(sql)) {
            select.setString(1, fngWorker);

            try (ResultSet result = select.executeQuery()) {
                List<FngShopItem> items = new ArrayList<>();
                while(result.next()){
                    FngShopItem item = new FngShopItem(
                            UUID.fromString(result.getString(1)),
                            result.getString(2),
                            result.getString(3),
                            result.getInt(4),
                            result.getString(5),
                            result.getFloat(6)
                    );
                    ItemMeta meta = item.getItemMeta();
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "fngshopitem"), PersistentDataType.BOOLEAN, true);
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "price"), PersistentDataType.FLOAT, item.getPrice());
                    item.setItemMeta(meta);
                    items.add(item);
                }
                if (items.isEmpty()) throw new SQLException("Shop doesn't exist or is empty !");
                return new FngShop(this.plugin, items.getFirst().getShop_name(), items);
            }
        }
    }
}
