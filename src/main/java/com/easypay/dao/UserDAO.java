package com.easypay.dao;

import com.easypay.model.User;
import com.easypay.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    // Email se user dhoondta hai. Na mile to null deta hai.
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, full_name, email, phone, password_hash, account_status "
                   + "FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getLong("user_id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setAccountStatus(rs.getString("account_status"));
                    return user;
                }
                return null;
            }
        }
    }

    // User aur uska wallet ek hi transaction mein banata hai.
    // Dono ban jayein to commit, warna dono wapas (rollback).
    public long createUserWithWallet(User user) throws SQLException {
        String userSql = "INSERT INTO users (full_name, email, phone, password_hash, account_status) "
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
                userPs.setString(4, user.getPasswordHash());
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
}
