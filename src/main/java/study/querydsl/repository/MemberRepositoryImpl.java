package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.dto.SearchCondition;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustomInterface{
    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;  // Spring Bean에 등록되어 있어 알아서 Injection

    @Override
    public List<MemberTeamDto> findByConditionByMultiWhere(SearchCondition searchCondition) {
        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        member.team.id,
                        member.team.name)
                )
                .from(member)
                .join(member.team, team)
                .where(
                        matchUsername(searchCondition.getUsername()),
                        matchTeamName(searchCondition.getTeamname()),
                        matchGoeAge(searchCondition.getAgeGoe()),
                        matchLoeAge(searchCondition.getAgeLoe())
                )
                .fetch();
    }


    @Override
    public Page<MemberTeamDto> searchPageSimple(SearchCondition searchCondition, Pageable pageable) {
        // 위와 동일, 페이징 쿼리만 추가
        QueryResults<MemberTeamDto> results = jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        member.team.id,
                        member.team.name)
                )
                .from(member)
                .join(member.team, team)
                .where(
                        matchUsername(searchCondition.getUsername()),
                        matchTeamName(searchCondition.getTeamname()),
                        matchGoeAge(searchCondition.getAgeGoe()),
                        matchLoeAge(searchCondition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> contents = results.getResults();
        Long total = results.getTotal();
        return new PageImpl<>(contents, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(SearchCondition searchCondition, Pageable pageable) {
        return null;
    }


    private BooleanExpression matchUsername(String username){ return hasText(username) ? member.username.eq(username) : null; }
    private BooleanExpression matchTeamName(String teamName){ return hasText(teamName) ? team.name.eq(teamName) : null; }
    private BooleanExpression matchGoeAge(Integer age){ return age != null ? member.age.goe(age) : null; }
    private BooleanExpression matchLoeAge(Integer age){ return age != null ? member.age.loe(age) : null; }
}
