package org.fng.playerFNGTransaction.domain.transaction.shop;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.fng.playerFNGTransaction.infrastructure.FNGWalletRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class FngSellShop implements InventoryHolder {


    private static final int MOST_WANTED = 5000;
    private static final int HIGH_INTEREST_VALUE = 650;
    private static final int MID_INTEREST_VALUE = 200;
    private static final int LOW_INTEREST_VALUE = 100;
    private static final int DEFAULT_INTEREST_VALUE = 10;

    private static final Material[] MOST_WANTED_MATERIALS = {Material.OBSIDIAN, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Material.NETHER_STAR};
    private static final Material[] HIGH_INTEREST_MATERIALS = {Material.DIAMOND, Material.OBSIDIAN, Material.LAVA_BUCKET};
    private static final Material[] MID_INTEREST_MATERIALS = {Material.IRON_INGOT, Material.GOLD_INGOT, Material.BREAD};
    private static final Material[] LOW_INTEREST_MATERIALS = {Material.WHEAT, Material.MELON, Material.COPPER_INGOT, Material.SUGAR_CANE};

    private final Player player;
    private final float assets;
    private final FNGWalletRepository walletRepository;
    private boolean success;

    public FngSellShop(Player player, float assets, FNGWalletRepository walletRepository) {
        this.player = player;
        this.assets = assets;
        this.walletRepository = walletRepository;
    }

    @Override
    public @NotNull Inventory getInventory() {
        Inventory gui = Bukkit.createInventory(this, 54, Component.text("Placer vos items à vendre"));
// ----------- Bouton CONFIRM -----------
        ItemStack confirm = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta cMeta = confirm.getItemMeta();
        cMeta.displayName(Component.text("§a✔ Confirmer"));
        cMeta.lore(List.of(Component.text("Clique pour valider")));
        confirm.setItemMeta(cMeta);

// ----------- Bouton CANCEL ------------
        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta xMeta = cancel.getItemMeta();
        xMeta.displayName(Component.text("§c✖ Annuler"));
        xMeta.lore(List.of(Component.text("Clique pour revenir")));
        cancel.setItemMeta(xMeta);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta hMeta = (SkullMeta) head.getItemMeta();
        hMeta.lore(List.of(Component.text("Solde: "), Component.text("§a" + assets + "FNG")));
        hMeta.setOwningPlayer(player);
        hMeta.displayName(Component.text(player.getName()));
        head.setItemMeta(hMeta);
        gui.setItem(49, head);


        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fMeta = filler.getItemMeta();
        fMeta.displayName(Component.text(" ")); // nom vide pour éviter le survol texte
        filler.setItemMeta(fMeta);

        // Parcourt la barre du bas (indices 45-53) et remplit les cases libres
        for (int slot = 45; slot <= 53; slot++) {
            if (gui.getItem(slot) == null) {
                gui.setItem(slot, filler);
            }
        }


// ----------- Placement (rangée 6) ------
        gui.setItem(45, confirm);  // première case de la dernière rangée
        // case centrale
        gui.setItem(53, cancel);   // dernière case

        return gui;
    }


    public void startTransaction(Inventory inventory) {
        float amount = computeAmount(inventory);

        // Opération DB asynchrone : créditer le joueur
        Bukkit.getScheduler().runTaskAsynchronously(walletRepository.getPlugin(), () -> {
            try {
                walletRepository.add(player.getUniqueId(), amount);
                Bukkit.getScheduler().runTask(walletRepository.getPlugin(), () ->
                        player.sendMessage("Vous avez reçu : §a" + amount + " FNG !"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean succeeded() {
        return this.success;
    }
    public static float computeAmount(Inventory inv) {
        float total = 0f;
        for (int slot = 0; slot < 45; slot++) {
            ItemStack stack = inv.getItem(slot);
            if (stack == null || stack.getType().isAir()) continue;

            int unit = getUnitValue(stack.getType());
            total += unit * stack.getAmount();
        }
        return total;
    }

    private static int getUnitValue(Material mat) {
        // Précédence : MOST_WANTED > HIGH > MID > LOW > DEFAULT
        if (Arrays.asList(MOST_WANTED_MATERIALS).contains(mat))       return MOST_WANTED;
        if (Arrays.asList(HIGH_INTEREST_MATERIALS).contains(mat))     return HIGH_INTEREST_VALUE;
        if (Arrays.asList(MID_INTEREST_MATERIALS).contains(mat))      return MID_INTEREST_VALUE;
        if (Arrays.asList(LOW_INTEREST_MATERIALS).contains(mat))      return LOW_INTEREST_VALUE;
        return DEFAULT_INTEREST_VALUE;
    }

    public @NotNull Plugin getPlugin() {
        return this.walletRepository.getPlugin();
    }

    public void setSucceeded() {
        this.success = true;
    }
}
