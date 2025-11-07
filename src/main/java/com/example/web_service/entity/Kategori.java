package com.example.web_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "kategori")

public class Kategori {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long Id;

        @Column(nullable = false, unique = true)
        private String nama;
}
