package org.fng.playerFNGTransaction.api;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;

public class SpawnFngWorkerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return true;
        if(args.length != 1) return true;

        Location location = player.getLocation();
        World world = player.getWorld();
        Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
        villager.customName(Component.text(args[0]));
        villager.setCustomNameVisible(true);
        villager.setPersistent(true);


        return true;
    }
}
