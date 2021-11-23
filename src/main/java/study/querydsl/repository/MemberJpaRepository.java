package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.dto.SearchCondition;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import javax.persistence.EntityManager;

import java.util.List;

import static org.springframework.util.StringUtils.*;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

    // Main 쪽에 Bean 등록을 하면 RequireArgsConstructor 내 자동 처리
//    public MemberJpaRepository(EntityManager em) {
//        this.em = em;
//        this.jpaQueryFactory = new JPAQueryFactory(em);
//    }

    public void save(Member member) {
        em.persist(member);
    }

    public Member findById(Long id) {
        return jpaQueryFactory
                .select(QMember.member)
                .from(QMember.member)
                .where(QMember.member.id.eq(id))
                .fetchOne();
    }

    public List<Member> findAll() {
        return jpaQueryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<MemberTeamDto> findAllByMTD() {
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
                .fetch();
    }

    public List<MemberTeamDto> findByCondition(SearchCondition searchCondition){
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
                .where( makeCondition(searchCondition) )
                .fetch();
    }
    private BooleanBuilder makeCondition(SearchCondition searchCondition){
        BooleanBuilder builder = new BooleanBuilder();
        if(hasText(searchCondition.getUsername()))
            builder.and(member.username.eq(searchCondition.getUsername()));
        if(hasText(searchCondition.getTeamname()))
            builder.and(team.name.eq(searchCondition.getTeamname()));
        if(searchCondition.getAgeGoe() != null)
            builder.and(member.age.goe(searchCondition.getAgeGoe()));
        if(searchCondition.getAgeLoe() != null)
            builder.and(member.age.loe(searchCondition.getAgeLoe()));

        return builder;
    }
//    private BooleanExpression matchUsername(String username){
//        return username != null ? member.username.eq(username) : null;
//    }
//    private BooleanExpression matchTeamName(String teamName) {
//        return teamName != null ? team.name.eq(teamName) : null;
//    }
//    private BooleanExpression goeAge(Integer ageGoe){
//        return ageGoe != null ? member.age.goe(ageGoe) : null;
//    }
//    private BooleanExpression loeAge(Integer ageLoe){
//        return ageLoe != null ? member.age.loe(ageLoe) : null;
//    }
}
