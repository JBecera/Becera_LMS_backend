package edu.cit.becera.lrbms.features.fine.service;

import edu.cit.becera.lrbms.entities.Fine;
import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.features.fine.dto.FineResponse;
import edu.cit.becera.lrbms.repositories.FineRepository;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FineService {

    private final FineRepository fineRepository;
    private final MemberRepository memberRepository;

    public FineService(FineRepository fineRepository, MemberRepository memberRepository) {
        this.fineRepository = fineRepository;
        this.memberRepository = memberRepository;
    }

    public List<FineResponse> getAllFines() {
        return fineRepository.findAll().stream().map(FineResponse::from).toList();
    }

    public List<FineResponse> getFinesForMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return fineRepository.findByMember(member).stream().map(FineResponse::from).toList();
    }

    public FineResponse settle(Long id) {
        Fine fine = fineRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Fine not found"));
        fine.setPaymentStatus("PAID");
        return FineResponse.from(fineRepository.save(fine));
    }
}
