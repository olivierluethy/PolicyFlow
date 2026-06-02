package ch.policyflow.domain.entity;

import ch.policyflow.domain.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * An application user (advisor or administrator) who can authenticate and operate
 * the platform. Passwords are stored as BCrypt hashes only.
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 100)
    public String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    public UserRole role;

    @Column(name = "full_name", length = 200)
    public String fullName;

    @Column(name = "active", nullable = false)
    public boolean active = true;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Finds an active user by username.
     *
     * @param username the username
     * @return the matching user, or {@code null} if none exists
     */
    public static User findByUsername(String username) {
        return find("username = ?1 and active = true", username).firstResult();
    }
}
