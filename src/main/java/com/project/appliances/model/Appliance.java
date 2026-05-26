package com.project.appliances.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "appliance")
public class Appliance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "model")
    private String model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;

    @Column(name = "power_type")
    @Enumerated(EnumType.STRING)
    private PowerType powerType;

    @Column(name = "characteristic")
    private String characteristic;

    @Column(name = "description")
    private String description;

    @Column(name = "power")
    private Integer power;

    @Column(name = "price")
    private BigDecimal price;

}
