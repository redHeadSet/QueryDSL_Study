package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    public void casetest() {
        List<String> fetch1 = jpaQueryFactory
                .select(
                        member.age
                                .when(10).then("열살")
                                .when(20).then("슴살")
                                .when(30).then("서른")
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
                .select(member.username, Expressions.constant("A")) // 무조건 상수 A를 포함
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
        // JPQL 방식
        em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age)" +
                        " from Member m")
                .getResultList();

        // Setter 방식 - setter 함수를 사용하여 값이 입력됨
        List<MemberDto> setter = jpaQueryFactory
                .select(
                        Projections.bean(MemberDto.class,
                                member.username,
                                member.age)
                )
                .from(member)
                .fetch();

        // Field 방식 1 - setter 없이 바로 필드에 데이터 입력(필드명이 동일해야 함)
        List<MemberDto> field1 = jpaQueryFactory
                .select(
                        Projections.fields(MemberDto.class,
                                member.username,
                                member.age)
                )
                .from(member)
                .fetch();

        // Field 방식 2 - 만약, 필드명이 다를 경우 as 필요
        QMember subMember = new QMember("subMember");
        List<MemberDto2> field2 = jpaQueryFactory
                .select(
                        Projections.fields(MemberDto2.class,
                                member.username.as("name"), // field를 찾을 때 이름 기반으로 찾기 때문에 alias 설정
                                ExpressionUtils.as(         // 값을 sub_query 처리하고 싶을 때의 예시
                                        JPAExpressions
                                                .select(subMember.age.max())
                                                .from(subMember),
                                        "age"
                                )
                        )
                )
                .from(member)
                .fetch();

        // 생성자 방식 - 생성자에서 바로 입력 - 생성자는 Type을 보고 입력됨
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

        /* 장점
        컴파일 시점에 에러 잡힘 (잘못된 인자 등)
         */
        /* 단점
        @QueryProjection 넣어야 함 - 라이브러리 의존성이 생김
        Q클래스를 직접 생성해야 함
        DTO를 순수하게 쓰지 못함 : querydsl에 종속됨
         */
    }


}