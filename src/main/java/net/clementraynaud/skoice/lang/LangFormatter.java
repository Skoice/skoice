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

package net.clementraynaud.skoice.lang;

import java.util.HashMap;
import java.util.Map;

public class LangFormatter {

    private static final char START_SEPARATOR = '{';
    private static final char END_SEPARATOR = '}';

    protected final Map<String, String> defaultArgs = new HashMap<>();

    public void set(String key, String value) {
        this.defaultArgs.put(key, value);
    }

    public boolean contains(String key) {
        return this.defaultArgs.containsKey(key);
    }

    public String get(String key) {
        return this.defaultArgs.get(key);
    }

    public String format(String message, Map<String, String> args) {
        StringBuilder result = new StringBuilder();

        int i = 0;
        int start = message.indexOf(LangFormatter.START_SEPARATOR);
        int end = message.indexOf(LangFormatter.END_SEPARATOR, start + 1);

        while (start != -1 && end != -1) {
            result.append(message, i, start);

            String key = message.substring(start + 1, end);

            if (args.containsKey(key)) {
                result.append(args.get(key));
            } else if (this.contains(key)) {
                result.append(this.get(key));
            } else {
                result.append(LangFormatter.START_SEPARATOR)
                        .append(key)
                        .append(LangFormatter.END_SEPARATOR);
            }

            i = end + 1;
            start = message.indexOf(LangFormatter.START_SEPARATOR, i);
            end = message.indexOf(LangFormatter.END_SEPARATOR, start + 1);
        }

        return result.append(message.substring(i)).toString();
    }
}
