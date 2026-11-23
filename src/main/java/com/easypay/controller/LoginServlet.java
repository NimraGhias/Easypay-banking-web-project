package com.easypay.controller;

import com.easypay.model.User;
import com.easypay.service.AuthService;
import com.easypay.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/auth/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    private static class LoginRequest {
        String identifier; // email ya mobile number
        String pin;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            LoginRequest data = JsonUtil.readBody(request, LoginRequest.class);

            User user = authService.login(data.identifier, data.pin);

            if (user == null) {
                // Email/mobile ghalat ho ya PIN, message ek hi hai
                JsonUtil.write(response, 401, false, "Invalid email, mobile number or PIN.");
                return;
            }

            // Purani session khatam, nayi banao (session fixation se bachao)
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("fullName", user.getFullName());
            session.setMaxInactiveInterval(30 * 60); // 30 minute

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", true);
            body.put("message", "Login successful.");
            body.put("fullName", user.getFullName());
            JsonUtil.writeJson(response, 200, body);

        } catch (IllegalStateException e) {
            // Account lock hai
            JsonUtil.write(response, 429, false, e.getMessage());

        } catch (IllegalArgumentException e) {
            JsonUtil.write(response, 400, false, e.getMessage());

        } catch (SQLException e) {
            getServletContext().log("Login failed", e);
            JsonUtil.write(response, 500, false, "Something went wrong. Please try again.");
        }
    }
}