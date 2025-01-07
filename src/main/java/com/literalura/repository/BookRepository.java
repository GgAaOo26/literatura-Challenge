package com.literalura.repository;

import com.literalura.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Buscar libro por título
    Optional<Book> findByTitle(String title);

    // Buscar libros por idioma
    List<Book> findByLanguage(String language);

    // Contar libros por idioma
    long countByLanguage(String language);  // Método para contar los libros en un idioma
}
