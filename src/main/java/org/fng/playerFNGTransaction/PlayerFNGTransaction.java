package org.fng.playerFNGTransaction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.domain.transaction.AcceptCommand;
import org.fng.playerFNGTransaction.domain.transaction.TransactionCommand;
import org.fng.playerFNGTransaction.domain.transaction.TransactionManager;
import org.fng.playerFNGTransaction.domain.transaction.listener.FNGTransactionListener;
import org.fng.playerFNGTransaction.domain.wallet.SeeFundsCommand;

public final class PlayerFNGTransaction extends JavaPlugin implements Listener {
    public static final TextComponent PLUGIN_PREFIX =
            Component.text("[", NamedTextColor.BLUE)
            .append(Component.text("BANQUE FNG", NamedTextColor.WHITE))
            .append(Component.text("]", NamedTextColor.BLUE));
    public static final String SEE_FUNDS_COMMAND = "solde";

    public static final String TRANSACT_COMMAND = "transaction";
    public static final String ACCEPT_COMMAND = "accepte";

    @Override
    public void onEnable() {
        TransactionManager transactionManager = new TransactionManager(this);
        getCommand(ACCEPT_COMMAND).setExecutor(new AcceptCommand(transactionManager, new NamespacedKey(this, "wallet")));
        getCommand(TRANSACT_COMMAND).setExecutor(new TransactionCommand(this, transactionManager));
        getCommand(SEE_FUNDS_COMMAND).setExecutor(new SeeFundsCommand(this));
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new FNGTransactionListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


}
