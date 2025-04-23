package org.fng.playerFNGTransaction.domain.wallet;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
import org.fng.playerFNGTransaction.domain.wallet.exceptions.InsufficientFundsException;
import org.fng.playerFNGTransaction.infrastructure.FNGWalletRepository;

import java.sql.SQLException;

/**
 * ⚠️ This object performs a blocking SQL queries.
 * Must NOT be called on the main server thread.
 */
public class FNGWallet implements Wallet<Float> {

    public static float START_AMOUNT = 100.0f;

    private final FNGWalletRepository fngWalletRepository;
    private final JavaPlugin plugin;
    private final Player player;
    public FNGWallet(Player player, FNGWalletRepository repository, JavaPlugin plugin1) {
        this.fngWalletRepository = repository;
        this.player = player;
        this.plugin = plugin1;
    }
    @Override
    public Float retrieve(Float amount) {
        if(this.getAmount() < amount){
            throw new InsufficientFundsException();
        }
        this.removeAmount(amount);
        return amount;
    }

    @Override
    public void receive(Float amount) {
        this.addAmount(amount);
    }

    public boolean playerHasWallet(){
        try {
            return this.fngWalletRepository.hasWallet(player.getUniqueId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeAmount(Float amount){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try{
                this.fngWalletRepository.remove(this.player.getUniqueId(), amount);
            }catch (SQLException e){
                if(e.getMessage().contains("CHECK constraint failed")){
                    this.player.sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX.append(Component.text(" Tu n'as pas assez de FNG!", NamedTextColor.RED)));
                }
            }
        });
    }
    public void addAmount(Float amount){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try{
                this.fngWalletRepository.add(this.player.getUniqueId(), amount);
            }catch (SQLException e){
                this.player.sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX.append(Component.text(" erreur", NamedTextColor.RED)));
            }
        });
    }
   public float getAmount() {
        if(!this.playerHasWallet()) throw new RuntimeException("Le joueur n'a pas de portefeuil !");
        float amount = 0.0f;
        try {
            amount = this.fngWalletRepository.getPlayerFNG(this.player.getUniqueId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return amount;
    }


    public void setAmount(Float amount) {
        try {
            this.fngWalletRepository.setAmount(player.getUniqueId(), amount);
        } catch (SQLException e) {
            if(e.getMessage().contains("CHECK constraint failed")){
                player.sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX.append(Component.text(" Tu n'as pas assez de FNG!", NamedTextColor.RED)));
            }
        }
    }

    public Player getPlayer() {
        return this.player;
    }

}
