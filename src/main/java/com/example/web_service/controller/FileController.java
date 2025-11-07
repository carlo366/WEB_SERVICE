package com.example.web_service.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class FileController {

    private final String UPLOAD_DIR = "uploads";

    @GetMapping("/api/uploads/{filename}")
    public ResponseEntity<FileSystemResource> getFile(@PathVariable String filename) {
        File file = new File(UPLOAD_DIR + "/" + filename);
        if (!file.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().body(new FileSystemResource(file));
    }
}
