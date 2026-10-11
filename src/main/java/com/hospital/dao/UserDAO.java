package com.hospital.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.util.PasswordUtil;

/**
 * Data Access Object for User authentication and role management.
 * Strictly uses PreparedStatement for all parameter bindings to eliminate SQL injection.
 */
public class UserDAO implements GenericDAO<User, Integer> {

    @Override
    public User save(User user) throws DatabaseException {
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, email, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getRole().name());
            
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to register new user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(User user) throws DatabaseException {
        String sql = "UPDATE users SET full_name = ?, email = ?, role = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole().name());
            ps.setInt(4, user.getUserId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user: " + e.getMessage(), e);
        }
    }

    public boolean updatePassword(int userId, String newPassword) throws DatabaseException {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(newPassword, salt);
        String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, userId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user password: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DatabaseException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    @Override
    public User findById(Integer id) throws DatabaseException {
        String sql = "SELECT user_id, username, password_hash, salt, full_name, email, role, created_at FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve user by ID: " + e.getMessage(), e);
        }
    }

    public User findByUsername(String username) throws DatabaseException {
        String sql = "SELECT user_id, username, password_hash, salt, full_name, email, role, created_at FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by username: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() throws DatabaseException {
        String sql = "SELECT user_id, username, password_hash, salt, full_name, email, role, created_at FROM users ORDER BY full_name";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve user list: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticates user against stored SHA-256 hash + salt.
     */
    public User authenticate(String username, String rawPassword) throws DatabaseException {
        User user = findByUsername(username);
        if (user == null) {
            return null;
        }
        boolean match = PasswordUtil.verifyPassword(rawPassword, user.getSalt(), user.getPasswordHash());
        return match ? user : null;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("salt"),
            rs.getString("full_name"),
            rs.getString("email"),
            Role.fromString(rs.getString("role")),
            rs.getTimestamp("created_at")
        );
    }
}
