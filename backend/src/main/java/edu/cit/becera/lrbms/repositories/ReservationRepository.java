package edu.cit.becera.lrbms.repositories;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByMember(Member member);

    List<Reservation> findByMemberAndBookAndStatusIn(Member member, Book book, List<String> statuses);

    List<Reservation> findByBookAndStatusOrderByIdAsc(Book book, String status);
}
