package org.fng.playerFNGTransaction.api;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
import org.fng.playerFNGTransaction.domain.transaction.TransactionManager;
import org.fng.playerFNGTransaction.domain.wallet.FNGWallet;
import org.fng.playerFNGTransaction.infrastructure.FNGWalletRepository;
import org.jetbrains.annotations.NotNull;

public class AcceptCommand implements CommandExecutor {

    private final TransactionManager transactionManager;
    private final FNGWalletRepository fngWalletRepository;
    private final JavaPlugin plugin;
    public AcceptCommand(TransactionManager transactionManager, FNGWalletRepository fngWalletRepository, JavaPlugin plugin) {
        this.transactionManager = transactionManager;
        this.fngWalletRepository = fngWalletRepository;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        FNGWallet receiverWallet = new FNGWallet(player, this.fngWalletRepository, this.plugin);
        boolean success = this.transactionManager.confirm(receiverWallet);

        if (success) {
            player.getPlayer().sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX.append(Component.text("✅ Transaction acceptée !")));
        } else {
            player.getPlayer().sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX.append(Component.text("❌ Aucune transaction en attente.")));
        }
        });
        return true;
    }
}
