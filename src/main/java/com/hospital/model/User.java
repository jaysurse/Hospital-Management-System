package com.hospital.model;

import java.sql.Timestamp;

/**
 * System user entity for authentication and role-based access control (RBAC).
 */
public class User implements Identifiable<Integer> {

    private int userId;
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String email;
    private Role role;
    private Timestamp createdAt;
    private Integer associatedPatientId;
    private Integer associatedDoctorId;

    public User() {
        this.role = Role.RECEPTIONIST;
    }

    public User(String username, String passwordHash, String salt, String fullName, String email, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public User(int userId, String username, String passwordHash, String salt, String fullName, String email, Role role, Timestamp createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    @Override
    public Integer getId() {
        return userId;
    }

    @Override
    public void setId(Integer id) {
        this.userId = (id == null) ? 0 : id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getAssociatedPatientId() {
        return associatedPatientId;
    }

    public void setAssociatedPatientId(Integer associatedPatientId) {
        this.associatedPatientId = associatedPatientId;
    }

    public Integer getAssociatedDoctorId() {
        return associatedDoctorId;
    }

    public void setAssociatedDoctorId(Integer associatedDoctorId) {
        this.associatedDoctorId = associatedDoctorId;
    }

    @Override
    public String toString() {
        return fullName + " (" + username + " - " + (role != null ? role.getDisplayName() : "User") + ")";
    }
}
