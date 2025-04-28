package org.fng.playerFNGTransaction.domain.transaction.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.transaction.shop.FngSellShop;
import org.fng.playerFNGTransaction.domain.transaction.shop.FngShop;
import org.fng.playerFNGTransaction.domain.wallet.FNGWallet;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
import org.fng.playerFNGTransaction.infrastructure.FNGWalletRepository;
import org.fng.playerFNGTransaction.infrastructure.ShopManagerRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.fng.playerFNGTransaction.domain.transaction.shop.FngSellShop.computeAmount;

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
            if (villager.getPersistentDataContainer().has(new NamespacedKey(plugin, "fng_worker"))) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    Player player = event.getPlayer();
                    try {
                        Inventory inventory = this.shopManagerRepository.getShop(
                                        Objects.requireNonNull(villager.getPersistentDataContainer()
                                                .get(new NamespacedKey(plugin, "fng_worker"),
                                                        PersistentDataType.STRING)))
                                .getInventory(new FNGWallet(player, this.fngWalletRepository, plugin));
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.openInventory(inventory);
                        });
                    } catch (SQLException e) {
                        player.sendMessage("Erreur dans la récupération du shop...");
                    }
                });
            }

        }

    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        if (inv.getHolder() instanceof FngSellShop shop){
            if(shop.succeeded()) return;
            for (int slot = 0; slot < 45; slot++) {
                ItemStack item = inv.getItem(slot);
                if (item == null || item.getType().isAir()) continue;

                Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);

                overflow.values().forEach(leftOver ->
                        player.getWorld().dropItemNaturally(player.getLocation(), leftOver));

                inv.clear(slot);
            }
        }

    }


    private void updateConfirmLore(Inventory gui) {
        float amount = computeAmount(gui);

        ItemStack confirm = gui.getItem(45);
        if (confirm == null || confirm.getType() != Material.GREEN_STAINED_GLASS_PANE) return;

        ItemMeta meta = confirm.getItemMeta();
        meta.lore(List.of(
                Component.text("Clique pour valider"),
                Component.text("Valeur du lot : §a" + amount + " FNG")
        ));
        confirm.setItemMeta(meta);
        gui.setItem(45, confirm);   // pousse la mise à jour
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getHolder() instanceof FngSellShop shop) {
            int slot = event.getSlot();
            Bukkit.getScheduler().runTask(
                    shop.getPlugin(),
                    () -> updateConfirmLore(event.getView().getTopInventory()));

            // ----- Barre du bas -----
            if (slot >= 45 && slot <= 53) {
                event.setCancelled(true);               // empêche de déplacer l’item

                ItemStack clicked = event.getCurrentItem();
                if (clicked == null) return;

                switch (clicked.getType()) {
                    case GREEN_STAINED_GLASS_PANE -> {
                        if(FngSellShop.computeAmount(event.getView().getTopInventory()) <= 0) return;
                        event.getInventory().setItem(45, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
                        shop.startTransaction(event.getView().getTopInventory());
                        shop.setSucceeded();
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.closeInventory();
                    }
                    case RED_STAINED_GLASS_PANE -> player.closeInventory(); // annule
                    default -> { /* tête du joueur ou autre → rien */ }
                }
                return;
            }
        }

        if (event.getInventory().getHolder() instanceof FngShop shop) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

                if (clickedItem.getPersistentDataContainer().has(new NamespacedKey(plugin, "fngshopitem"))) {
                    FNGWallet wallet = new FNGWallet(player, fngWalletRepository, plugin);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if(player.getInventory().firstEmpty() != -1) {

                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                try {
                                    wallet.removeAmount(clickedItem.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "price"), PersistentDataType.FLOAT));
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        player.sendMessage("Transaction effectué !");
                                        player.getInventory().addItem(clickedItem);
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                    });
                                }catch (Exception e) {
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                    });
                                }
                            });


                        }else{
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            player.sendMessage("Inventaire plein.");
                        }

                    });
                }
                if (clickedItem.getPersistentDataContainer().has(new NamespacedKey(plugin, "next"))) {
                    shop.nextPage();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.openInventory(shop.getInventory());
                    });
                }
                if (clickedItem.getPersistentDataContainer().has(new NamespacedKey(plugin, "prev"))) {
                    shop.prevPage();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.openInventory(shop.getInventory());
                    });
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
            if (!fngWalletRepository.hasWallet(player.getUniqueId())) {
                fngWalletRepository.insertWallet(player.getUniqueId(), DEFAULT_VALUE);
                player.sendMessage("Création du portefeuille réussie !");

                if (player.getPersistentDataContainer().has(WALLET_KEY, PersistentDataType.FLOAT)) {
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
        if (!player.hasPlayedBefore()) {
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
                        .append(Component.text("Avec cette monaie, tu peux acheter à des joueurs et à des villageois.", NamedTextColor.WHITE))
                        .append(Component.text(" /" + PlayerFNGTransaction.SEE_FUNDS_COMMAND, NamedTextColor.YELLOW))
                        .append(Component.text(" pour voir ton solde", NamedTextColor.WHITE));
                player.sendMessage(fngExplanationMessage);
            }, 20L * 10);

        }
    }
}
