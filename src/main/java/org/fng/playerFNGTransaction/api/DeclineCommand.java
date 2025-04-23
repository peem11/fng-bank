package org.fng.playerFNGTransaction.api;

import net.kyori.adventure.text.Component;
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

public class DeclineCommand implements CommandExecutor {
    private final FNGWalletRepository fngWalletRepository;
    private final TransactionManager transactionManager;
    private final JavaPlugin plugin;
    public DeclineCommand(TransactionManager transactionManager, FNGWalletRepository fngWalletRepository, JavaPlugin plugin) {
        this.transactionManager = transactionManager;
        this.fngWalletRepository = fngWalletRepository;
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player player)) return true;

        FNGWallet receiverWallet = new FNGWallet(player, fngWalletRepository, this.plugin);
        boolean success = transactionManager.decline(receiverWallet);

        if (success) {
            player.getPlayer().sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX.append(Component.text(" ✅ Transaction refusé avec succès.")));
        } else {
            player.getPlayer().sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX.append(Component.text("❌ Aucune transaction en attente.")));
        }

        return true;
    }
}
