package com.easypay.controller;

import com.easypay.util.DBConnection;
import com.easypay.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/api/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try (Connection con = DBConnection.getConnection()) {
            if (con.isValid(2)) {
                JsonUtil.write(response, 200, true, "Server and database are working.");
            } else {
                JsonUtil.write(response, 500, false, "Database connection is not valid.");
            }
        } catch (Exception e) {
            getServletContext().log("Health check failed", e);
            JsonUtil.write(response, 500, false, "Database connection failed. Check Tomcat log.");
        }
    }
}