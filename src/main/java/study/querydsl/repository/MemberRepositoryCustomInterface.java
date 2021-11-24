package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.SearchCondition;

import java.util.List;

public interface MemberRepositoryCustomInterface {
    List<MemberTeamDto> findByConditionByMultiWhere(SearchCondition searchCondition);

    // 페이징 처리
    Page<MemberTeamDto> searchPageSimple(SearchCondition searchCondition, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(SearchCondition searchCondition, Pageable pageable);
}
