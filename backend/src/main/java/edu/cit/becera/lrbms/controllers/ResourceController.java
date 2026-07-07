package edu.cit.becera.lrbms.controllers;

import edu.cit.becera.lrbms.entities.Resource;
import edu.cit.becera.lrbms.services.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class ResourceController {

    private final ResourceService service;

    public ResourceController(ResourceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Resource> getAllResources() {
        return service.getAllResources();
    }

    @PostMapping
    public ResponseEntity<?> createResource(@RequestBody Resource resource) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.saveResource(resource));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateResource(@PathVariable Long id, @RequestBody Resource resource) {
        try {
            return ResponseEntity.ok(service.updateResource(id, resource));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable Long id) {
        service.deleteResource(id);
        return ResponseEntity.ok(Map.of("message", "Resource removed successfully"));
    }
}
