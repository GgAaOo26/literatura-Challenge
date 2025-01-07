package com.literalura.repository;

import com.literalura.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    // Método para buscar autores vivos en un determinado año
    List<Author> findByBirthYearLessThanEqualAndDeathYearGreaterThan(int year, int year2);

    // Método para buscar autores por nombre
    Optional<Author> findByName(String name);
}
