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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.Scanner;
import java.util.function.Consumer;

public class Updater {

    private final Skoice plugin;
    private final int resourceId;

    public Updater(Skoice plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void checkVersion() {
        this.getVersion(version -> {
            if (!this.plugin.getDescription().getVersion().equals(version)) {
                this.update(version);
            }
        });
    }

    private void getVersion(final Consumer<String> consumer) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId)
                    .openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException ignored) {
            }
        });
    }

    private void update(String version) {
        String fileName = this.plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        String fileSuffix = "-temp";

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (FileOutputStream outputStream = new FileOutputStream(fileName + fileSuffix)) {
                outputStream.getChannel()
                        .transferFrom(Channels.newChannel(new URL("https://api.spiget.org/v2/resources/" + this.resourceId + "/versions/latest/download")
                                .openStream()), 0, Long.MAX_VALUE);

                new File(fileName).delete();
                new File(fileName + fileSuffix).renameTo(new File(fileName));

                this.plugin.getLogger().info("logger.info.plugin-updated");
            } catch (IOException e) {
                new File(fileName + fileSuffix).delete();

                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.outdated-version",
                        this.plugin.getDescription().getVersion(), version));
            }
        });
    }
}