package com.literalura.controller;

import com.literalura.service.GutendexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookController {

    @Autowired
    private GutendexService gutendexService;

    // Contar libros por idioma
    // Contar libros por idioma
    @GetMapping("/countByLanguage")
    public ResponseEntity<?> countBooksByLanguage(@RequestParam("language") String language) {
        try {
            long count = gutendexService.countBooksByLanguage(language);
            return ResponseEntity.ok("Número de libros en el idioma " + language + ": " + count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el número de libros por idioma: " + e.getMessage());
        }
    }


    // Listar libros por idioma
    @GetMapping("/booksByLanguage")
    public ResponseEntity<?> getBooksByLanguage(@RequestParam("language") String language) {
        try {
            return ResponseEntity.ok(gutendexService.listBooksByLanguage(language));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los libros por idioma: " + e.getMessage());
        }
    }
}
