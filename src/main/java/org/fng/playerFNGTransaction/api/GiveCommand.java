package org.fng.playerFNGTransaction.api;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.wallet.FNGWallet;
import org.fng.playerFNGTransaction.infrastructure.FNGWalletRepository;
import org.jetbrains.annotations.NotNull;

public class GiveCommand implements CommandExecutor {

    private JavaPlugin plugin;
    private final FNGWalletRepository fngWalletRepository;

    public GiveCommand(JavaPlugin plugin, FNGWalletRepository fngWalletRepository) {
        this.plugin = plugin;
        this.fngWalletRepository = fngWalletRepository;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player player){
            if (!player.hasPermission("fng.donner")) {
                player.sendMessage("Tu n'as pas la permission.");
                return true;
            }
            if (args.length != 2) {
                player.sendMessage("Usage: /donner <joueur> <quantité>");
                return true;
            }
            Player receiver = Bukkit.getPlayerExact(args[0]);
            if (receiver == null || !receiver.isOnline()) {
                player.sendMessage("Le joueur " + args[0] + " est introuvable ou hors ligne.");
                return true;
            }

            float amount;
            try {
                amount = Float.parseFloat(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("La quantité doit être un nombre.");
                return true;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                new FNGWallet(player, this.fngWalletRepository, this.plugin).addAmount(amount);
            });
        }
        return true;
    }
}
