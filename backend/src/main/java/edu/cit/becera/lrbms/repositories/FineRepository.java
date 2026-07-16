package edu.cit.becera.lrbms.repositories;

import edu.cit.becera.lrbms.entities.Fine;
import edu.cit.becera.lrbms.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByMember(Member member);

    boolean existsByMemberAndPaymentStatus(Member member, String paymentStatus);
}
