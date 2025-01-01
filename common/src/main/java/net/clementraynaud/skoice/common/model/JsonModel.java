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

package net.clementraynaud.skoice.common.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

public abstract class JsonModel {

    private static final Gson gson = new Gson();
    protected final String _t = this.getClass().getSimpleName();

    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            JsonObject jsonObject = JsonModel.gson.fromJson(json, JsonObject.class);
            if (jsonObject == null) {
                return null;
            }
            JsonPrimitive typeFromJson = jsonObject.getAsJsonPrimitive("_t");
            if (!(typeFromJson != null && classOfT.getSimpleName().equals(typeFromJson.getAsString()))) {
                return null;
            }
        } catch (JsonSyntaxException e) {
            return null;
        }
        return JsonModel.gson.fromJson(json, classOfT);
    }

    public static String toJson(JsonModel model) {
        return JsonModel.gson.toJson(model);
    }
}
