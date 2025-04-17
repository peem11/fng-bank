package org.fng.playerFNGTransaction.domain.wallet;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
import org.jetbrains.annotations.NotNull;

public class SeeFundsCommand implements CommandExecutor {

    JavaPlugin plugin;
    public SeeFundsCommand(JavaPlugin javaPlugin) {
        this.plugin = javaPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player player) {
            FNGWallet wallet = new FNGWallet(player, new NamespacedKey(this.plugin, "wallet"));

            player.sendMessage(Component.text("Votre solde: ").append(Component.text(wallet.getAmount() + "FNG", NamedTextColor.GREEN)));

            return true;
        }
        return true;
    }
}
