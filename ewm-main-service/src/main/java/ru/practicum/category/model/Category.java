package ru.practicum.category.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "category")
@Entity
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    @Column
    private String description;
}
