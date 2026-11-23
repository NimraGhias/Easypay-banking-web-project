package com.easypay.controller;

import com.easypay.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/auth/me")
public class MeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JsonUtil.write(response, 401, false, "Please log in first.");
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("fullName", session.getAttribute("fullName"));
        JsonUtil.writeJson(response, 200, body);
    }
}