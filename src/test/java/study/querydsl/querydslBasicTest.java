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

        Member member1 = new Member("mem1", 20);
        Member member2 = new Member("mem2", 30);
        Member member3 = new Member("mem3", 40);
        Member member4 = new Member("mem4", 50);
        Member member5 = new Member("mem5", 60);
        Member member6 = new Member("mem6", 70);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);

        Team team1 = new Team("team1");
        Team team2 = new Team("team2");
        em.persist(team1);
        em.persist(team2);

        member1.changeTeam(team1);
        member2.changeTeam(team1);
        member3.changeTeam(team1);
        member4.changeTeam(team2);
        member5.changeTeam(team2);
        member6.changeTeam(team2);
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
}