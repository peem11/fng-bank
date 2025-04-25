package org.fng.playerFNGTransaction.domain.transaction.shop;

import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.wallet.FNGWallet;
import org.fng.playerFNGTransaction.infrastructure.ShopManagerRepository;

public class ShopManager {
    private final JavaPlugin plugin;
    private final ShopManagerRepository repository;
    public ShopManager(JavaPlugin plugin, ShopManagerRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public void buy(FngShopItem item, FNGWallet wallet) {

    }

    public void sell(FngShopItem item, FNGWallet wallet){

    }
}
