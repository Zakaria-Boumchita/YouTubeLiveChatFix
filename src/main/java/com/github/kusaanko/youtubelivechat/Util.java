package com.github.kusaanko.youtubelivechat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SuppressWarnings("unchecked")
public class Util {
    private static final Gson gson = new Gson();

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(20))
            .writeTimeout(Duration.ofSeconds(20))
            .build();

    public static String toJSON(Map<String, Object> json) {
        StringBuilder js = new StringBuilder();
        js.append("{");
        for (String key : json.keySet()) {
            js.append("'").append(key).append("': ");
            Object d = json.get(key);
            if (d instanceof Byte ||
                    d instanceof Character ||
                    d instanceof Short ||
                    d instanceof Integer ||
                    d instanceof Long ||
                    d instanceof Float ||
                    d instanceof Double ||
                    d instanceof Boolean) {
                js.append(d);
            } else if (d instanceof Map) {
                js.append(toJSON((Map<String, Object>) d));
            } else {
                js.append("\"")
                        .append(d.toString().replace("\"", "\\\"").replace("\\", "\\\\"))
                        .append("\"");
            }
            js.append(", ");
        }
        return js.substring(0, js.length() - 2) + "}";
    }

    public static Map<String, Object> toJSON(String json) {
        if (!json.startsWith("{")) {
            throw new IllegalArgumentException("This is not json(map)!");
        }
        return gson.fromJson(json, Map.class);
    }

    public static Map<String, Object> getJSONMap(Map<String, Object> json, String... keys) {
        Map<String, Object> map = json;
        for (String key : keys) {
            if (map.containsKey(key)) {
                map = (Map<String, Object>) map.get(key);
            } else {
                return null;
            }
        }
        return map;
    }

    public static Map<String, Object> getJSONMap(Map<String, Object> json, Object... keys) {
        Map<String, Object> map = json;
        List<Object> list = null;
        for (Object key : keys) {
            if (map != null) {
                if (map.containsKey(key.toString())) {
                    Object value = map.get(key.toString());
                    if (value instanceof List) {
                        list = (List<Object>) value;
                        map = null;
                    } else {
                        map = (Map<String, Object>) value;
                    }
                } else {
                    return null;
                }
            } else {
                map = (Map<String, Object>) list.get((int) key);
                list = null;
            }
        }
        return map;
    }

    public static List<Object> getJSONList(Map<String, Object> json, String listKey, String... keys) {
        Map<String, Object> map = getJSONMap(json, keys);
        if (map != null && map.containsKey(listKey)) {
            return (List<Object>) map.get(listKey);
        }
        return null;
    }

    public static Object getJSONValue(Map<String, Object> json, String key) {
        if (json != null && json.containsKey(key)) {
            return json.get(key);
        }
        return null;
    }

    public static String getJSONValueString(Map<String, Object> json, String key) {
        Object value = getJSONValue(json, key);
        return value != null ? value.toString() : null;
    }

    public static boolean getJSONValueBoolean(Map<String, Object> json, String key) {
        Object value = getJSONValue(json, key);
        return value != null && (boolean) value;
    }

    public static long getJSONValueLong(Map<String, Object> json, String key) {
        Object value = getJSONValue(json, key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0;
    }

    public static int getJSONValueInt(Map<String, Object> json, String key) {
        return (int) getJSONValueLong(json, key);
    }

    public static String getPageContent(String url, Map<String, String> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();

        putRequestHeader(headers);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("HTTP error code: " + response.code());
            }
        }
    }

    public static String getPageContentWithJson(String url, String jsonData, Map<String, String> headers) throws IOException {
        RequestBody requestBody = RequestBody.create(
                jsonData,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);

        putRequestHeader(headers);
        headers.put("Content-Type", "application/json; charset=utf-8");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("HTTP error code: " + response.code());
            }
        }
    }

    public static void sendHttpRequestWithJson(String url, String jsonData, Map<String, String> headers) throws IOException {
        RequestBody requestBody = RequestBody.create(
                jsonData,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);

        putRequestHeader(headers);
        headers.put("Content-Type", "application/json; charset=utf-8");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "empty";
                throw new IOException("Request failed: HTTP " + response.code() + ", body: " + errorBody);
            }
        }
    }

    private static void putRequestHeader(Map<String, String> headers) {
        headers.put("Accept-Charset", "utf-8");
        headers.put("User-Agent", YouTubeLiveChat.userAgent);
    }

    public static String generateClientMessageId() {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 26; i++) {
            sb.append(base.charAt(random.nextInt(base.length())));
        }
        return sb.toString();
    }

    public static JsonElement searchJsonElementByKey(String key, JsonElement jsonElement) {
        if (jsonElement.isJsonArray()) {
            for (JsonElement element : jsonElement.getAsJsonArray()) {
                JsonElement found = searchJsonElementByKey(key, element);
                if (found != null) {
                    return found;
                }
            }
        } else if (jsonElement.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                if (entry.getKey().equals(key)) {
                    return entry.getValue();
                }
                JsonElement found = searchJsonElementByKey(key, entry.getValue());
                if (found != null) {
                    return found;
                }
            }
        } else {
            if (jsonElement.toString().equals(key)) {
                return jsonElement;
            }
        }
        return null;
    }
}
