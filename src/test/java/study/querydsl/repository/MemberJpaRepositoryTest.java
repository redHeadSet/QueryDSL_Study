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
class MemberJpaRepositoryTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private MemberJpaRepository memberJpaRepository;

    private void makeDefault() {
        Team team1 = new Team("team1"); em.persist(team1);
        Team team2 = new Team("team2"); em.persist(team2);

        Member member1 = new Member("mem1", 10); memberJpaRepository.save(member1); member1.changeTeam(team1);
        Member member2 = new Member("mem2", 20); memberJpaRepository.save(member2); member2.changeTeam(team1);
        Member member3 = new Member("mem3", 30); memberJpaRepository.save(member3); member3.changeTeam(team2);
        Member member4 = new Member("mem4", 40); memberJpaRepository.save(member4); member4.changeTeam(team2);
    }

    @Test
    public void normal_test() {
        // given
        makeDefault();

        // when
        List<Member> all = memberJpaRepository.findAll();

        // then
        Assertions.assertThat(all.size()).isEqualTo(4);
        for (Member each : all) {
            System.out.println("each = " + each.getUsername());
        }
    }

    @Test
    public void get_dto_data() {
        // given
        makeDefault();
        SearchCondition searchCondition
                = new SearchCondition(null, null, 20, null);

        // when
        List<MemberTeamDto> allByMTD = memberJpaRepository.findAllByMTD();
        List<MemberTeamDto> byCondition = memberJpaRepository.findByCondition(searchCondition);

        // then
        for (MemberTeamDto each : byCondition) {
            System.out.println("");
        }
    }
}