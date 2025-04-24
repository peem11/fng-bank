package org.fng.playerFNGTransaction.api;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;



public class BroadcastCommand implements CommandExecutor {

    public BroadcastCommand(){
      
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(args.length == 0){
            commandSender.sendMessage("Usage: /broadcast optionel:<groupe> <message>");
            return true;
        }

        List<Player> players = Bukkit.getOnlinePlayers().stream().map(Player::getPlayer).toList();
        if(args.length > 1){
            players = players.stream().filter(player -> player.hasPermission("group." + args[0])).toList();
            players.forEach(player -> {
                player.sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX.append(Component.text(" " + String.join(" ", Arrays.copyOfRange(args, 1, args.length)))));
            });
            return true;
        }
        players.forEach(player -> {
            player.sendMessage(PlayerFNGTransaction.PLUGIN_PREFIX.append(Component.text(" " + String.join(" ", Arrays.copyOfRange(args, 0, args.length)))));
        });

        return true;
    }
}
