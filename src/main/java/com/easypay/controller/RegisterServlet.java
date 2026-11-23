package com.easypay.controller;

import com.easypay.service.AuthService;
import com.easypay.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/auth/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    // Browser ki bheji hui JSON ke fields yahan aate hain
    private static class RegisterRequest {
        String fullName;
        String email;
        String phone;
        String pin;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            RegisterRequest data = JsonUtil.readBody(request, RegisterRequest.class);

            authService.register(data.fullName, data.email, data.phone, data.pin);

            JsonUtil.write(response, 201, true, "Account created. Please log in.");

        } catch (IllegalArgumentException e) {
            // Galat input: user ko wajah batate hain
            JsonUtil.write(response, 400, false, e.getMessage());

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                // Duplicate email ya mobile (database ka UNIQUE rule)
                JsonUtil.write(response, 409, false,
                        "This email or mobile number is already registered.");
            } else {
                getServletContext().log("Register failed", e);
                JsonUtil.write(response, 500, false, "Something went wrong. Please try again.");
            }
        }
    }
}