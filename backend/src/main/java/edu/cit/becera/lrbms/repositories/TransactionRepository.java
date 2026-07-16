package edu.cit.becera.lrbms.repositories;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByMember(Member member);

    List<Transaction> findByMemberAndStatus(Member member, String status);

    long countByMemberAndStatus(Member member, String status);
}
