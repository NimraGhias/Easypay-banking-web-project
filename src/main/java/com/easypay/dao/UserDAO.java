package com.easypay.dao;

import com.easypay.model.User;
import com.easypay.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    // locked_until abhi aage ki taareekh hai ya nahi, yeh MySQL khud batata hai
    private static final String SELECT_USER =
            "SELECT user_id, full_name, email, phone, pin_hash, failed_attempts, "
          + "account_status, (locked_until IS NOT NULL AND locked_until > NOW()) AS is_locked "
          + "FROM users ";

    public User findByEmail(String email) throws SQLException {
        return findOne(SELECT_USER + "WHERE email = ?", email);
    }

    public User findByPhone(String phone) throws SQLException {
        return findOne(SELECT_USER + "WHERE phone = ?", phone);
    }

    private User findOne(String sql, String value) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, value);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setPinHash(rs.getString("pin_hash"));
                user.setFailedAttempts(rs.getInt("failed_attempts"));
                user.setAccountStatus(rs.getString("account_status"));
                user.setLocked(rs.getBoolean("is_locked"));
                return user;
            }
        }
    }

    // User aur uska wallet ek hi transaction mein banata hai.
    // Dono ban jayein to commit, warna dono wapas (rollback).
    public long createUserWithWallet(User user) throws SQLException {
        String userSql = "INSERT INTO users (full_name, email, phone, pin_hash, account_status) "
                       + "VALUES (?, ?, ?, ?, 'ACTIVE')";
        String walletSql = "INSERT INTO wallets (user_id, balance, currency) "
                         + "VALUES (?, 0.00, 'PKR')";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement userPs =
                         con.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement walletPs = con.prepareStatement(walletSql)) {

                userPs.setString(1, user.getFullName());
                userPs.setString(2, user.getEmail());
                userPs.setString(3, user.getPhone());
                userPs.setString(4, user.getPinHash());
                userPs.executeUpdate();

                long newUserId;
                try (ResultSet keys = userPs.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("User id was not generated.");
                    }
                    newUserId = keys.getLong(1);
                }

                walletPs.setLong(1, newUserId);
                walletPs.executeUpdate();

                con.commit();
                return newUserId;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // Ghalat PIN par: ginti 1 barhao. Limit poori ho to account lock karo.
    public void recordFailedAttempt(long userId, int maxAttempts, int lockMinutes)
            throws SQLException {
        String addSql = "UPDATE users SET failed_attempts = failed_attempts + 1 "
                      + "WHERE user_id = ?";
        String lockSql = "UPDATE users SET locked_until = DATE_ADD(NOW(), INTERVAL ? MINUTE), "
                       + "failed_attempts = 0 "
                       + "WHERE user_id = ? AND failed_attempts >= ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement addPs = con.prepareStatement(addSql);
             PreparedStatement lockPs = con.prepareStatement(lockSql)) {

            addPs.setLong(1, userId);
            addPs.executeUpdate();

            lockPs.setInt(1, lockMinutes);
            lockPs.setLong(2, userId);
            lockPs.setInt(3, maxAttempts);
            lockPs.executeUpdate();
        }
    }

    // Sahi PIN par: ginti aur lock dono saaf
    public void resetFailedAttempts(long userId) throws SQLException {
        String sql = "UPDATE users SET failed_attempts = 0, locked_until = NULL "
                   + "WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }
}