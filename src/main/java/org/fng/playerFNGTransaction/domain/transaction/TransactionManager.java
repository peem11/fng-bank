package org.fng.playerFNGTransaction.domain.transaction;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
import org.fng.playerFNGTransaction.domain.wallet.FNGWallet;

import java.util.HashMap;
import java.util.Map;

public class TransactionManager{
    private final Map<String, PendingTransaction> pending = new HashMap<>();

    private final JavaPlugin plugin;
    public TransactionManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public void transact(Holder<Float,Float> holder, Receiver<Float> receiver, Float amount){
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (holder instanceof FNGWallet sender && receiver instanceof FNGWallet receiverPlayer) {
            /*if (sender.getPlayer().getName().equals(receiverPlayer.getPlayer().getName())) {
                sender.getPlayer().sendMessage("❌ Tu ne peux pas faire une transaction à toi-même.");
                return;
            }*/
                if(sender.getAmount() < amount){
                    sender.getPlayer().sendMessage("❌ Tu n'a pas assez de FNG sale pauvre");
                    return;
                }
                if(pending.containsValue(new PendingTransaction(sender, receiverPlayer, amount))) {
                    sender.getPlayer().sendMessage("❌ Tu ne peux pas effectuer 2 transactions en même temps. Veuillez attendre 20 secondes avant de recommencer.");
                }
                pending.put(receiverPlayer.getPlayer().getName(), new PendingTransaction(sender, receiverPlayer, amount));
                Bukkit.getScheduler().runTaskLater(plugin, ()->{
                    pending.remove(receiverPlayer.getPlayer().getName(), new PendingTransaction(sender, receiverPlayer, amount));
                },20L * 20);
                sendConfirmation(sender, receiverPlayer, amount);
            }
        });

    }

    private void sendConfirmation(FNGWallet sender, FNGWallet receiver, Float amount) {
        Bukkit.getScheduler().runTask(plugin, () -> {
        TextComponent message = Component.text("[").color(NamedTextColor.GRAY)
                .append(Component.text("ACCEPTER").color(NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand("/accepte " + sender.getPlayer().getName())))
                .append(Component.text("] [").color(NamedTextColor.GRAY).append(Component.text("REFUSER").color(NamedTextColor.RED)
                                .clickEvent(ClickEvent.runCommand("/refuse " + sender.getPlayer().getName())))
                        .append(Component.text("]").color(NamedTextColor.GRAY)));

        receiver.getPlayer().sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX
                .append(Component.text(" " + sender.getPlayer().getName(), NamedTextColor.YELLOW))
                .append(Component.text(" veut vous envoyer " + amount + "FNG.")));

        receiver.getPlayer().sendMessage(message);
        });
    }

    public boolean confirm(FNGWallet receiver) {
        PendingTransaction transaction = pending.remove(receiver.getPlayer().getName());
        if (transaction == null) return false;

        transaction.receiver.receive(transaction.holder.retrieve(transaction.amount));
        return true;
    }

    public boolean decline(FNGWallet receiverWallet) {
        PendingTransaction transaction = pending.remove(receiverWallet.getPlayer().getName());
        return transaction != null;
    }

    private record PendingTransaction(FNGWallet holder, FNGWallet receiver, Float amount) {
        @Override
        public boolean equals(Object o){
            if(o instanceof PendingTransaction transaction) {
                return holder.getPlayer().getName().equals(transaction.holder.getPlayer().getName()) && receiver.getPlayer().getName().equals(transaction.receiver.getPlayer().getName());
            }
            return false;
        }
    }
}