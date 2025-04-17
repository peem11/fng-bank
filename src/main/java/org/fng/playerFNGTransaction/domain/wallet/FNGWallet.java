package org.fng.playerFNGTransaction.domain.wallet;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.fng.playerFNGTransaction.domain.wallet.exceptions.InsufficientFundsException;
import org.fng.playerFNGTransaction.domain.wallet.exceptions.WalletKeyFetchException;

import java.util.Optional;

public class FNGWallet implements Wallet<Float> {

    public static float START_AMOUNT = 100.0f;
    private final NamespacedKey key;
    private final Player player;
    public FNGWallet(Player player, NamespacedKey walletKey) {
        this.key = walletKey;
        this.player = player;
    }
    @Override
    public Float retrieve(Float amount) {
        if(this.getAmount() < amount){
            throw new InsufficientFundsException();
        }
        this.setAmount(this.getAmount() - amount);
        return amount;
    }

    @Override
    public void receive(Float amount) {
        this.setAmount(this.getAmount() + amount);
    }
    public float getAmount() {
        Optional<Float> keyOptional = Optional.ofNullable(this.player.getPersistentDataContainer().get(this.key, PersistentDataType.FLOAT));
        return keyOptional.orElseThrow(()->new WalletKeyFetchException("Erreur pendant la récupération de la clé du portefeuille"));
    }

    public void setAmount(Float amount) {
        this.player.getPersistentDataContainer().set(this.key, PersistentDataType.FLOAT, amount);
    }

    public Player getPlayer() {
        return this.player;
    }

}
