package org.fng.playerFNGTransaction.domain.transaction.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.wallet.FNGWallet;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
public class FNGTransactionListener implements Listener {
    private final NamespacedKey WALLET_KEY;
    private final JavaPlugin plugin;
    public FNGTransactionListener(JavaPlugin plugin) {
        WALLET_KEY = new NamespacedKey(plugin, "wallet");;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        onJoin(player);
        checkFirstJoin(player);

    }

    private void onJoin(Player player) {
        player.sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX
                .append(Component.text(" Bonjour ", NamedTextColor.GREEN))
                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                .append(Component.text("!", NamedTextColor.WHITE)));
    }

    private void checkFirstJoin(Player player) {
        if(!player.hasPlayedBefore()){
            player.getPersistentDataContainer().set(WALLET_KEY, PersistentDataType.FLOAT, 0f);
            TextComponent firstJoinMessage = PlayerFNGTransaction.PLUGIN_PREFIX
                    .append(Component.text(" C'est la première fois que tu rejoins la ville de ", NamedTextColor.WHITE))
                    .append(Component.text(" François N.G.", NamedTextColor.BLUE))
                    .append(Component.text("!", NamedTextColor.WHITE));
            player.sendMessage(firstJoinMessage);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                TextComponent firstJoinGiftMessage = PlayerFNGTransaction.PLUGIN_PREFIX
                        .append(Component.text(" Pour te souhaiter la bienvenue nous te donnons ", NamedTextColor.WHITE))
                        .append(Component.text(FNGWallet.START_AMOUNT)
                                .append(Component.text("FNG!")));
                player.getPersistentDataContainer().set(WALLET_KEY, PersistentDataType.FLOAT, player.getPersistentDataContainer().get(WALLET_KEY, PersistentDataType.FLOAT) + FNGWallet.START_AMOUNT);

                player.sendMessage(firstJoinGiftMessage);
            }, 20L * 5);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                TextComponent fngExplanationMessage = PlayerFNGTransaction.PLUGIN_PREFIX
                        .append(Component.text(" Le FNG est la monaie d'échange de la ville de ", NamedTextColor.WHITE))
                        .append(Component.text("François N.G. ", NamedTextColor.BLUE))
                        .append(Component.text("Avec cette monaie, tu peux acheter à des joueurs et à des villageois.",NamedTextColor.WHITE))
                        .append(Component.text(" /"+ PlayerFNGTransaction.SEE_FUNDS_COMMAND, NamedTextColor.YELLOW))
                        .append(Component.text(" pour voir ton solde", NamedTextColor.WHITE));
                player.sendMessage(fngExplanationMessage);
            }, 20L * 10);

        }
    }
}
