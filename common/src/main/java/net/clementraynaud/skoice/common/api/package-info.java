/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

/**
 * Skoice Developer API
 * <p>
 * This package contains the main API entry points and event handling interfaces
 * for developers building plugins that integrate with Skoice.
 *
 * <h2>Overview</h2>
 * <p>The Skoice API allows you to:
 * <ul>
 *   <li>Check if players are connected to proximity voice chat</li>
 *   <li>Query and manage Discord-Minecraft account links</li>
 *   <li>Monitor proximity voice chat system status</li>
 *   <li>Listen to events for player connections, account linking, and system state changes</li>
 * </ul>
 *
 * <h2>Installation</h2>
 *
 * <h3>Maven Dependency</h3>
 * <p>Currently, Skoice is not published to a public Maven repository. You'll need to clone
 * and compile Skoice locally, then install it into your local Maven repository.
 *
 * <h4>For Spigot Plugins:</h4>
 * <pre>{@code
 * <dependency>
 *     <groupId>net.clementraynaud</groupId>
 *     <artifactId>skoice.spigot</artifactId>
 *     <version>3.2.9</version>
 *     <scope>provided</scope>
 *     <exclusions>
 *         <exclusion>
 *             <groupId>net.clementraynaud</groupId>
 *             <artifactId>skoice.common</artifactId>
 *         </exclusion>
 *     </exclusions>
 * </dependency>
 * }</pre>
 *
 * <h4>For Velocity Plugins:</h4>
 * <pre>{@code
 * <dependency>
 *     <groupId>net.clementraynaud</groupId>
 *     <artifactId>skoice.common</artifactId>
 *     <version>3.2.9</version>
 *     <scope>provided</scope>
 * </dependency>
 * }</pre>
 *
 * <h3>Plugin Configuration</h3>
 *
 * <h4>Spigot (plugin.yml):</h4>
 * <pre>{@code
 * depend: [Skoice]
 * }</pre>
 *
 * <h4>Velocity:</h4>
 * <pre>{@code
 * @Plugin(
 *     id = "your-plugin-id",
 *     name = "YourPlugin",
 *     version = "1.0.0",
 *     dependencies = {
 *         @Dependency(id = "skoice")
 *     }
 * )
 * }</pre>
 *
 * <h2>Quick Start</h2>
 *
 * <h3>Accessing the API</h3>
 * <pre>{@code
 * import net.clementraynaud.skoice.common.Skoice;
 * import net.clementraynaud.skoice.common.SkoiceAPI;
 * import net.clementraynaud.skoice.common.EventBus;
 *
 * public class YourPlugin extends JavaPlugin {
 *     private SkoiceAPI skoice;
 *     private EventBus eventBus;
 *
 *     @Override
 *     public void onEnable() {
 *         try {
 *             this.skoice = Skoice.api();
 *             this.eventBus = Skoice.eventBus();
 *
 *             if (this.skoice == null || this.eventBus == null) {
 *                 throw new RuntimeException("Skoice API is not available");
 *             }
 *
 *             // API is ready to use
 *             setupEventListeners();
 *         } catch (Throwable t) {
 *             t.printStackTrace();
 *             getServer().getPluginManager().disablePlugin(this);
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>Checking Player Status</h3>
 * <pre>{@code
 * UUID playerUuid = player.getUniqueId();
 *
 * // Check if player has linked their Discord account
 * if (skoice.isLinked(playerUuid)) {
 *     // Player has linked their account
 * }
 *
 * // Check if player is connected to proximity chat
 * if (skoice.isProximityConnected(playerUuid)) {
 *     // Player is in proximity chat
 * }
 *
 * // Check if proximity chat system is running
 * if (skoice.isSystemReady()) {
 *     // Proximity chat is active
 * }
 * }</pre>
 *
 * <h3>Subscribing to Events</h3>
 * <pre>{@code
 * // Listen for players connecting to proximity chat
 * eventBus.subscribe(PlayerProximityConnectEvent.class, event -> {
 *     UUID playerId = event.getMinecraftId();
 *     String discordId = event.getDiscordId();
 *     // Player connected to proximity chat
 * });
 *
 * // Listen for players disconnecting from proximity chat
 * eventBus.subscribe(PlayerProximityDisconnectEvent.class, event -> {
 *     UUID playerId = event.getMinecraftId();
 *     // Player disconnected from proximity chat
 * });
 *
 * // Listen for system ready
 * eventBus.subscribe(SystemReadyEvent.class, event -> {
 *     // Proximity chat is now running
 * });
 *
 * // Listen for system interruption
 * eventBus.subscribe(SystemInterruptionEvent.class, event -> {
 *     // Proximity chat stopped running
 * });
 * }</pre>
 *
 * <h2>Complete Example: Proximity Chat Requirement Plugin</h2>
 * <p>This example shows how to create a plugin that requires players to be in proximity chat,
 * displaying alerts until they connect.
 *
 * <h3>Spigot Implementation</h3>
 * <pre>{@code
 * public class ProximityChatRequired extends JavaPlugin implements Listener {
 *     private final Map<UUID, Integer> alertTasks = new HashMap<>();
 *     private SkoiceAPI skoice;
 *
 *     @Override
 *     public void onEnable() {
 *         EventBus eventBus;
 *         try {
 *             this.skoice = Skoice.api();
 *             eventBus = Skoice.eventBus();
 *             if (this.skoice == null || eventBus == null) {
 *                 throw new RuntimeException("Skoice API is not available");
 *             }
 *         } catch (Throwable t) {
 *             t.printStackTrace();
 *             getServer().getPluginManager().disablePlugin(this);
 *             return;
 *         }
 *
 *         getServer().getPluginManager().registerEvents(this, this);
 *
 *         eventBus.subscribe(PlayerProximityConnectEvent.class, event -> {
 *             if (isPlayerAlerted(event.getMinecraftId())) {
 *                 getServer().getScheduler().cancelTask(alertTasks.get(event.getMinecraftId()));
 *                 alertTasks.remove(event.getMinecraftId());
 *             }
 *         });
 *
 *         eventBus.subscribe(PlayerProximityDisconnectEvent.class, event -> {
 *             if (skoice.isSystemReady()) {
 *                 alert(getServer().getPlayer(event.getMinecraftId()));
 *             }
 *         });
 *
 *         eventBus.subscribe(SystemReadyEvent.class, event -> {
 *             getServer().getOnlinePlayers().forEach(player -> {
 *                 if (!skoice.isProximityConnected(player.getUniqueId())) {
 *                     alert(player);
 *                 }
 *             });
 *         });
 *
 *         eventBus.subscribe(SystemInterruptionEvent.class, event -> {
 *             alertTasks.keySet().forEach(uuid ->
 *                 getServer().getScheduler().cancelTask(alertTasks.get(uuid))
 *             );
 *             alertTasks.clear();
 *         });
 *     }
 *
 *     private boolean isPlayerAlerted(UUID uuid) {
 *         return alertTasks.containsKey(uuid);
 *     }
 *
 *     private void alert(Player player) {
 *         if (player != null && player.isOnline() && !isPlayerAlerted(player.getUniqueId())) {
 *             int taskId = getServer().getScheduler().runTaskTimer(this, () -> {
 *                 if (player.isOnline()) {
 *                     player.sendTitlePart(TitlePart.TITLE, Component.text("Proximity Chat is Required!"));
 *                     player.sendTitlePart(TitlePart.TIMES, Title.Times.times(
 *                             Duration.ofMillis(0), Duration.ofMillis(800), Duration.ofMillis(0)));
 *                 } else {
 *                     getServer().getScheduler().cancelTask(alertTasks.get(player.getUniqueId()));
 *                     alertTasks.remove(player.getUniqueId());
 *                 }
 *             }, 0, 8).getTaskId();
 *             alertTasks.put(player.getUniqueId(), taskId);
 *         }
 *     }
 *
 *     @EventHandler
 *     public void onPlayerJoin(PlayerJoinEvent event) {
 *         getServer().getScheduler().runTaskLater(this, () -> {
 *             if (!skoice.isProximityConnected(event.getPlayer().getUniqueId())
 *                     && skoice.isSystemReady()) {
 *                 alert(event.getPlayer());
 *             }
 *         }, 40);
 *     }
 *
 *     @EventHandler
 *     public void onPlayerQuit(PlayerQuitEvent event) {
 *         if (isPlayerAlerted(event.getPlayer().getUniqueId())) {
 *             getServer().getScheduler().cancelTask(alertTasks.get(event.getPlayer().getUniqueId()));
 *             alertTasks.remove(event.getPlayer().getUniqueId());
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h2>API Reference</h2>
 *
 * <h3>Main Classes</h3>
 * <ul>
 *   <li>{@link net.clementraynaud.skoice.common.SkoiceAPI} - Main API for querying player and system state</li>
 *   <li>{@link net.clementraynaud.skoice.common.EventBus} - Event subscription system</li>
 *   <li>{@link net.clementraynaud.skoice.common.EventHandler} - Handle for managing event subscriptions</li>
 * </ul>
 *
 * <h3>Events</h3>
 * <ul>
 *   <li>{@link net.clementraynaud.skoice.common.api.events.player.PlayerProximityConnectEvent} - Player connects to proximity chat</li>
 *   <li>{@link net.clementraynaud.skoice.common.api.events.player.PlayerProximityDisconnectEvent} - Player disconnects from proximity chat</li>
 *   <li>{@link net.clementraynaud.skoice.common.api.events.account.AccountLinkEvent} - Player links their Discord account</li>
 *   <li>{@link net.clementraynaud.skoice.common.api.events.account.AccountUnlinkEvent} - Player unlinks their Discord account</li>
 *   <li>{@link net.clementraynaud.skoice.common.api.events.system.SystemReadyEvent} - Proximity chat system starts</li>
 *   <li>{@link net.clementraynaud.skoice.common.api.events.system.SystemInterruptionEvent} - Proximity chat system stops</li>
 * </ul>
 *
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Always check if the API is not null before using it</li>
 *   <li>Use {@code isSystemReady()} to determine if proximity chat is active before enforcing requirements</li>
 *   <li>Subscribe to {@code SystemReadyEvent} and {@code SystemInterruptionEvent} to handle system state changes</li>
 *   <li>Clean up event handlers and scheduled tasks when your plugin disables</li>
 *   <li>Account link/unlink events are only fired for in-game operations, not for programmatic API calls</li>
 *   <li>All event handlers are executed asynchronously on a dedicated event thread</li>
 * </ul>
 *
 * @see net.clementraynaud.skoice.common.SkoiceAPI
 * @see net.clementraynaud.skoice.common.EventBus
 */
package net.clementraynaud.skoice.common.api;
