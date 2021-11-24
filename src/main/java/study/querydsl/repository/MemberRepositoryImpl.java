package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
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
                        goeAge(searchCondition.getAgeGoe()),
                        loeAge(searchCondition.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression matchUsername(String username){
        return hasText(username) ? member.username.eq(username) : null;
    }
    private BooleanExpression matchTeamName(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }
    private BooleanExpression goeAge(Integer ageGoe){
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }
    private BooleanExpression loeAge(Integer ageLoe){
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
