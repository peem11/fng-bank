package org.fng.playerFNGTransaction.api;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
import org.jetbrains.annotations.NotNull;
import java.util.List;



public class BroadcastCommand implements CommandExecutor {

    public BroadcastCommand(){
      
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        List<Player> players = Bukkit.getOnlinePlayers().stream().map(Player::getPlayer).toList();
        if(args.length > 1){
            players = players.stream().filter(player -> player.hasPermission("group." + args[0])).toList();
            players.forEach(player -> {
                player.sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX + " " + args[1]);
            });
            return true;
        }
        players.forEach(player -> {
            player.sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX + " " + args[0]);
        });

        return true;
    }
}
