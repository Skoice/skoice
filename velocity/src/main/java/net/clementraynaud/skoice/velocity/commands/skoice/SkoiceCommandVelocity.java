package net.clementraynaud.skoice.velocity.commands.skoice;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.velocity.SkoiceVelocity;
import net.clementraynaud.skoice.common.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.velocity.minecraft.VelocityBasePlayer;
import net.clementraynaud.skoice.velocity.minecraft.VelocityCommandSender;

import java.util.List;

public class SkoiceCommandVelocity extends SkoiceCommand implements SimpleCommand {

    private final SkoiceVelocity plugin;

    public SkoiceCommandVelocity(Skoice plugin) {
        super(plugin);
        this.plugin = (SkoiceVelocity) plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.source() instanceof Player player) {
            super.onCommand(new VelocityBasePlayer(player), invocation.arguments());

        } else {
            super.onCommand(new VelocityCommandSender(invocation.source()), invocation.arguments());
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return super.onTabComplete(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return SimpleCommand.super.hasPermission(invocation);
    }

    @Override
    public void init() {
        CommandManager commandManager = this.plugin.getPlugin().getProxy().getCommandManager();
        commandManager.register(commandManager.metaBuilder("skoice")
                .plugin(this.plugin.getPlugin())
                .build(), this);
    }
}
