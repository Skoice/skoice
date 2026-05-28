/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.util;

import net.clementraynaud.skoice.common.Skoice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MojangUsernameCache {

    private static final String PROFILE_ENDPOINT = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Pattern NAME_FIELD = Pattern.compile("\"name\"\\s*:\\s*\"([A-Za-z0-9_]{1,16})\"");
    private static final Map<UUID, String> CACHE = new ConcurrentHashMap<>();

    private MojangUsernameCache() {
    }

    public static String getCached(UUID uuid) {
        return MojangUsernameCache.CACHE.get(uuid);
    }

    public static void retrieve(Skoice plugin, UUID uuid, Consumer<String> callback) {
        if (uuid == null) {
            callback.accept(null);
            return;
        }
        String cached = MojangUsernameCache.CACHE.get(uuid);
        if (cached != null) {
            callback.accept(cached);
            return;
        }
        plugin.getScheduler().runTaskAsynchronously(() -> {
            String name = MojangUsernameCache.fetch(uuid);
            if (name != null) {
                MojangUsernameCache.CACHE.put(uuid, name);
            }
            callback.accept(name);
        });
    }

    private static String fetch(UUID uuid) {
        try {
            URL url = new URL(MojangUsernameCache.PROFILE_ENDPOINT + uuid.toString().replace("-", ""));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String body = reader.lines().collect(Collectors.joining());
                Matcher matcher = MojangUsernameCache.NAME_FIELD.matcher(body);
                return matcher.find() ? matcher.group(1) : null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
