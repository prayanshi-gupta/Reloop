package com.reloop.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper to construct standardized JSON responses.
 * Follows structure: { "success": true, "data": ... } or { "success": false, "error": "..." }
 */
public class ResponseBuilder {

    public static String success(Object data) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", true);
        map.put("data", data);
        return JsonUtil.toJson(map);
    }

    public static String successMessage(String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", true);
        map.put("message", message);
        return JsonUtil.toJson(map);
    }

    public static String error(String errorMessage) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", false);
        map.put("error", errorMessage != null ? errorMessage : "An unexpected error occurred");
        return JsonUtil.toJson(map);
    }
}
