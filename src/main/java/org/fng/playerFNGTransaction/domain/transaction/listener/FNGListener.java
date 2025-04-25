package org.fng.playerFNGTransaction.domain.transaction.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.transaction.TransactionManager;
import org.fng.playerFNGTransaction.domain.transaction.shop.FngShop;
import org.fng.playerFNGTransaction.domain.transaction.shop.FngShopItem;
import org.fng.playerFNGTransaction.domain.wallet.FNGWallet;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
import org.fng.playerFNGTransaction.infrastructure.FNGWalletRepository;
import org.fng.playerFNGTransaction.infrastructure.ShopManagerRepository;

import java.sql.SQLException;

public class FNGListener implements Listener {
    private static final float DEFAULT_VALUE = 100.0f;
    private final NamespacedKey WALLET_KEY;
    private final FNGWalletRepository fngWalletRepository;
    private final ShopManagerRepository shopManagerRepository;
    private final JavaPlugin plugin;
    public FNGListener(JavaPlugin plugin, FNGWalletRepository fngWalletRepository, ShopManagerRepository shopManagerRepository) {
        WALLET_KEY = new NamespacedKey(plugin, "wallet");
        this.fngWalletRepository = fngWalletRepository;
        this.plugin = plugin;
        this.shopManagerRepository = shopManagerRepository;
    }



    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
            if (event.getRightClicked() instanceof Villager villager) {
                if(villager.getPersistentDataContainer().has(new NamespacedKey(plugin, "fng_worker"))){
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    Player player = event.getPlayer();
                    try {
                        player.openInventory(this.shopManagerRepository.getShop(new NamespacedKey(plugin, "fng_worker").value()).getInventory());
                    }catch (SQLException e){
                        player.sendMessage("Erreur dans la récupération du shop...");
                    }
                    });
                }

            }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getInventory().getHolder() instanceof FngShop shop){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            event.setCancelled(true); // prevent item grab

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if(clickedItem instanceof FngShopItem shopItem){
                FNGWallet wallet = new FNGWallet(player, fngWalletRepository, plugin);
                player.sendMessage("Transaction effectué !");
            }
            });
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player player = event.getPlayer();
                this.onJoin(player);
                this.checkFirstJoin(player);
            })
        );

    }

    private void onJoin(Player player) {
        player.sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX
                .append(Component.text(" Bonjour ", NamedTextColor.GREEN))
                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                .append(Component.text("!", NamedTextColor.WHITE)));
        try {
            if(!fngWalletRepository.hasWallet(player.getUniqueId())){
                fngWalletRepository.insertWallet(player.getUniqueId(), DEFAULT_VALUE);
                player.sendMessage("Création du portefeuille réussie !");

                if(player.getPersistentDataContainer().has(WALLET_KEY, PersistentDataType.FLOAT)) {
                    new FNGWallet(player, this.fngWalletRepository, this.plugin).setAmount(player.getPersistentDataContainer().get(WALLET_KEY, PersistentDataType.FLOAT));
                    player.getPersistentDataContainer().remove(WALLET_KEY);
                    player.sendMessage("Migration vers la BD effectué avec succès !");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
