package com.easypay.controller;

import com.easypay.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/api/auth/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // false ka matlab: session maujood ho to do, nayi na banao
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        JsonUtil.write(response, 200, true, "Logged out.");
    }
}