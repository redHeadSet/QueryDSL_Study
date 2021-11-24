package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.SearchCondition;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> getallmemebers_v1(SearchCondition searchCondition) {
        return memberJpaRepository.findByConditionByMultiWhere(searchCondition);
    }
    // Get 방식으로
    // ~?username=mem1 등으로 처리하면 SearchCondition 내 자동으로 들어감

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> getallmemebers_v2(SearchCondition searchCondition, Pageable pageable) {
        return memberRepository.searchPageComplex(searchCondition, pageable);
    }
}
