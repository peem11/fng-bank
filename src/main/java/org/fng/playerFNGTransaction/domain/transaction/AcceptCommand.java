package org.fng.playerFNGTransaction.domain.transaction;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.fng.playerFNGTransaction.domain.wallet.FNGWallet;
import org.jetbrains.annotations.NotNull;

public class AcceptCommand implements CommandExecutor {

    private final TransactionManager transactionManager;
    private final NamespacedKey key;
    public AcceptCommand(TransactionManager transactionManager, NamespacedKey walletKey) {
        this.transactionManager = transactionManager;
        this.key = walletKey;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        FNGWallet receiverWallet = new FNGWallet(player, key);
        boolean success = transactionManager.confirm(receiverWallet);

        if (success) {
            player.getPlayer().sendMessage("✅ Transaction acceptée !");
        } else {
            player.getPlayer().sendMessage("❌ Aucune transaction en attente.");
        }

        return true;
    }
}
