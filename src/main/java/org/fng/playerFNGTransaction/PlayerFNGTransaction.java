package org.fng.playerFNGTransaction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class PlayerFNGTransaction extends JavaPlugin implements Listener {


    private static final String SELL_COMMAND = "vendre";
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
    public static final String ADD_TO_SHOP_COMMAND = "addtoshop";
    private ShopManagerRepository shopManagerRepository;
    private FNGWalletRepository fngWalletRepository;

    @Override
    public void onEnable() {

        this.shopManagerRepository = new ShopManagerRepository(this);
        this.fngWalletRepository = new FNGWalletRepository(this);
        TransactionManager transactionManager = new TransactionManager(this);


        getCommand(SELL_COMMAND).setExecutor(new SellCommand(this, this.fngWalletRepository));
        getCommand(CREATE_SHOP_COMMAND).setExecutor(new CreateShopCommand(this,shopManagerRepository));
        getCommand(ADD_TO_SHOP_COMMAND).setExecutor(new AddToShopCommand(this,shopManagerRepository));
        getCommand(SPAWN_WORKER_COMMAND).setExecutor(new SpawnFngWorkerCommand(this));

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

    public static List<String> splitArguments(String input) {
        List<String> args = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(input);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                args.add(matcher.group(1));
            } else {
                args.add(matcher.group(2));
            }
        }
        return args;
    }
}
