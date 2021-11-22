package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
class querydslBasicTest {
    @Autowired
    private EntityManager em;
    private JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    private void makeDefault() {
        jpaQueryFactory = new JPAQueryFactory(em);

        Team team1 = new Team("team1"); em.persist(team1);
        Team team2 = new Team("team2"); em.persist(team2);

        Member member1 = new Member("mem1", 20); em.persist(member1); member1.changeTeam(team1);
        Member member2 = new Member("mem2", 30); em.persist(member2); member2.changeTeam(team1);
        Member member3 = new Member("mem3", 40); em.persist(member3); member3.changeTeam(team1);

        Member member4 = new Member("mem4", 50); em.persist(member4); member4.changeTeam(team2);
        Member member5 = new Member("mem5", 60); em.persist(member5); member5.changeTeam(team2);
        Member member6 = new Member("mem6", 70); em.persist(member6); member6.changeTeam(team2);

        Member member7 = new Member(null, 70); em.persist(member7);
        Member member8 = new Member(null, 70); em.persist(member8);
    }

    @Test
    public void 쿼리DSL_select() {
        Member findedMem = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("mem1"))
                .fetchOne();

        Assertions.assertThat(findedMem.getAge()).isEqualTo(20);
    }

    @Test
    public void search() {
        List<Member> mem1 = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("mem1")
                        .and(member.age.eq(20)))
                .fetch();
        List<Member> mem2 = jpaQueryFactory
                .selectFrom(member)
                .where( member.username.eq("mem1"), // AND 조건인 경우, 쉼표로 구분 가능
                        member.age.eq(20))
                .fetch();

        Assertions.assertThat(mem1.size()).isEqualTo(1);
        Assertions.assertThat(mem1.get(0).getAge()).isEqualTo(20);
        Assertions.assertThat(mem1.get(0).getUsername()).isEqualTo("mem1");
    }

    @Test
    public void fetch(){
        QueryResults<Member> memberQueryResults = jpaQueryFactory
                .selectFrom(member)
                .fetchResults();

        long total = memberQueryResults.getTotal();
        long limit = memberQueryResults.getLimit();
        long offset = memberQueryResults.getOffset();
        List<Member> results = memberQueryResults.getResults();

        long l = jpaQueryFactory.selectFrom(member).fetchCount();
    }

//    정렬 순서
//    1. 나이 asc
//    2. 이름 desc
//    3. 이름이 없는 경우 맨 뒤
    @Test
    public void sortAndPaging() {
        QueryResults<Member> memberQueryResults = jpaQueryFactory
                .selectFrom(member)
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .offset(1)
                .limit(2)
                .fetchResults();
    }

    @Test
    public void aggregation() {
        List<Tuple> fetch = jpaQueryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.min(),
                        member.age.max()
                )
                .from(member)
                .fetch();
        Tuple tuple = fetch.get(0);
        Long aLong = tuple.get(member.count());
        Integer integer = tuple.get(member.age.sum());
        Double aDouble = tuple.get(member.age.avg());
        Integer integer1 = tuple.get(member.age.min());
        Integer integer2 = tuple.get(member.age.max());
    }

    @Test
    public void group() {
        List<Tuple> result = jpaQueryFactory
                .select(
                        member.team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(member.age.avg().gt(40))
                .fetch();

        for (Tuple each : result) {
            String teamName = each.get(member.team.name);
            Double teamAgeAvg = each.get(member.age.avg());
            System.out.println(teamName + " team : " + teamAgeAvg);
        }
    }

    /*
    조인 대상 필터링
     */
    @Test
    public void join_on_filter() {
        List<Tuple> result1 = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                    .on(team.name.eq("team1"))
                .fetch();

        // 위와 다르게 그대로 join 하는 경우에는 leftJoin 안에 인자가 1개만 넘어간다
        List<Tuple> result2 = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name)) // 사용자 이름과 팀 이름이 같은 경우 join
                                                                  // 정상적으로 결과가 나오진 않음 (억지로 한거라)
                                                                  // on을 사용하여 연관관계가 없는 대상을 join 가능
                .fetch();
    }

    @Test
    public void fetchJoin() {
        em.flush(); em.clear();

        // find One
        Member mem1 = jpaQueryFactory
                .select(member)
                .from(member)
                .join(member.team, team)
                .where(member.username.eq("mem1"))
                .fetchOne();
        mem1.getTeam().getName();   // 지연로딩이라 team 처리 시에 select 문이 또 나감

        // fetch join
        em.flush(); em.clear();

        Member mem2 = jpaQueryFactory
                .select(member)
                .from(member)
                .join(member.team, team).fetchJoin()    // fetchjoin 처리 시 아래 getTeam().getName() 처리 시
                                                        // select 처리 안하고 나감
                .where(member.username.eq("mem1"))
                .fetchOne();
        mem2.getTeam().getName();
    }

    @Test
    public void sub_query() {
        // 서브 쿼리 alias 는 겹치면 안되기 때문에 처리
        QMember subMember1 = new QMember("subMember1");
        List<Member> fetch1 = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(
                        JPAExpressions  // 서브 쿼리 처리
                                .select(subMember1.age.max())
                                .from(subMember1)
                ))
                .fetch();

        // IN 처리 예제
        QMember subMember2 = new QMember("subMember2");
        List<Member> fetch2 = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.age.in(
                        JPAExpressions  // 서브 쿼리 처리
                                .select(subMember2.age)
                                .from(subMember2)
                                .where(subMember2.age.goe(30))
                ))
                .fetch();

        // cf. from 절 내에 sub-query는 사용이 불가능하다!
    }
}