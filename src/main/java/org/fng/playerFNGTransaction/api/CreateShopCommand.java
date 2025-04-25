package org.fng.playerFNGTransaction.api;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.fng.playerFNGTransaction.infrastructure.ShopManagerRepository;
import org.jetbrains.annotations.NotNull;

public class CreateShopCommand implements CommandExecutor {
    public CreateShopCommand(ShopManagerRepository shopManagerRepository) {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //TODO
        return false;
    }
}
