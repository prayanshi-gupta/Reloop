package com.reloop.util;

import java.util.*;

/**
 * Pure Java utility for JSON formatting and simple parsing without external libraries.
 * Keeps the project 100% compliant with standard Java SE.
 * Demonstrates: StringBuilder, String manipulation, Recursion, Collections.
 */
public class JsonUtil {

    /**
     * Serializes an object, map, or list into a JSON string.
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Collection) {
            Collection<?> col = (Collection<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escape(obj.toString()) + "\"";
    }

    /**
     * Parses a flat or simple JSON object string into a Map<String, Object>.
     */
    public static Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (json == null) return map;
        String s = json.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length() - 1);
        s = s.trim();
        if (s.isEmpty()) return map;

        int len = s.length();
        int i = 0;
        while (i < len) {
            // Find key
            while (i < len && (s.charAt(i) == ' ' || s.charAt(i) == ',' || s.charAt(i) == '\n' || s.charAt(i) == '\r')) i++;
            if (i >= len) break;
            if (s.charAt(i) != '"') break;
            i++; // skip open quote
            int keyStart = i;
            while (i < len && s.charAt(i) != '"') i++;
            String key = s.substring(keyStart, i);
            i++; // skip close quote

            // Find colon
            while (i < len && s.charAt(i) != ':') i++;
            i++; // skip colon
            while (i < len && Character.isWhitespace(s.charAt(i))) i++;

            // Value
            if (i < len && s.charAt(i) == '"') {
                i++; // skip open quote
                int valStart = i;
                while (i < len && s.charAt(i) != '"') {
                    if (s.charAt(i) == '\\' && i + 1 < len) i++;
                    i++;
                }
                String val = s.substring(valStart, i);
                i++; // skip close quote
                map.put(key, unescape(val));
            } else if (i < len && s.charAt(i) == '[') {
                int listStart = i;
                int depth = 1;
                i++;
                while (i < len && depth > 0) {
                    if (s.charAt(i) == '[') depth++;
                    else if (s.charAt(i) == ']') depth--;
                    i++;
                }
                String arrayStr = s.substring(listStart, i);
                map.put(key, parseJsonArrayOfObjects(arrayStr));
            } else {
                int valStart = i;
                while (i < len && s.charAt(i) != ',' && s.charAt(i) != '}') i++;
                String val = s.substring(valStart, i).trim();
                if ("true".equalsIgnoreCase(val)) map.put(key, Boolean.TRUE);
                else if ("false".equalsIgnoreCase(val)) map.put(key, Boolean.FALSE);
                else if ("null".equalsIgnoreCase(val)) map.put(key, null);
                else {
                    try {
                        if (val.contains(".")) {
                            map.put(key, Double.parseDouble(val));
                        } else {
                            map.put(key, Integer.parseInt(val));
                        }
                    } catch (NumberFormatException e) {
                        map.put(key, val);
                    }
                }
            }
        }
        return map;
    }

    /**
     * Parses a JSON array of objects: [ { ... }, { ... } ]
     */
    public static List<Map<String, Object>> parseJsonArrayOfObjects(String json) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (json == null) return list;
        String s = json.trim();
        if (s.startsWith("[")) s = s.substring(1);
        if (s.endsWith("]")) s = s.substring(0, s.length() - 1);
        s = s.trim();
        if (s.isEmpty()) return list;

        int len = s.length();
        int i = 0;
        while (i < len) {
            while (i < len && s.charAt(i) != '{') i++;
            if (i >= len) break;
            int objStart = i;
            int depth = 1;
            i++;
            while (i < len && depth > 0) {
                if (s.charAt(i) == '{') depth++;
                else if (s.charAt(i) == '}') depth--;
                i++;
            }
            String objStr = s.substring(objStart, i);
            list.add(parseJsonObject(objStr));
        }
        return list;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
