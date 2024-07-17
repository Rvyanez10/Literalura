//modelo directorio//
literatura/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── literatura/
│   │   │               ├── LiteraturaApplication.java
│   │   │               ├── controller/
│   │   │               │   └── BookController.java
│   │   │               ├── model/
│   │   │               │   └── Book.java
│   │   │               ├── repository/
│   │   │               │   └── BookRepository.java
│   │   │               ├── service/
│   │   │               │   └── BookService.java
│   │   │               └── util/
│   │   │                   └── GutendexClient.java
│   └── resources/
│       └── application.properties
└── pom.xml

package com.example.literatura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiteraturaApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiteraturaApplication.class, args);
    }
}

package com.example.literatura.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String authorLastName;
    private String authorFirstName;
    private String language;
    private int downloadCount;

    // Getters y Setters
}

package com.example.literatura.repository;

import com.example.literatura.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitle(String title);
    List<Book> findByAuthorLastNameAndAuthorFirstName(String lastName, String firstName);
    List<Book> findByLanguage(String language);
}

package com.example.literatura.service;

import com.example.literatura.model.Book;
import com.example.literatura.repository.BookRepository;
import com.example.literatura.util.GutendexClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private GutendexClient gutendexClient;

    public Book searchAndSaveBook(String title) {
        Book book = gutendexClient.searchBookByTitle(title);
        if (book != null && bookRepository.findByTitle(book.getTitle()).isEmpty()) {
            return bookRepository.save(book);
        }
        return null;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<String> getAllAuthors() {
        return bookRepository.findAll().stream()
                .map(book -> book.getAuthorLastName() + ", " + book.getAuthorFirstName())
                .distinct()
                .toList();
    }

    public List<String> getAuthorsAliveInYear(int year) {
        // Esta es una implementación simplificada. Se debe ampliar para verificar fechas de nacimiento y fallecimiento.
        return getAllAuthors();
    }

    public List<Book> getBooksByLanguage(String language) {
        return bookRepository.findByLanguage(language);
    }
}

package com.example.literatura.service;

import com.example.literatura.model.Book;
import com.example.literatura.repository.BookRepository;
import com.example.literatura.util.GutendexClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private GutendexClient gutendexClient;

    public Book searchAndSaveBook(String title) {
        Book book = gutendexClient.searchBookByTitle(title);
        if (book != null && bookRepository.findByTitle(book.getTitle()).isEmpty()) {
            return bookRepository.save(book);
        }
        return null;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<String> getAllAuthors() {
        return bookRepository.findAll().stream()
                .map(book -> book.getAuthorLastName() + ", " + book.getAuthorFirstName())
                .distinct()
                .toList();
    }

    public List<String> getAuthorsAliveInYear(int year) {
        // Esta es una implementación simplificada. Se debe ampliar para verificar fechas de nacimiento y fallecimiento.
        return getAllAuthors();
    }

    public List<Book> getBooksByLanguage(String language) {
        return bookRepository.findByLanguage(language);
    }
}

package com.example.literatura.util;

import com.example.literatura.model.Book;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

@Component
public class GutendexClient {
    private static final String API_URL = "https://gutendex.com/books";

    public Book searchBookByTitle(String title) {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(API_URL + "?search=" + title, String.class);
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray results = jsonResponse.getJSONArray("results");

        if (results.length() > 0) {
            JSONObject bookData = results.getJSONObject(0);
            String bookTitle = bookData.getString("title");
            JSONArray authors = bookData.getJSONArray("authors");
            String authorLastName = authors.getJSONObject(0).getString("last_name");
            String authorFirstName = authors.getJSONObject(0).getString("first_name");
            String language = bookData.getJSONArray("languages").getString(0);
            int downloadCount = bookData.getInt("download_count");

            Book book = new Book();
            book.setTitle(bookTitle);
            book.setAuthorLastName(authorLastName);
            book.setAuthorFirstName(authorFirstName);
            book.setLanguage(language);
            book.setDownloadCount(downloadCount);
            return book;
        }
        return null;
    }
}
package com.example.literatura.controller;

import com.example.literatura.model.Book;
import com.example.literatura.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Scanner;

@Controller
public class BookController {
    @Autowired
    private BookService bookService;

    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Seleccione una opción:");
            System.out.println("1. Buscar libro por título");
            System.out.println("2. Listar todos los libros");
            System.out.println("3. Listar todos los autores");
            System.out.println("4. Listar autores vivos en un año específico");
            System.out.println("5. Listar libros por idioma");
            System.out.println("6. Salir");

            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("Ingrese el título del libro: ");
                    String title = scanner.nextLine();
                    Book book = bookService.searchAndSaveBook(title);
                    if (book != null) {
                        System.out.println("Libro guardado: " + book.getTitle());
                    } else {
                        System.out.println("Libro no encontrado o ya existe en la base de datos.");
                    }
                    break;
                case 2:
                    List<Book> books = bookService.getAllBooks();
                    books.forEach(b -> System.out.println(b.getTitle() + " - " + b.getAuthorLastName() + ", " + b.getAuthorFirstName()));
                    break;
                case 3:
                    List<String> authors = bookService.getAllAuthors();
                    authors.forEach(System.out::println);
                    break;
                case 4:
                    System.out.print("Ingrese el año: ");
                    int year = scanner.nextInt();
                    List<String> authorsAlive = bookService.getAuthorsAliveInYear(year);
                    authorsAlive.forEach(System.out::println);
                    break;
                case 5:
                    System.out.print("Ingrese el idioma (ES, EN, FR, PT): ");
                    String language = scanner.nextLine();
                    List<Book> booksByLanguage = bookService.getBooksByLanguage(language);
                    booksByLanguage.forEach(b -> System.out.println(b.getTitle() + " - " + b.getAuthorLastName() + ", " + b.getAuthorFirstName()));
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }
}
spring.datasource.url=jdbc:postgresql://localhost:5432/literatura
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

