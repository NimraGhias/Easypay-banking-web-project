package com.easypay.filter;

import com.easypay.util.JsonUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

@WebFilter("/*")
public class AuthFilter implements Filter {

    // In addresses ke liye login ki zaroorat nahi
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "", "/", "/login.html", "/register.html",
            "/auth.js", "/auth.css", "/style.css", "/easypay-logo.png",
            "/api/auth/login", "/api/auth/register", "/api/auth/logout",
            "/api/health");

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // "/Easypay/index.html" se context hata kar "/index.html" banata hai
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (PUBLIC_PATHS.contains(path)) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("userId") != null;

        if (loggedIn) {
            // Logout ke baad Back button se purana dashboard na dikhe
            response.setHeader("Cache-Control", "no-store");
            chain.doFilter(req, res);
            return;
        }

        if (path.startsWith("/api/")) {
            JsonUtil.write(response, 401, false, "Please log in first.");
        } else {
            response.sendRedirect(request.getContextPath() + "/login.html");
        }
    }
}