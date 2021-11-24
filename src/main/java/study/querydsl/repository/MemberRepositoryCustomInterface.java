package study.querydsl.repository;

import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.SearchCondition;

import java.util.List;

public interface MemberRepositoryCustomInterface {
    List<MemberTeamDto> findByConditionByMultiWhere(SearchCondition searchCondition);
}
