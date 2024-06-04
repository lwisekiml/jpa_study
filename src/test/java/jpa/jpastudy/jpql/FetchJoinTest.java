package jpa.jpastudy.jpql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class FetchJoinTest {
    @PersistenceContext
    EntityManager em;

    @BeforeEach
    public void before() {
        Team teamA = new Team();
        teamA.setName("팀A");
        em.persist(teamA);

        Team teamB = new Team();
        teamB.setName("팀B");
        em.persist(teamB);

        Member member1 = new Member();
        member1.setUsername("회원1");
        member1.setAge(5);
        member1.setTeam(teamA);
        em.persist(member1);

        Member member2 = new Member();
        member2.setUsername("회원2");
        member2.setAge(30);
        member2.setTeam(teamA);
        em.persist(member2);

        Member member3 = new Member();
        member3.setUsername("회원3");
        member3.setAge(20);
        member3.setTeam(teamB);
        em.persist(member3);

        em.flush();
        em.clear();
    }

    @Test
    public void fetch_join_전() {
        String query = "select m from Member m";

        List<Member> result = em.createQuery(query, Member.class)
                .getResultList();

        for (Member member : result) {
            System.out.println("member = " + member.getUsername() + ", "
                                            + member.getTeam().getName());
            // 회원1, 팀A(SQL)
            // 회원2, 팀A(1차캐시)
            // 회원3, 팀B(SQL)

            // 회원 100명 -> N + 1
        }
    }

    @Test
    public void fetch_join_후() {
        String query = "select m from Member m join fetch m.team";

        List<Member> result = em.createQuery(query, Member.class)
                .getResultList();

        for (Member member : result) {
            System.out.println("member = " + member.getUsername() + ", "
                                            + member.getTeam().getName());
        }
    }

    @Test
    public void 컬렉션_fetch_join() {
        /*
           하이버네이트6 변경 사항
           DISTINCT가 추가로 애플리케이션에서 중복 제거시도
           -> 하이버네이트6 부터는 DISTINCT 명령어를 사용하지 않아도 애플리케이션에서 중복 제거가 자동으로 적용
           하이버네이트6 전에는 select distinct를 사용해야 중복이 제거 되었다.
         */
        // 하이버네이트6 전에는 result.size() 값은 3이 나온다. 조인하면서 뻥튀기가 되서 그렇다.
        String query = "select t from Team t join fetch t.members";
        List<Team> result = em.createQuery(query, Team.class)
                .getResultList();

        System.out.println("result.size() = " + result.size());

        for (Team team : result) {
            System.out.println("member = " + team.getName() + " | members = " + team.getMembers().size());
            for (Member member : team.getMembers()) {
                System.out.println("-> member = " + member);
            }
        }
    }

    @Test
    public void fetch_join_과_일반_조인_차이() {
        // 아래의 경우 select 절에서 team만 가져온다. 이 때도 6이전 에는 데이터 뻥튀기가 된다.
//        String query = "select t from Team t join t.members"; // 일반 조인 실행시 연관된 엔티티를 함께 조회X
        String query = "select t from Team t join fetch t.members";
        List<Team> result = em.createQuery(query, Team.class)
                .getResultList();

        System.out.println("result.size() = " + result.size());

        for (Team team : result) {
            System.out.println("member = " + team.getName() + " | members = " + team.getMembers().size());
            for (Member member : team.getMembers()) {
                System.out.println("-> member = " + member);
            }
        }
    }

    @Test
    public void fetch_join_은_대상에는_별칭x() {
        // 아래 처럼 페치 조인 대상에 별칭주는 것은 가급적 사용X (m / m.age)
        String query = "select t from Team t join fetch t.members m where m.age > 10"; // 일반 조인 실행시 연관된 엔티티를 함께 조회X
        List<Team> result = em.createQuery(query, Team.class)
                .getResultList();

        System.out.println("result.size() = " + result.size());

        for (Team team : result) {
            System.out.println("member = " + team.getName() + " | members = " + team.getMembers().size());
            for (Member member : team.getMembers()) {
                System.out.println("-> member = " + member);
            }
        }
    }

    @Test
    public void fetch_join_한계() {
        String query = "select t from Team t join fetch t.members m";
            /*
            WARN: HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory
            아래와 같이 쿼리에 페이징 관련 쿼리가 없다. 그래서 DB에서 이 팀에 대한 데이터를 다 끌고 온 것이다.
            select
                t1_0.id,
                t1_0.age,
                m1_0.TEAM_ID,
                m1_0.id,
                m1_0.age,
                m1_0.type,
                m1_0.username,
                t1_0.name
            from
                Team t1_0
            join
                Member m1_0
                    on t1_0.id=m1_0.TEAM_ID
            */
        List<Team> result = em.createQuery(query, Team.class)
                .setFirstResult(0)
                .setMaxResults(1)
                .getResultList();

        System.out.println("result.size() = " + result.size());

        for (Team team : result) {
            System.out.println("member = " + team.getName() + " | members = " + team.getMembers().size());
            for (Member member : team.getMembers()) {
                System.out.println("-> member = " + member);
            }
        }
    }

    @Test
    public void fetch_join_한계_해결방법() {
        // team에 @BatchSize(size = 100) 세팅
        // String query = "select m from Member m join fetch m.team t";
        // 한번에 팀A와 팀B와 연관된 멩버를 다 가져온다.
        String query = "select t from Team t";

        List<Team> result = em.createQuery(query, Team.class)
                .setFirstResult(0)
                .setMaxResults(2)
                .getResultList();

        System.out.println("result.size() = " + result.size());

        for (Team team : result) {
            System.out.println("member = " + team.getName() + "| members = " + team.getMembers().size());
            for (Member member : team.getMembers()) {
                System.out.println("-> member = " + member);
            }
        }
    }
}
