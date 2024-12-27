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
