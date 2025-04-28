package org.fng.playerFNGTransaction.api;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.transaction.shop.FngSellShop;
import org.fng.playerFNGTransaction.infrastructure.FNGWalletRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class SellCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final FNGWalletRepository walletRepository;
    public SellCommand(JavaPlugin plugin, FNGWalletRepository walletRepository) {
        this.plugin = plugin;
        this.walletRepository = walletRepository;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player player)) return true;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                float assets = this.walletRepository.getPlayerFNG(player.getUniqueId());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    FngSellShop sellShop = new FngSellShop(player, assets, this.walletRepository);
                    player.openInventory(sellShop.getInventory());
                });

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });


        return true;
    }
}
