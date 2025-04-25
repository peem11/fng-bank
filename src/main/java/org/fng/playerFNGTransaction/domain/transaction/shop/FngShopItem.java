package org.fng.playerFNGTransaction.domain.transaction.shop;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class FngShopItem extends ItemStack {

    private UUID item_uuid;
    private String shop_name;
    private String display_name;
    private float price;

    public FngShopItem(UUID itemUuid, String shopName, String materialName, int amount, String displayName, float price) {
        super(Material.valueOf(materialName), amount);
        ItemMeta meta = this.getItemMeta();
        meta.displayName(Component.text(displayName));
        meta.lore(List.of(Component.text("Acheter x" + amount + " " + this.getType() + " pour " + price + "FNG")));
        this.setItemMeta(meta);
        item_uuid = itemUuid;
        shop_name = shopName;
        display_name = displayName;
        this.price = price;
    }

    public UUID getItem_uuid() {
        return item_uuid;
    }

    public String getShop_name() {
        return shop_name;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public float getPrice() {
        return price;
    }
}
