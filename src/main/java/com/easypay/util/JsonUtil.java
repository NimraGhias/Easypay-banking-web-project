package com.easypay.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonUtil {

    private static final Gson GSON = new Gson();

    private JsonUtil() { }

    // Request ki JSON body ko Java object mein badalta hai
    public static <T> T readBody(HttpServletRequest request, Class<T> type) throws IOException {
        request.setCharacterEncoding("UTF-8");
        try (BufferedReader reader = request.getReader()) {
            T result = GSON.fromJson(reader, type);
            if (result == null) {
                throw new IllegalArgumentException("Request body is empty.");
            }
            return result;
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid request format.");
        }
    }

    // {"success": true/false, "message": "..."} bhejta hai
    public static void write(HttpServletResponse response, int status,
                             boolean success, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", success);
        body.put("message", message);
        writeJson(response, status, body);
    }

    // Koi bhi object JSON bana kar bhejta hai
    public static void writeJson(HttpServletResponse response, int status,
                                 Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GSON.toJson(body));
    }
}