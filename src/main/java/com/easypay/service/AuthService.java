package com.easypay.service;

import com.easypay.dao.UserDAO;
import com.easypay.model.User;
import com.easypay.util.PasswordUtil;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^03\\d{9}$");

    private final UserDAO userDAO = new UserDAO();

    // Naya account banata hai. Ghalat input par IllegalArgumentException deta hai.
    public long register(String fullName, String email, String phone, String password)
            throws SQLException {

        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        phone = phone == null ? "" : phone.trim();

        if (fullName.isEmpty() || fullName.length() > 100) {
            throw new IllegalArgumentException("Please enter your full name.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches() || email.length() > 150) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Mobile number must look like 03XXXXXXXXX.");
        }
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("Password must be 8 to 72 characters.");
        }
        if (userDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("This email is already registered.");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone.isEmpty() ? null : phone);
        user.setPasswordHash(PasswordUtil.hash(password));

        return userDAO.createUserWithWallet(user);
    }

    // Sahi email/password par User deta hai, warna null.
    public User login(String email, String password) throws SQLException {
        if (email == null || password == null || email.isBlank() || password.isEmpty()) {
            return null;
        }

        User user = userDAO.findByEmail(email.trim().toLowerCase());

        if (user == null
                || !"ACTIVE".equals(user.getAccountStatus())
                || !PasswordUtil.verify(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }
}
