package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.hibernate.dialect.H2Dialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.MemberDto2;
import study.querydsl.dto.MemberDto_qp;
import study.querydsl.dto.QMemberDto_qp;
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
    public void ??????DSL_select() {
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
                .where( member.username.eq("mem1"), // AND ????????? ??????, ????????? ?????? ??????
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

//    ?????? ??????
//    1. ?????? asc
//    2. ?????? desc
//    3. ????????? ?????? ?????? ??? ???
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
    ?????? ?????? ?????????
     */
    @Test
    public void join_on_filter() {
        List<Tuple> result1 = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                    .on(team.name.eq("team1"))
                .fetch();

        // ?????? ????????? ????????? join ?????? ???????????? leftJoin ?????? ????????? 1?????? ????????????
        List<Tuple> result2 = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name)) // ????????? ????????? ??? ????????? ?????? ?????? join
                                                                  // ??????????????? ????????? ????????? ?????? (????????? ?????????)
                                                                  // on??? ???????????? ??????????????? ?????? ????????? join ??????
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
        mem1.getTeam().getName();   // ?????????????????? team ?????? ?????? select ?????? ??? ??????

        // fetch join
        em.flush(); em.clear();

        Member mem2 = jpaQueryFactory
                .select(member)
                .from(member)
                .join(member.team, team).fetchJoin()    // fetchjoin ?????? ??? ?????? getTeam().getName() ?????? ???
                                                        // select ?????? ????????? ??????
                .where(member.username.eq("mem1"))
                .fetchOne();
        mem2.getTeam().getName();
    }

    @Test
    public void sub_query() {
        // ?????? ?????? alias ??? ????????? ????????? ????????? ??????
        QMember subMember1 = new QMember("subMember1");
        List<Member> fetch1 = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(
                        JPAExpressions  // ?????? ?????? ??????
                                .select(subMember1.age.max())
                                .from(subMember1)
                ))
                .fetch();

        // IN ?????? ??????
        QMember subMember2 = new QMember("subMember2");
        List<Member> fetch2 = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.age.in(
                        JPAExpressions  // ?????? ?????? ??????
                                .select(subMember2.age)
                                .from(subMember2)
                                .where(subMember2.age.goe(30))
                ))
                .fetch();

        // cf. from ??? ?????? sub-query??? ????????? ???????????????!
    }

    @Test
    public void casetest() {
        List<String> fetch1 = jpaQueryFactory
                .select(
                        member.age
                                .when(10).then("??????")
                                .when(20).then("??????")
                                .when(30).then("??????")
                                .otherwise(member.age.stringValue())
                )
                .from(member)
                .fetch();

        List<String> fetch2 = jpaQueryFactory
                .select(
                        new CaseBuilder()
                                .when(member.age.between(0, 30)).then("0~30")
                                .when(member.age.between(31, 60)).then("31~60")
                                .otherwise("over 61")
                ).from(member)
                .fetch();
    }

    @Test
    public void constTest() {
        List<Tuple> fetch1 = jpaQueryFactory
                .select(member.username, Expressions.constant("A")) // ????????? ?????? A??? ??????
                .from(member)
                .where(member.username.eq("mem1"))
                .fetch();

        // {username}_{age}
        List<String> fetch2 = jpaQueryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();
    }

    @Test
    public void use_dto_test() {
        // JPQL ??????
        em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age)" +
                        " from Member m")
                .getResultList();

        // Setter ?????? - setter ????????? ???????????? ?????? ?????????
        List<MemberDto> setter = jpaQueryFactory
                .select(
                        Projections.bean(MemberDto.class,
                                member.username,
                                member.age)
                )
                .from(member)
                .fetch();

        // Field ?????? 1 - setter ?????? ?????? ????????? ????????? ??????(???????????? ???????????? ???)
        List<MemberDto> field1 = jpaQueryFactory
                .select(
                        Projections.fields(MemberDto.class,
                                member.username,
                                member.age)
                )
                .from(member)
                .fetch();

        // Field ?????? 2 - ??????, ???????????? ?????? ?????? as ??????
        QMember subMember = new QMember("subMember");
        List<MemberDto2> field2 = jpaQueryFactory
                .select(
                        Projections.fields(MemberDto2.class,
                                member.username.as("name"), // field??? ?????? ??? ?????? ???????????? ?????? ????????? alias ??????
                                ExpressionUtils.as(         // ?????? sub_query ???????????? ?????? ?????? ??????
                                        JPAExpressions
                                                .select(subMember.age.max())
                                                .from(subMember),
                                        "age"
                                )
                        )
                )
                .from(member)
                .fetch();

        // ????????? ?????? - ??????????????? ?????? ?????? - ???????????? Type??? ?????? ?????????
        List<MemberDto> constructor = jpaQueryFactory
                .select(
                        Projections.constructor(MemberDto.class,
                                member.username,
                                member.age)
                )
                .from(member)
                .fetch();
    }

    @Test
    public void query_projection_test() {
        List<MemberDto_qp> result = jpaQueryFactory
                .select(new QMemberDto_qp(member.username, member.age))
                .from(member)
                .fetch();

        /* ??????
        ????????? ????????? ?????? ?????? (????????? ?????? ???)
         */
        /* ??????
        @QueryProjection ????????? ??? - ??????????????? ???????????? ??????
        Q???????????? ?????? ???????????? ???
        DTO??? ???????????? ?????? ?????? : querydsl??? ?????????
         */
    }

    @Test
    public void dynamicQuery_test() {
        String name = "mem1";
        Integer age = 20;

        List<Member> result1 = use_BooleanBuilder(name, age);
        List<Member> result2 = use_multi_where(name, age);
        System.out.println();
    }

    public List<Member> use_BooleanBuilder(String username, Integer age) {
        BooleanBuilder builder = new BooleanBuilder();
        if(username != null)
            builder.and(member.username.eq(username));
        if(age != null)
            builder.and(member.age.eq(age));

        return jpaQueryFactory
                .select(member)
                .from(member)
                .where(builder)
                .fetch();
    }

    public List<Member> use_multi_where(String username, Integer age){
        return jpaQueryFactory
                .select(member)
                .from(member)
                .where(
                        usernameEq(username),
                        ageEq(age)
                )
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return (username != null) ? member.username.eq(username) : null;
    }

    private BooleanExpression ageEq(Integer age) {
        return (age != null) ? member.age.eq(age) : null;
    }

    // ???????????????, ?????? ????????? ?????? - ??? ??? ???????????? ?????????????????? ?????? ??????
    private BooleanExpression doubleCheck(String name, Integer age){
        return usernameEq(name).and(ageEq(age));
    }

    @Test
    public void bulk_test1() {
        long updated_count = jpaQueryFactory
                .update(member)
                .set(member.username, "?????????")
                .where(member.age.lt(45))
                .execute();
        // ??????!
        // ?????? ????????? DB??? ?????? update ?????? ???????????? ?????????
        // ????????? ??????????????? ??????????????? ?????????!
        em.flush(); em.clear();
    }

    @Test
    public void bulk_test2() {
        long updated_count = jpaQueryFactory
                .update(member)
                .set(member.age, member.age.add(-1))
                .execute();
        em.flush(); em.clear();
    }

    @Test
    public void bulk_test3() {
        long deleted_count = jpaQueryFactory
                .delete(member)
                .where(member.age.goe(50))
                .execute();
        em.flush(); em.clear();
    }

    @Test
    public void sql_func_test() {
        // ????????? ?????? ?????? function?????? ??????
        // H2Dialect ?????? registerFunction ????????? ??????????????? ????????????

        List<String> fetch1 = jpaQueryFactory
                .select(
                        Expressions.stringTemplate(
                                "function('replace', {0}, {1}, {2})",
                                member.username, "mem", "MEM"
                        )
                )
                .from(member)
                .fetch();

        List<String> fetch2 = jpaQueryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(
                        member.username.lower() // ??????????????? ????????? ????????? ??????, querydsl??? ???????????? ??????
                        )
                )
                .fetch();
    }

}