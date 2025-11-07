package com.example.web_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.web_service.entity.Kategori;

public interface KategoriRepository extends JpaRepository<Kategori, Long> {
    
}