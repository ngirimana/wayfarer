package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
public class Trip extends PanacheEntity {

    @NotBlank
    public String origin;

    @NotBlank
    public String destination;

    @NotNull
    public LocalDateTime tripDate;

    @Positive
    public double fare;

    @Positive
    public int capacity;

    @NotBlank
    public String status; // ACTIVE, CANCELLED

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}
