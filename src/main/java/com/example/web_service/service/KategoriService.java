package com.example.web_service.service;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import com.example.web_service.entity.Kategori;
import com.example.web_service.repository.KategoriRepository;

@Service

public class KategoriService {
    private final KategoriRepository kategoriRepository;

    public KategoriService(KategoriRepository kategoriRepository){
        this.kategoriRepository = kategoriRepository;
    }

    public List<Kategori> getAll(){
        return kategoriRepository.findAll();
    }

    public Optional<Kategori> getByid(Long id){
        return kategoriRepository.findById(id);
    }

    public Kategori save(Kategori kategori){
        return kategoriRepository.save(kategori);
    }

    public void delete(Long id){
        kategoriRepository.deleteById(id);
    }


}
