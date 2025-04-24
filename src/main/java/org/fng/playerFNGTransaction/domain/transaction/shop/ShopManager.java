package org.fng.playerFNGTransaction.domain.transaction.shop;

import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.wallet.FNGWallet;

public class ShopManager {
    private final JavaPlugin plugin;
    private final ShopManagerRepository repository;
    public ShopManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void buy(FngShopItem item, FNGWallet wallet) {

    }

    public void sell(FngShopItem item, FNGWallet wallet){

    }
}
