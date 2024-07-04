/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import net.clementraynaud.skoice.storage.config.ConfigField;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.function.Consumer;

public class Updater {

    private static final long TICKS_BETWEEN_VERSION_CHECKING = 720000L;
    private static final long TICKS_BEFORE_VERSION_CHECKING = 1200L;

    private static final String UPDATER_URL = "https://clementraynaud.net/files";

    private static final String VERSION_ENDPOINT = "/version";
    private static final String DOWNLOAD_ENDPOINT = "/Skoice.jar";

    private static final String LATEST_CHANNEL = "skoice-latest";
    private static final String BETA_CHANNEL = "skoice-beta";

    private final Skoice plugin;
    private final String pluginPath;
    private String fullURL;
    private String downloadedVersion;

    public Updater(Skoice plugin, String pluginPath) {
        this.plugin = plugin;
        this.pluginPath = pluginPath;
    }

    public void checkVersion() {
        this.updateReleaseChannel();
        this.getVersion(version -> {
            if (version != null && !this.plugin.getDescription().getVersion().equals(version) && !version.equals(this.downloadedVersion)) {
                this.update(version);
            }
        });
    }

    public void runUpdaterTaskTimer() {
        this.plugin.getServer().getScheduler().runTaskTimer(
                this.plugin,
                this::checkVersion,
                Updater.TICKS_BEFORE_VERSION_CHECKING,
                Updater.TICKS_BETWEEN_VERSION_CHECKING
        );
    }

    private void updateReleaseChannel() {
        String releaseChannel = Updater.LATEST_CHANNEL;
        if ("beta".equals(this.plugin.getConfigYamlFile().getString(ConfigField.RELEASE_CHANNEL.toString()))) {
            releaseChannel = Updater.BETA_CHANNEL;
        }
        this.fullURL = String.format("%s/%s", Updater.UPDATER_URL, releaseChannel);
    }

    private void getVersion(final Consumer<String> consumer) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL(this.fullURL + Updater.VERSION_ENDPOINT)
                    .openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException ignored) {
            }
        });
    }

    private void update(String version) {
        File update = new File(this.plugin.getServer().getUpdateFolderFile().getAbsolutePath() + File.separator
                + this.pluginPath.substring(this.pluginPath.lastIndexOf(File.separator) + 1));

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            this.plugin.getServer().getUpdateFolderFile().mkdirs();

            try (FileOutputStream outputStream = new FileOutputStream(update)) {
                outputStream.getChannel()
                        .transferFrom(Channels.newChannel(new URL(this.fullURL + Updater.DOWNLOAD_ENDPOINT)
                                .openStream()), 0, Long.MAX_VALUE);
                this.downloadedVersion = version;
                this.plugin.getLogger().info(this.plugin.getLang().getMessage("logger.info.plugin-updated"));

            } catch (IOException exception) {
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.outdated-version",
                        this.plugin.getDescription().getVersion(), version));
                try {
                    Files.delete(update.getAbsoluteFile().toPath());
                } catch (IOException ignored) {
                }
            }
        });
    }
}