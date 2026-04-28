package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", uniqueConstraints = {
    @jakarta.persistence.UniqueConstraint(columnNames = {"trip_id", "seatNumber"})
})
public class Booking extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    public User user;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    @NotNull
    public Trip trip;

    @Positive
    public int seatNumber;

    @Positive
    @jakarta.persistence.Column(name = "fare_paid")
    public double farePaid;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}
