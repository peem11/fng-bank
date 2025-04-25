package org.fng.playerFNGTransaction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.api.*;
import org.fng.playerFNGTransaction.domain.transaction.TransactionManager;
import org.fng.playerFNGTransaction.domain.transaction.listener.FNGListener;
import org.fng.playerFNGTransaction.domain.wallet.SeeFundsCommand;
import org.fng.playerFNGTransaction.infrastructure.FNGWalletRepository;
import org.fng.playerFNGTransaction.infrastructure.ShopManagerRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;


public final class PlayerFNGTransaction extends JavaPlugin implements Listener {
    public static final TextComponent PLUGIN_PREFIX =
            Component.text("[", NamedTextColor.BLUE)
            .append(Component.text("BANQUE FNG", NamedTextColor.WHITE))
            .append(Component.text("]", NamedTextColor.BLUE));
    public static final String SEE_FUNDS_COMMAND = "solde";

    public static final String TRANSACT_COMMAND = "transaction";
    public static final String ACCEPT_COMMAND = "accepte";
    private static final String DECLINE_COMMAND = "refuse";
    private static final String GIVE_COMMAND = "donner";
    private static final String BROADCAST_COMMAND = "broadcast";
    private static final String SPAWN_WORKER_COMMAND = "spawnfngworker";
    public static final String CREATE_SHOP_COMMAND = "createshop";
    public static final String ADD_TO_SHOP_COMMAND = "addshopitem";
    private ShopManagerRepository shopManagerRepository;
    private FNGWalletRepository fngWalletRepository;

    @Override
    public void onEnable() {
        this.shopManagerRepository = new ShopManagerRepository(this);
        this.fngWalletRepository = new FNGWalletRepository(this);
        TransactionManager transactionManager = new TransactionManager(this);

        getCommand(CREATE_SHOP_COMMAND).setExecutor(new CreateShopCommand(shopManagerRepository));
        getCommand(ADD_TO_SHOP_COMMAND).setExecutor(new AddToShopCommand(shopManagerRepository));

        getCommand(SPAWN_WORKER_COMMAND).setExecutor(new SpawnFngWorkerCommand());
        getCommand(BROADCAST_COMMAND).setExecutor(new BroadcastCommand());
        getCommand(GIVE_COMMAND).setExecutor(new GiveCommand(this, this.fngWalletRepository));
        getCommand(DECLINE_COMMAND).setExecutor(new DeclineCommand(transactionManager, this.fngWalletRepository,this));
        getCommand(ACCEPT_COMMAND).setExecutor(new AcceptCommand(transactionManager, this.fngWalletRepository, this));
        getCommand(TRANSACT_COMMAND).setExecutor(new TransactionCommand(this, transactionManager, this.fngWalletRepository));
        getCommand(SEE_FUNDS_COMMAND).setExecutor(new SeeFundsCommand(this, this.fngWalletRepository));
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new FNGListener(this, this.fngWalletRepository, this.shopManagerRepository), this);

    }

    @Override
    public void onDisable() {
        if (fngWalletRepository != null) {
            try {
                fngWalletRepository.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (this.shopManagerRepository != null) {
            try {
                this.shopManagerRepository.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
