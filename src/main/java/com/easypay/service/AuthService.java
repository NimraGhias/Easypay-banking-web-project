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
    private static final Pattern PIN_PATTERN =
            Pattern.compile("^\\d{5}$");

    // Lockout ke rules
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final UserDAO userDAO = new UserDAO();

    // Naya account banata hai. Ghalat input par IllegalArgumentException deta hai.
    public long register(String fullName, String email, String phone, String pin)
            throws SQLException {

        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        phone = normalizePhone(phone);

        if (fullName.isEmpty() || fullName.length() > 100) {
            throw new IllegalArgumentException("Please enter your full name.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches() || email.length() > 150) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Mobile number must look like 03XXXXXXXXX.");
        }
        if (pin == null || !PIN_PATTERN.matcher(pin).matches()) {
            throw new IllegalArgumentException("PIN must be exactly 5 digits.");
        }
        if (isWeakPin(pin)) {
            throw new IllegalArgumentException("Choose a harder PIN (not 12345 or 11111).");
        }
        if (userDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("This email is already registered.");
        }
        if (userDAO.findByPhone(phone) != null) {
            throw new IllegalArgumentException("This mobile number is already registered.");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPinHash(PasswordUtil.hash(pin));

        return userDAO.createUserWithWallet(user);
    }

    // identifier = email ya mobile number.
    // Sahi PIN par User deta hai, ghalat par null.
    // Account lock ho to IllegalStateException deta hai.
    public User login(String identifier, String pin) throws SQLException {
        if (identifier == null || identifier.isBlank() || pin == null || pin.isEmpty()) {
            return null;
        }

        identifier = identifier.trim();
        User user;
        if (identifier.contains("@")) {
            user = userDAO.findByEmail(identifier.toLowerCase());
        } else {
            user = userDAO.findByPhone(normalizePhone(identifier));
        }

        if (user == null || !"ACTIVE".equals(user.getAccountStatus())) {
            return null;
        }

        // Lock ke dauran sahi PIN bhi qabool nahi
        if (user.isLocked()) {
            throw new IllegalStateException(
                    "Too many wrong attempts. Try again in " + LOCK_MINUTES + " minutes.");
        }

        if (!PasswordUtil.verify(pin, user.getPinHash())) {
            userDAO.recordFailedAttempt(user.getUserId(), MAX_ATTEMPTS, LOCK_MINUTES);
            return null;
        }

        userDAO.resetFailedAttempts(user.getUserId());
        return user;
    }

    // Space aur dash hata deta hai: "0300-123 4567" -> "03001234567"
    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.trim().replaceAll("[\\s-]", "");
    }

    // 11111 ya 12345 / 54321 jaise aasan PIN
    private boolean isWeakPin(String pin) {
        boolean allSame = pin.chars().distinct().count() == 1;
        boolean ascending = "01234567890".contains(pin);
        boolean descending = "09876543210".contains(pin);
        return allSame || ascending || descending;
    }
}