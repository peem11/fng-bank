package org.fng.playerFNGTransaction.api;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.fng.playerFNGTransaction.PlayerFNGTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SpawnFngWorkerCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SpawnFngWorkerCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return true;
        List<String> argsList = PlayerFNGTransaction.splitArguments(String.join(" ", args));

        if(argsList.size() != 2) return true;

        Location location = player.getLocation();
        World world = player.getWorld();
        Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
        villager.customName(Component.text(argsList.get(1)));
        villager.setCustomNameVisible(true);
        villager.setPersistent(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.getPersistentDataContainer().set(new NamespacedKey(plugin, "fng_worker"), PersistentDataType.STRING, argsList.getFirst());

        return true;
    }

}
