package com.github.kusaanko.youtubelivechat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public class JsonPathFinder {

    public static JsonElement findPath(Reader jsonReader, String path) throws IOException {
        List<String> pathParts = Arrays.asList(path.split("\\."));
        JsonReader reader = new JsonReader(jsonReader);
        return findPathRecursive(reader, pathParts, 0);
    }

    public static String findIsViewedLive(String response) throws IOException {
        // Ищем serviceTrackingParams по пути playerResponse.responseContext.serviceTrackingParams
        JsonElement stp = JsonPathFinder.findPath(new StringReader(response), "playerResponse.responseContext.serviceTrackingParams");
        if (stp == null || !stp.isJsonArray()) return null;

        JsonArray serviceTrackingParams = stp.getAsJsonArray();

        for (JsonElement elem : serviceTrackingParams) {
            if (!elem.isJsonObject()) continue;
            JsonObject obj = elem.getAsJsonObject();
            if (!obj.has("params") || !obj.get("params").isJsonArray()) continue;

            JsonArray params = obj.getAsJsonArray("params");
            for (JsonElement paramElem : params) {
                if (!paramElem.isJsonObject()) continue;
                JsonObject paramObj = paramElem.getAsJsonObject();
                if ("is_viewed_live".equals(paramObj.get("key").getAsString())) {
                    return paramObj.get("value").getAsString();
                }
            }
        }
        return null;
    }

    public static JsonElement findPathRecursive(JsonReader reader, List<String> pathParts, int depth) throws IOException {
        reader.setLenient(true);

        JsonToken token = reader.peek();

        if (token == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals(pathParts.get(depth))) {
                    if (depth == pathParts.size() - 1) {
                        return JsonParser.parseReader(reader); // нужный элемент
                    } else {
                        return findPathRecursive(reader, pathParts, depth + 1);
                    }
                } else {
                    skipValue(reader);
                }
            }
            reader.endObject();
        } else if (token == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();
            while (reader.hasNext()) {
                JsonElement found = findPathRecursive(reader, pathParts, depth);
                if (found != null) return found;
            }
            reader.endArray();
        } else {
            // Если это примитив или что-то еще - пропускаем
            skipValue(reader);
        }

        return null;
    }

    private static void skipValue(JsonReader reader) throws IOException {
        reader.skipValue();
    }
}