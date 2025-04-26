package org.fng.playerFNGTransaction.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
import org.fng.playerFNGTransaction.infrastructure.ShopManagerRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateShopCommand implements CommandExecutor {


    private final JavaPlugin plugin;
    private final ShopManagerRepository shopManagerRepository;

    public CreateShopCommand(JavaPlugin plugin, ShopManagerRepository shopManagerRepository) {
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
        if (argsList.size() != 1) {
            sender.sendMessage(Component.text("Usage : /createshop <nom-du-shop>", NamedTextColor.RED));
            return true;
        }

        String shopName = argsList.getFirst();

            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try {
                    shopManagerRepository.createShop(shopName);

                    Bukkit.getScheduler().runTask(this.plugin, () -> {
                        sender.sendMessage(Component.text("Shop \"" + shopName + "\" créé avec succès !", NamedTextColor.GREEN));
                    });
                } catch (SQLException e) {
                    Bukkit.getScheduler().runTask(this.plugin, () -> {
                        sender.sendMessage(Component.text("Erreur lors de la création du shop.", NamedTextColor.RED));
                    });
                }
            });

        return true;
    }

}
