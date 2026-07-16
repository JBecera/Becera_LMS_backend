package edu.cit.becera.lrbms;

import edu.cit.becera.lrbms.entities.Fine;
import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.features.fine.dto.FineResponse;
import edu.cit.becera.lrbms.features.fine.service.FineService;
import edu.cit.becera.lrbms.repositories.FineRepository;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FineServiceTest {

    @Mock private FineRepository fineRepository;
    @Mock private MemberRepository memberRepository;

    private FineService fineService;

    @BeforeEach
    void setUp() {
        fineService = new FineService(fineRepository, memberRepository);
    }

    @Test
    void settleShouldMarkFineAsPaid() {
        Member member = new Member();
        member.setId(1L);
        member.setFirstName("Jane");
        member.setLastName("Doe");

        Fine fine = new Fine();
        fine.setId(7L);
        fine.setMember(member);
        fine.setAmount(15.0);
        fine.setReason("Overdue return (3 days late)");
        fine.setPaymentStatus("UNPAID");

        when(fineRepository.findById(7L)).thenReturn(Optional.of(fine));
        when(fineRepository.save(any(Fine.class))).thenAnswer(inv -> inv.getArgument(0));

        FineResponse response = fineService.settle(7L);

        assertEquals("PAID", response.getPaymentStatus());
    }
}
