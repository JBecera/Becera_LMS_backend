package edu.cit.becera.lrbms.repositories;

import edu.cit.becera.lrbms.entities.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
