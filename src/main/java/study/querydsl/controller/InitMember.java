package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
@Profile("local")
@RequiredArgsConstructor
public class InitMember {
    private final InitClass initClass;

    @PostConstruct
    public void Init() {
        initClass.init();
    }

    @Component
    static class InitClass{
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {
            Team team1 = new Team("team1"); em.persist(team1);
            Team team2 = new Team("team2"); em.persist(team2);

            for(int i = 0; i < 100; i++){
                Member member = new Member("mem"+i, i*10);
                em.persist(member);
                member.changeTeam( (i % 2 == 0) ? team2 : team1 );
            }
        }
    }
}
