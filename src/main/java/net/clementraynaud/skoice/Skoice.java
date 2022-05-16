/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 *
 * This file is part of Skoice.
 *
 * Skoice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skoice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skoice.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.clementraynaud.skoice;

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.config.OutdatedConfig;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.lang.LangFile;
import net.clementraynaud.skoice.listeners.channel.voice.network.VoiceChannelDeleteListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceJoinListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceLeaveListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceMoveListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerJoinListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerMoveListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerQuitListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerTeleportListener;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.menus.MenuType;
import net.clementraynaud.skoice.menus.Response;
import net.clementraynaud.skoice.system.EligiblePlayers;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.clementraynaud.skoice.util.UpdateUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Skoice extends JavaPlugin {

    private static final int SERVICE_ID = 11380;
    public static final int RESSOURCE_ID = 82861;

    protected Skoice plugin;
    protected LangFile lang;
    protected Config config;
    protected Bot bot;
    private EligiblePlayers eligiblePlayers;

    @Override
    public void onEnable() {
        new Metrics(this, Skoice.SERVICE_ID);
        this.plugin = this;
        this.initializeConfig();
        this.initializeLang();
        this.getLogger().info(this.lang.getMessage("logger.info.plugin-enabled"));
        new OutdatedConfig(this, this.config).update();
        this.eligiblePlayers = new EligiblePlayers();
        this.initializeBot();
        this.initializeSkoiceCommand();
        new UpdateUtil(this, Skoice.RESSOURCE_ID, this.lang.getMessage("logger.warning.outdated-version")).checkVersion();
    }

    @Override
    public void onDisable() {
        if (this.bot.getJda() != null) {
            new InterruptSystemTask(this.config).run();
            this.bot.getJda().shutdown();
        }
        this.getLogger().info(this.lang.getMessage("logger.info.plugin-disabled"));
    }

    private void initializeConfig() {
        this.config = new Config(this, this.getConfig());
        this.config.getFile().options().copyDefaults(true);
        this.config.saveFile();
        this.config.initializeUpdater();
    }

    private void initializeLang() {
        this.lang = new LangFile();
        this.lang.load(Lang.valueOf(this.config.getFile().getString(ConfigField.LANG.get())));
    }

    private void initializeBot() {
        this.bot = new Bot(this, this.config, this.lang, this.eligiblePlayers);
        this.bot.connect(null);
        if (this.bot.getJda() != null) {
            this.config.initializeReader(this.bot);
            this.bot.setup(true, null);
        }
    }

    private void initializeSkoiceCommand() {
        SkoiceCommand skoiceCommand = new SkoiceCommand();
        this.getCommand("skoice").setExecutor(skoiceCommand);
        this.getCommand("skoice").setTabCompleter(skoiceCommand);
    }

    public void updateConfigurationStatus(boolean startup) {
        boolean wasBotReady = this.bot.isReady();
        if (!this.config.getFile().contains(ConfigField.TOKEN.get())) {
            this.bot.setReady(false);
            this.getLogger().warning(this.lang.getMessage("logger.warning.no-token"));
        } else if (this.bot.getJda() == null) {
            this.bot.setReady(false);
        } else if (this.bot.isOnMultipleGuilds()) {
            this.bot.setReady(false);
            this.getLogger().warning(this.lang.getMessage("logger.warning.multiple-guilds"));
        } else if (!this.config.getFile().contains(ConfigField.LOBBY_ID.get())) {
            this.bot.setReady(false);
            this.getLogger().warning(this.lang.getMessage("logger.warning.no-lobby-id"));
        } else if (!this.config.getFile().contains(ConfigField.HORIZONTAL_RADIUS.get())
                || !this.config.getFile().contains(ConfigField.VERTICAL_RADIUS.get())) {
            this.bot.setReady(false);
            this.getLogger().warning(this.lang.getMessage("logger.warning.no-radius"));
        } else {
            this.bot.setReady(true);
        }
        this.updateActivity();
        this.updateListeners(startup, wasBotReady);
    }

    private void updateActivity() {
        if (this.bot.getJda() != null) {
            Activity activity = this.bot.getJda().getPresence().getActivity();
            if (this.bot.isReady() && !Objects.equals(activity, Activity.listening("/link"))) {
                this.bot.getJda().getPresence().setActivity(Activity.listening("/link"));
            } else if (!this.bot.isReady() && !Objects.equals(activity, Activity.listening("/configure"))) {
                this.bot.getJda().getPresence().setActivity(Activity.listening("/configure"));
            }
        }
    }

    private void updateListeners(boolean startup, boolean wasBotReady) {
        if (startup) {
            if (this.bot.isReady()) {
                this.registerEligiblePlayerListeners();
                this.bot.getJda().addEventListener(new GuildVoiceJoinListener(this.config, this.lang, this.eligiblePlayers), new GuildVoiceLeaveListener(this.config, this.lang),
                        new GuildVoiceMoveListener(this.config), new VoiceChannelDeleteListener());
            } else {
                Bukkit.getPluginManager().registerEvents(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this.config, this.lang, this.bot), this);
            }
        } else if (!wasBotReady && this.bot.isReady()) {
            HandlerList.unregisterAll(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this.config, this.lang, this.bot));
            this.registerEligiblePlayerListeners();
            this.bot.getJda().addEventListener(new GuildVoiceJoinListener(this.config, this.lang, this.eligiblePlayers), new GuildVoiceLeaveListener(this.config, this.lang),
                    new GuildVoiceMoveListener(this.config), new VoiceChannelDeleteListener());
            this.getLogger().info(this.lang.getMessage("logger.info.configuration-complete"));
            Message configurationMessage = new Response(this, this.config, this.lang, this.bot).getConfigurationMessage();
            if (configurationMessage != null && configurationMessage.getInteraction() != null) {
                configurationMessage.getInteraction().getUser().openPrivateChannel().complete()
                        .sendMessageEmbeds(new EmbedBuilder().setTitle(MenuEmoji.GEAR + this.lang.getMessage("discord.menu.configuration.title"))
                                .addField(MenuEmoji.HEAVY_CHECK_MARK + this.lang.getMessage("discord.field.configuration-complete.title"),
                                        this.lang.getMessage("discord.field.configuration-complete.description"), false)
                                .setColor(MenuType.SUCCESS.getColor()).build())
                        .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
            }
        } else if (wasBotReady && !this.bot.isReady()) {
            new Response(this, this.config, this.lang, this.bot).deleteMessage();
            this.unregisterEligiblePlayerListeners();
            Bukkit.getPluginManager().registerEvents(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this.config, this.lang, this.bot), this);
            if (this.bot.getJda() != null) {
                this.bot.getJda().removeEventListener(new GuildVoiceJoinListener(this.config, this.lang, this.eligiblePlayers), new GuildVoiceLeaveListener(this.config, this.lang),
                        new GuildVoiceMoveListener(this.config), new VoiceChannelDeleteListener());
            }
            new InterruptSystemTask(this.config).run();
        }
    }

    private void registerEligiblePlayerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this.config, this.lang, this.eligiblePlayers), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this.eligiblePlayers), this);
        Bukkit.getPluginManager().registerEvents(new PlayerTeleportListener(this.eligiblePlayers), this);
    }

    private void unregisterEligiblePlayerListeners() {
        HandlerList.unregisterAll(new PlayerJoinListener(this.config, this.lang, this.eligiblePlayers));
        HandlerList.unregisterAll(new PlayerQuitListener(this));
        HandlerList.unregisterAll(new PlayerMoveListener(this.eligiblePlayers));
        HandlerList.unregisterAll(new PlayerTeleportListener(this.eligiblePlayers));
    }
}
