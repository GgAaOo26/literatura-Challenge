package com.literalura;

import com.literalura.model.Author;
import com.literalura.model.Book;
import com.literalura.service.GutendexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class LiteraluraGaoApplication implements CommandLineRunner {

	@Autowired
	private GutendexService gutendexService;

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraGaoApplication.class, args);
	}

	@Override
	public void run(String... args) {
		// Llamar la función que gestiona el menú
		executeMenu();
	}

	private void executeMenu() {
		Scanner scanner = new Scanner(System.in);
		boolean running = true;

		while (running) {
			showMenu();  // Mostrar el menú
			System.out.print("Selecciona una opción: ");
			try {
				int option = scanner.nextInt();
				scanner.nextLine(); // Consumir la nueva línea

				// Procesar la opción seleccionada
				running = handleMenuOption(option, scanner);
			} catch (InputMismatchException e) {
				System.out.println("Entrada inválida. Por favor, ingresa un número válido.");
				scanner.nextLine(); // Limpiar el buffer
			}
		}

		scanner.close();
	}

	private void showMenu() {
		System.out.println("\n--- Menú Principal ---");
		System.out.println("1. Buscar libro por título");
		System.out.println("2. Listar libros registrados");
		System.out.println("3. Listar autores registrados");
		System.out.println("4. Listar autores vivos en un año");
		System.out.println("5. Listar libros por idioma");
		System.out.println("0. Salir");
	}

	// Manejar la opción seleccionada
	private boolean handleMenuOption(int option, Scanner scanner) {
		switch (option) {
			case 1 -> handleBookByTitle(scanner);
			case 2 -> handleListBooks();
			case 3 -> handleListAuthors();
			case 4 -> handleListAuthorsAlive();
			case 5 -> handleListBooksByLanguage(scanner);
			case 0 -> {
				System.out.println("¡Gracias por usar la aplicación Literalura! Hasta luego.");
				return false;  // Salir del bucle
			}
			default -> System.out.println("Opción inválida. Por favor, selecciona una opción válida.");
		}
		return true;  // Mantener el menú activo
	}

	// Buscar libro por título
	private void handleBookByTitle(Scanner scanner) {
		System.out.print("Ingresa el título del libro: ");
		String title = scanner.nextLine().trim();
		if (title.isEmpty()) {
			System.out.println("El título no puede estar vacío.");
			return;
		}

		try {
			Book book = gutendexService.fetchBookByTitle(title);
			System.out.println("Libro encontrado: " + book);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	// Listar libros registrados
	private void handleListBooks() {
		try {
			List<Book> books = gutendexService.getBooks(0, 10);
			books.forEach(book -> System.out.println(book));
		} catch (Exception e) {
			System.out.println("Error al obtener los libros: " + e.getMessage());
		}
	}

	private void handleListAuthors() {
		try {
			// Listar todos los autores registrados
			List<Author> authors = gutendexService.listAllAuthors();

			// Imprimir los autores y verificar que sus datos se muestren correctamente
			authors.forEach(author -> {
				// Aquí imprimimos los autores para asegurarnos de que los valores se están mostrando
				System.out.println(author);
			});
		} catch (Exception e) {
			System.out.println("Error al obtener los autores: " + e.getMessage());
		}
	}

	// Listar autores vivos en un año
	private void handleListAuthorsAlive() {
		System.out.print("Ingresa el año para listar autores vivos: ");
		Scanner scanner = new Scanner(System.in);
		int year = scanner.nextInt();
		// Suponiendo que tienes un método que devuelve los autores vivos en un año específico
		List<Author> authors = gutendexService.listAuthorsAliveInYear(year);
		authors.forEach(author -> System.out.println(author));
	}

	// Listar libros por idioma y mostrar la cantidad de libros registrados
	private void handleListBooksByLanguage(Scanner scanner) {
		System.out.print("Ingresa el idioma (código ISO, por ejemplo, 'en' para inglés): ");
		String language = scanner.nextLine().trim();

		// Obtener los libros por idioma
		List<Book> books = gutendexService.listBooksByLanguage(language);

		// Mostrar la cantidad de libros
		long count = gutendexService.countBooksByLanguage(language);
		System.out.println("Número de libros en el idioma '" + language + "': " + count);

		// Mostrar los libros
		if (books.isEmpty()) {
			System.out.println("No se encontraron libros en el idioma '" + language + "'.");
		} else {
			books.forEach(book -> System.out.println(book));
		}
	}

}
