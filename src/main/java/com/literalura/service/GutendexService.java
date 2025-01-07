package com.literalura.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.literalura.model.Author;
import com.literalura.model.Book;
import com.literalura.repository.AuthorRepository;
import com.literalura.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@Service
public class GutendexService {

    private static final String BASE_URL = "https://gutendex.com/books/";

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    // Obtener libros por idioma
    public List<Book> listBooksByLanguage(String language) {
        return bookRepository.findByLanguage(language);  // Llamada al repositorio para buscar libros por idioma
    }

    // Contar libros por idioma
    public long countBooksByLanguage(String language) {
        return bookRepository.countByLanguage(language);  // Método para contar los libros en un idioma
    }

    // Buscar libro por título y actualizar autor si ya existe
    public Book fetchBookByTitle(String title) {
        try {
            // Codificar el título para la URL
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String apiUrl = BASE_URL + "?search=" + encodedTitle;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Verificar el estado de la respuesta
            if (response.statusCode() != 200) {
                throw new RuntimeException("Error al consultar la API: Código de estado " + response.statusCode());
            }

            // Procesar el JSON de la respuesta
            JsonNode rootNode = new ObjectMapper().readTree(response.body());
            JsonNode resultsNode = rootNode.path("results");

            if (resultsNode.isArray() && resultsNode.size() > 0) {
                JsonNode firstResult = resultsNode.get(0);
                Book book = mapJsonNodeToBook(firstResult);

                // Verificar si ya existe un autor con ese nombre, si es así, actualizar sus datos
                Author author = book.getAuthor();
                Optional<Author> existingAuthor = authorRepository.findByName(author.getName());
                if (existingAuthor.isPresent()) {
                    Author existingAuthorData = existingAuthor.get();
                    // Actualizar el autor con los datos obtenidos de la API
                    existingAuthorData.setBirthYear(author.getBirthYear());
                    existingAuthorData.setDeathYear(author.getDeathYear());
                    authorRepository.save(existingAuthorData); // Guardar la actualización del autor
                }

                return bookRepository.save(book); // Guardar el libro, incluyendo el autor actualizado
            } else {
                throw new IllegalArgumentException("No se encontraron resultados para el título: " + title);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al consultar la API", e);
        }
    }


    // Función para mapear el JSON de libro a un objeto Book
    private Book mapJsonNodeToBook(JsonNode jsonNode) {
        Book book = new Book();
        book.setTitle(jsonNode.path("title").asText());

        // Verificar si el libro ya existe en la base de datos
        Optional<Book> existingBook = bookRepository.findByTitle(book.getTitle());
        if (existingBook.isPresent()) {
            return existingBook.get();
        }

        // Obtener el nombre del autor
        JsonNode authorNode = jsonNode.path("authors").isArray() && jsonNode.path("authors").size() > 0
                ? jsonNode.path("authors").get(0)
                : null;

        // Verificación e impresión de la información del autor
        if (authorNode != null) {
            System.out.println("Información del autor: ");
            System.out.println("Autor: " + authorNode.path("name").asText());

            Integer birthYear = null;
            Integer deathYear = null;

            // Verificar existencia y valores de birth_year y death_year
            if (authorNode.has("birth_year")) {
                birthYear = authorNode.path("birth_year").asInt();
                System.out.println("Año de nacimiento: " + birthYear);
            } else {
                System.out.println("Año de nacimiento no encontrado.");
            }

            if (authorNode.has("death_year")) {
                deathYear = authorNode.path("death_year").asInt();
                System.out.println("Año de fallecimiento: " + deathYear);
            } else {
                System.out.println("Año de fallecimiento no encontrado.");
            }

            // Guardar el autor con la información obtenida
            Author author = saveAuthor(authorNode.path("name").asText(), birthYear, deathYear);
            book.setAuthor(author);
        }

        // Establecer otros atributos del libro
        book.setLanguage(jsonNode.path("languages").get(0).asText());  // Suponiendo que solo hay un idioma
        book.setDownloadCount(jsonNode.path("download_count").asInt(0));

        return bookRepository.save(book);
    }


    private Author saveAuthor(String authorName, Integer birthYear, Integer deathYear) {
        Optional<Author> existingAuthor = authorRepository.findByName(authorName);

        if (existingAuthor.isPresent()) {
            // Si el autor ya existe, actualiza los valores de birthYear y deathYear
            Author author = existingAuthor.get();

            // Actualizar los campos solo si los valores no son nulos
            if (birthYear != null && !birthYear.equals(author.getBirthYear())) {
                author.setBirthYear(birthYear);  // Sobrescribir el valor si es diferente o si es nulo
            }

            if (deathYear != null && !deathYear.equals(author.getDeathYear())) {
                author.setDeathYear(deathYear);  // Sobrescribir el valor si es diferente o si es nulo
            }

            return authorRepository.save(author);  // Guardar los cambios
        } else {
            // Si no existe el autor, crea uno nuevo
            Author newAuthor = new Author();
            newAuthor.setName(authorName);
            newAuthor.setBirthYear(birthYear);
            newAuthor.setDeathYear(deathYear);
            return authorRepository.save(newAuthor);  // Guardar el nuevo autor
        }
    }



    // Listar autores vivos en un año determinado
    public List<Author> listAuthorsAliveInYear(int year) {
        return authorRepository.findByBirthYearLessThanEqualAndDeathYearGreaterThan(year, year);  // Busca los autores vivos en un determinado año
    }
    // Obtener libros con paginación
    public List<Book> getBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAll(pageable).getContent();
    }
    // Metodo para listar todos los autores
    public List<Author> listAllAuthors() {
        return authorRepository.findAll();  // Devolver todos los autores desde la base de datos
    }

}
