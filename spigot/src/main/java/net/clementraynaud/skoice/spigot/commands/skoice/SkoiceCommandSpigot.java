package net.clementraynaud.skoice.spigot.commands.skoice;

import net.clementraynaud.skoice.common.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.spigot.SkoicePluginSpigot;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.spigot.minecraft.SpigotBasePlayer;
import net.clementraynaud.skoice.spigot.minecraft.SpigotCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SkoiceCommandSpigot extends SkoiceCommand implements CommandExecutor, TabCompleter {

    private final SkoiceSpigot plugin;

    public SkoiceCommandSpigot(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void init() {
        PluginCommand skoiceCommand = this.plugin.getPlugin().getCommand("skoice");
        if (skoiceCommand != null) {
            skoiceCommand.setExecutor(this);
            skoiceCommand.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (SkoicePluginSpigot.isProxyMode()) {
            sender.sendMessage("Proxy mode enabled.");
            return true;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return super.onCommand(new SpigotBasePlayer(player), args);
        } else {
            return super.onCommand(new SpigotCommandSender(sender), args);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (SkoicePluginSpigot.isProxyMode()) {
            return new ArrayList<>();
        }
        return super.onTabComplete(new SpigotCommandSender(sender), args);
    }
}
