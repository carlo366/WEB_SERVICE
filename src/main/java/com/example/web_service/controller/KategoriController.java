package com.example.web_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

import com.example.web_service.entity.Kategori;
import com.example.web_service.service.KategoriService;

@RestController
@RequestMapping("/api/kategori")
public class KategoriController {
    private final KategoriService kategoriService;

    public KategoriController(KategoriService kategoriService) {
        this.kategoriService = kategoriService;
    }

    // GET semua kategori
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<Kategori> data = kategoriService.getAll();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return error("Gagal mengambil data kategori: " + e.getMessage());
        }
    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Optional<Kategori> kategori = kategoriService.getByid(id);
            if (kategori.isPresent()) {
                return ResponseEntity.ok(kategori.get());
            } else {
                return error("Kategori dengan ID " + id + " tidak ditemukan");
            }
        } catch (Exception e) {
            return error("Terjadi kesalahan: " + e.getMessage());
        }
    }

    // POST buat kategori baru
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Kategori kategori) {
        try {
            if (kategori.getNama() == null || kategori.getNama().isEmpty()) {
                return error("Nama kategori wajib diisi");
            }
            Kategori saved = kategoriService.save(kategori);
            return success("Kategori berhasil dibuat", saved);
        } catch (Exception e) {
            return error("Gagal membuat kategori: " + e.getMessage());
        }
    }

    // PUT update kategori
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Kategori kategori) {
        try {
            Optional<Kategori> existing = kategoriService.getByid(id);
            if (existing.isPresent()) {
                Kategori k = existing.get();
                k.setNama(kategori.getNama());
                Kategori updated = kategoriService.save(k);
                return success("Kategori berhasil diperbarui", updated);
            } else {
                return error("Kategori dengan ID " + id + " tidak ditemukan");
            }
        } catch (Exception e) {
            return error("Gagal memperbarui kategori: " + e.getMessage());
        }
    }

    // DELETE kategori
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            Optional<Kategori> existing = kategoriService.getByid(id);
            if (existing.isPresent()) {
                kategoriService.delete(id);
                return success("Kategori berhasil dihapus", null);
            } else {
                return error("Kategori dengan ID " + id + " tidak ditemukan");
            }
        } catch (Exception e) {
            return error("Gagal menghapus kategori: " + e.getMessage());
        }
    }

    // 🔹 Helper method untuk response sukses dan error
    private ResponseEntity<Map<String, Object>> success(String message, Object data) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("message", message);
        if (data != null) res.put("data", data);
        return ResponseEntity.ok(res);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "error");
        res.put("message", message);
        return ResponseEntity.badRequest().body(res);
    }
}
