package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.SearchCondition;
import study.querydsl.repository.MemberJpaRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> getallmemebers(SearchCondition searchCondition) {
        return memberJpaRepository.findByConditionByMultiWhere(searchCondition);
    }
    // Get 방식으로
    // ~?username=mem1 등으로 처리하면 SearchCondition 내 자동으로 들어감
}
