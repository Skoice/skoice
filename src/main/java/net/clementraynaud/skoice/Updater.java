/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

    private final Skoice plugin;
    private final String pluginPath;
    private String downloadedVersion;

    public Updater(Skoice plugin, String pluginPath) {
        this.plugin = plugin;
        this.pluginPath = pluginPath;
        this.plugin.getServer().getScheduler().runTaskTimer(
                this.plugin,
                this::checkVersion,
                Updater.TICKS_BEFORE_VERSION_CHECKING,
                Updater.TICKS_BETWEEN_VERSION_CHECKING
        );
    }

    private void checkVersion() {
        this.getVersion(version -> {
            if (version != null && !this.plugin.getDescription().getVersion().equals(version) && !version.equals(this.downloadedVersion)) {
                this.update(version);
            }
        });
    }

    private void getVersion(final Consumer<String> consumer) {
        this.plugin.getFoliaLib().getImpl().runAsync(() -> {
            try (InputStream inputStream = new URL("https://clementraynaud.net/files/skoice-latest/version")
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

        this.plugin.getFoliaLib().getImpl().runAsync(() -> {
            this.plugin.getServer().getUpdateFolderFile().mkdirs();

            try (FileOutputStream outputStream = new FileOutputStream(update)) {
                outputStream.getChannel()
                        .transferFrom(Channels.newChannel(new URL("https://clementraynaud.net/files/skoice-latest/Skoice.jar")
                                .openStream()), 0, Long.MAX_VALUE);
                this.downloadedVersion = version;
                this.plugin.getLogger().info(this.plugin.getLang().getMessage("logger.info.plugin-updated"));

            } catch (IOException exception) {
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.outdated-version",
                        this.plugin.getDescription().getVersion(), version));
                try {
                    Files.delete(update.getAbsoluteFile().toPath());
                } catch (IOException exception2) {
                    this.plugin.getBugsnag().notify(exception2);
                }
                this.plugin.getBugsnag().notify(exception);
            }
        });
    }
}