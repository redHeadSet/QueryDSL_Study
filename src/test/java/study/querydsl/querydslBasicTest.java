package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static study.querydsl.entity.QMember.member;

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
}