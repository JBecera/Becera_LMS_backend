package edu.cit.becera.lrbms.services;

import edu.cit.becera.lrbms.entities.Resource;
import edu.cit.becera.lrbms.repositories.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository repository;

    public ResourceService(ResourceRepository repository) {
        this.repository = repository;
    }

    public List<Resource> getAllResources() {
        return repository.findAll();
    }

    public Resource saveResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource data is required");
        }
        if (resource.getName() == null || resource.getName().isBlank()) {
            throw new IllegalArgumentException("Resource name is required");
        }
        if (resource.getType() == null || resource.getType().isBlank()) {
            throw new IllegalArgumentException("Resource type is required");
        }
        return repository.save(resource);
    }

    public Resource updateResource(Long id, Resource updatedResource) {
        Resource resource = repository.findById(id).orElseThrow();
        resource.setName(updatedResource.getName());
        resource.setType(updatedResource.getType());
        return repository.save(resource);
    }

    public void deleteResource(Long id) {
        repository.deleteById(id);
    }
}
