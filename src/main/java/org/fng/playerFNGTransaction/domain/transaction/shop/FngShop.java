package org.fng.playerFNGTransaction.domain.transaction.shop;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.fng.playerFNGTransaction.domain.transaction.Receiver;
import org.fng.playerFNGTransaction.infrastructure.ShopManagerRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FngShop implements InventoryHolder {

    private final String name;
    private final List<FngShopItem> fngShopItems;
    public FngShop(String name, List<FngShopItem> fngShopItems){
        this.name = name;
        this.fngShopItems = fngShopItems;
    }
    @Override
    public @NotNull Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, InventoryType.CHEST);
        fngShopItems.forEach((inventory::addItem));
        return inventory;
    }

    public String getName(){
        return this.name;
    }


}
