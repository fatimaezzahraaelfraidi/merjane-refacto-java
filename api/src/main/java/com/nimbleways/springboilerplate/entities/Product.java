package com.nimbleways.springboilerplate.entities;

import com.nimbleways.springboilerplate.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lead_time")
    private Integer leadTime;

    @Column(name = "availableQuantity")
    private Integer availableQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ProductType type;

    @Column(name = "name")
    private String name;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "season_start_date")
    private LocalDate seasonStartDate;

    @Column(name = "season_end_date")
    private LocalDate seasonEndDate;

    public boolean isInStock() {
        return availableQuantity != null && availableQuantity > 0;
    }

    public boolean isExpired() {
        return expiryDate != null && !expiryDate.isAfter(LocalDate.now());
    }

    public boolean isInSeason() {
        LocalDate today = LocalDate.now();
        return seasonStartDate != null
                && seasonEndDate != null
                && today.isAfter(seasonStartDate)
                && today.isBefore(seasonEndDate);
    }

    public boolean leadTimeExceedsSeason() {
        return seasonEndDate != null
                && leadTime != null
                && LocalDate.now().plusDays(leadTime).isAfter(seasonEndDate);
    }

    public boolean isSeasonStarted() {
        return seasonStartDate != null && !seasonStartDate.isAfter(LocalDate.now());
    }

    public void decrementStock() {
        if (availableQuantity != null && availableQuantity > 0) {
            this.availableQuantity--;
        }
    }
}
