package org.fng.playerFNGTransaction.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
import org.fng.playerFNGTransaction.domain.transaction.shop.FngShopItem;
import org.fng.playerFNGTransaction.infrastructure.ShopManagerRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddToShopCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final ShopManagerRepository shopManagerRepository;

    public AddToShopCommand(JavaPlugin plugin, ShopManagerRepository shopManagerRepository) {
        this.plugin = plugin;
        this.shopManagerRepository = shopManagerRepository;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Cette commande doit être exécutée en jeu.", NamedTextColor.RED));
            return true;
        }
        List<String> argsList = PlayerFNGTransaction.splitArguments(String.join(" ", args));
        // /addtoshop <shop> <material> <amount> <prix> <display name...>
        if (argsList.size() < 5) {
            sender.sendMessage(Component.text(
                    "Usage : /addtoshop <shop name> <material> <amount> <prix> <nom de l'item...>",
                    NamedTextColor.RED));
            return true;
        }

        String shopName = argsList.getFirst();
        String matString = argsList.get(1).toUpperCase(Locale.ROOT);

        Material material = Material.matchMaterial(matString);
        if (material == null) {
            sender.sendMessage(Component.text("Matériel inconnu : " + matString, NamedTextColor.RED));
            return true;
        }

        int amount;
        float price;
        try {
            amount = Integer.parseInt(argsList.get(2));
            price = Float.parseFloat(argsList.get(3));
        } catch (NumberFormatException ex) {
            sender.sendMessage(Component.text("Amount et prix doivent être numériques.", NamedTextColor.RED));
            return true;
        }

        String displayName = argsList.get(4);

        FngShopItem item = new FngShopItem(
                UUID.randomUUID(),
                shopName,
                material.name(),
                amount,
                displayName,
                price
        );

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = false;
            try {
                shopManagerRepository.addItemToShop(item);
                success = true;
            } catch (SQLException e) {
                plugin.getLogger().warning("Échec d’insertion SQL : " + e.getMessage());
            }

            boolean finalSuccess = success;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (finalSuccess) {
                    sender.sendMessage(Component.text(
                            "Ajouté " + amount + "×" + material +
                                    " à « " + shopName + " » pour " + price + " FNG.",
                            NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text(
                            "Impossible d’ajouter l’item au shop.",
                            NamedTextColor.RED));
                }
            });
        });

        return true;
    }
}
