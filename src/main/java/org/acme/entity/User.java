package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    @NotBlank
    @Email
    @Column(unique = true)
    public String email;

    @NotBlank
    @Column(unique = true)
    public String phone;

    @NotBlank
    public String password;

    @NotBlank
    public String role; // ADMIN, USER

    public boolean isVerified = false;

    public String otp;

    public LocalDateTime otpExpiry;
    
    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;

    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
