package com.dailycodework.dream_shops.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Magacin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;


    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "magacin", cascade = CascadeType.ALL)
    private Set<Product> items = new HashSet<>();

    @OneToMany(mappedBy = "magacin",cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Delivery> deliveries = new HashSet<>();

    public void addItem(Product magacinItem) {
        this.items.add(magacinItem);
        magacinItem.setMagacin(this);
    }
}