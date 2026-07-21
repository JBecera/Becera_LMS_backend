package edu.cit.becera.lrbms.features.catalog.controller;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.features.catalog.dto.BookRequest;
import edu.cit.becera.lrbms.features.catalog.service.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//Catalog CRUD implementation
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return catalogService.getAllBooks();
    }

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam(defaultValue = "") String query) {
        return catalogService.searchBooks(query);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBook(@PathVariable Long id) {
        return catalogService.getBook(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createBook(@RequestBody BookRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createBook(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody BookRequest request) {
        try {
            return ResponseEntity.ok(catalogService.updateBook(id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        catalogService.deleteBook(id);
        return ResponseEntity.ok(Map.of("message", "Book removed successfully"));
    }
}
