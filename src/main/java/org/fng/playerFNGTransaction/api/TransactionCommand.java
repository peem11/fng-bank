package org.fng.playerFNGTransaction.api;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.transaction.TransactionManager;
import org.fng.playerFNGTransaction.domain.wallet.FNGWallet;
import org.fng.playerFNGTransaction.infrastructure.FNGWalletRepository;
import org.jetbrains.annotations.NotNull;

public class TransactionCommand implements CommandExecutor {
    JavaPlugin plugin;
    TransactionManager transactionManager;
    FNGWalletRepository fngWalletRepository;

    public TransactionCommand(JavaPlugin plugin, TransactionManager transactionManager, FNGWalletRepository repository) {
        this.plugin = plugin;
        this.transactionManager = transactionManager;
        this.fngWalletRepository = repository;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player from)) {
            commandSender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        if (!from.hasPermission("fng.transaction")) {
            from.sendMessage("Tu n'as pas la permission.");
            return true;
        }

        if (args.length != 2) {
            from.sendMessage("Usage: /transaction <joueur> <quantité>");
            return true;
        }

        Player receiver = Bukkit.getPlayerExact(args[0]);

        if (receiver == null || !receiver.isOnline()) {
            from.sendMessage("Le joueur " + args[0] + " est introuvable ou hors ligne.");
            return true;
        }

        float amount;
        try {
            amount = Float.parseFloat(args[1]);
        } catch (NumberFormatException e) {
            from.sendMessage("La quantité doit être un nombre.");
            return true;
        }

        if (amount <= 0f) {
            from.sendMessage("La quantité doit être supérieure à 0.");
            return true;
        }
        FNGWallet receiverWallet = new FNGWallet(receiver, this.fngWalletRepository, this.plugin);
        FNGWallet senderWallet = new FNGWallet(from, this.fngWalletRepository, this.plugin);
        transactionManager.transact(senderWallet, receiverWallet, amount);
        return true;
    }
}
