package net.clementraynaud.skoice.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.clementraynaud.skoice.common.model.JsonModel;
import net.clementraynaud.skoice.common.model.minecraft.PlayerInfo;
import net.clementraynaud.skoice.common.model.minecraft.ProxyInfo;
import net.clementraynaud.skoice.velocity.minecraft.VelocityBasePlayer;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "skoice", name = "Skoice", version = "3.2.3", authors = {"carlodrift", "Lucas_Cdry"})
public class SkoicePluginVelocity {

    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("skoice:main");
    @Inject
    private Logger logger;
    @Inject
    private ProxyServer proxy;
    private SkoiceVelocity skoice;
    @DataDirectory
    @Inject
    private Path dataDirectory;
    private String serverInfo;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.proxy.getChannelRegistrar().register(SkoicePluginVelocity.IDENTIFIER);
        this.skoice = new SkoiceVelocity(this);
        this.skoice.start();
        this.serverInfo = JsonModel.toJson(new ProxyInfo(this.skoice.getVersion()));
    }

    public Path getDataDirectory() {
        return this.dataDirectory;
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.skoice.shutdown();
    }

    @Subscribe
    public void onDisconnectEvent(DisconnectEvent event) {
        this.skoice.getListenerManager().onPlayerQuit(new VelocityBasePlayer(event.getPlayer()));
        this.skoice.removePlayerInfo(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        if (event.getPreviousServer() != null) {
            this.skoice.getListenerManager().onPlayerQuit(new VelocityBasePlayer(event.getPlayer())).thenAccept(aVoid -> {
                this.skoice.removePlayerInfo(event.getPlayer().getUniqueId());
                this.skoice.getListenerManager().onPlayerJoin(new VelocityBasePlayer(event.getPlayer()), false);
            });
        } else {
            this.skoice.getListenerManager().onPlayerJoin(new VelocityBasePlayer(event.getPlayer()), false);
        }

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF(this.serverInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.sendPluginMessageToBackendUsingPlayer(event.getPlayer(), SkoicePluginVelocity.IDENTIFIER, b.toByteArray());
    }

    @Subscribe
    public void onPlayerAvailableCommands(PlayerAvailableCommandsEvent event) {
        event.getRootNode().removeChildByName("skoice:skoice");
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!SkoicePluginVelocity.IDENTIFIER.equals(event.getIdentifier())) {
            return;
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!(event.getSource() instanceof ServerConnection backend)) {
            return;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(event.getData());
        DataInputStream in = new DataInputStream(bais);
        String json;
        try {
            json = in.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JsonModel model = JsonModel.fromJson(json, PlayerInfo.class);
        if (model instanceof PlayerInfo info) {
            info.setWorld(info.getWorld() + ":" + backend.getServerInfo().getName());
            this.skoice.setPlayerInfo(info);
        }
    }


    public boolean sendPluginMessageToBackendUsingPlayer(Player player, ChannelIdentifier identifier, byte[] data) {
        var connection = player.getCurrentServer();
        return connection.map(serverConnection -> serverConnection.sendPluginMessage(identifier, data)).orElse(false);
    }

    public Logger getLogger() {
        return this.logger;
    }

    public ProxyServer getProxy() {
        return this.proxy;
    }
}
