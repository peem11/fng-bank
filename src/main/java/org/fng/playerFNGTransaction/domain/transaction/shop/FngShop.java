package org.fng.playerFNGTransaction.domain.transaction.shop;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.transaction.Receiver;
import org.fng.playerFNGTransaction.infrastructure.ShopManagerRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FngShop implements InventoryHolder {

    private final JavaPlugin plugin;
    private final String name;
    private int page;
    private final List<FngShopItem> fngShopItems;
    public FngShop(JavaPlugin plugin, String name, List<FngShopItem> fngShopItems){
        this.plugin = plugin;
        this.name = name;
        this.page = 0;
        this.fngShopItems = fngShopItems;
    }
    @Override
    public @NotNull Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, 27, Component.text(name)); // 3 lignes = 27 slots

        int startIndex = page * 9;
        int endIndex = Math.min(startIndex + 9, fngShopItems.size());

        List<FngShopItem> currentPageItems = fngShopItems.subList(startIndex, endIndex);

        // Mettre les items aux slots 9 -> 17 (ligne du milieu)
        int[] middleSlots = {9, 10, 11, 12, 13, 14, 15, 16, 17}; // slots du centre
        for (int i = 0; i < currentPageItems.size(); i++) {
            inventory.setItem(middleSlots[i], currentPageItems.get(i));
        }

        // Ajouter les flèches navigation
        ItemStack prevPage = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevPage.getItemMeta();
        prevMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "prev"), PersistentDataType.BOOLEAN, true);
        prevMeta.displayName(Component.text("§ePage précédente"));
        prevPage.setItemMeta(prevMeta);

        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "next"), PersistentDataType.BOOLEAN, true);
        nextMeta.displayName(Component.text("§ePage suivante"));
        nextPage.setItemMeta(nextMeta);

        if(page != 0){
            inventory.setItem(18, prevPage);
        }
        if((page * 9) + 9 < fngShopItems.size()){
            inventory.setItem(26, nextPage); // tout à droite, ligne du bas
        }


        return inventory;
    }

    public String getName(){
        return this.name;
    }

    public Inventory nextPage(){
        if((page * 9) + 9 < fngShopItems.size()){
            this.page++;
        }
        return getInventory();
    }
    public Inventory prevPage(){
        if(this.page > 0){
            this.page--;
        }
        return getInventory();
    }

}
