package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.SearchCondition;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private MemberRepository memberRepository;

    private void makeDefault() {
        Team team1 = new Team("team1"); em.persist(team1);
        Team team2 = new Team("team2"); em.persist(team2);

        Member member1 = new Member("mem1", 10); memberRepository.save(member1); member1.changeTeam(team1);
        Member member2 = new Member("mem2", 20); memberRepository.save(member2); member2.changeTeam(team1);
        Member member3 = new Member("mem3", 30); memberRepository.save(member3); member3.changeTeam(team2);
        Member member4 = new Member("mem4", 40); memberRepository.save(member4); member4.changeTeam(team2);
    }

    @Test
    public void simple_test() {
        // given
        makeDefault();

        // when
        List<Member> all = memberRepository.findAll();

        // then
        Assertions.assertThat(all.size()).isEqualTo(4);
    }

    @Test
    public void get_dto_data() {
        // given
        makeDefault();
        SearchCondition searchCondition
                = new SearchCondition(null, "team1", null, null);

        // when
        List<MemberTeamDto> byCondition2 = memberRepository.findByConditionByMultiWhere(searchCondition);

        // then
        for (MemberTeamDto each : byCondition2) {
            System.out.println("");
        }
    }
}