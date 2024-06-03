package jpa.jpastudy.jpql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class JoinTest {

    @PersistenceContext
    EntityManager em;

    @Test
    public void 조인() {
        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUsername("teamA");
        member.setAge(10);

        member.setTeam(team);

        em.persist(member);

        em.flush();
        em.clear();

        // inner 생략 가능
        String innerQuery = "select m from Member m inner join m.team t"; // where t.name = :teamName";
        // select * from `member` m join team t on t.id = m.team_id;
        List<Member> innerResult = em.createQuery(innerQuery, Member.class)
                .getResultList();
        /* Member에 @ManyToOne(fetch = FetchType.LAZY) 안했을 경우
         * select
         *         m1_0.id,
         *         m1_0.age,
         *         m1_0.team_id,
         *         m1_0.username
         *     from
         *         member m1_0
         *     join
         *         team t1_0
         *             on t1_0.id=m1_0.team_id
         *
         * select
         *         t1_0.id,
         *         t1_0.age,
         *         t1_0.name
         *     from
         *         team t1_0
         *     where
         *         t1_0.id=?
         */

        /* Member에 @ManyToOne(fetch = FetchType.LAZY) 했을 경우
         * select
         *         m1_0.id,
         *         m1_0.age,
         *         m1_0.team_id,
         *         m1_0.username
         *     from
         *         member m1_0
         *     join
         *         team t1_0
         *             on t1_0.id=m1_0.team_id
         */
        System.out.println("===================================================================================");
        // outer 생략가능
        String outerQuery = "select m from Member m left outer join m.team t";
        // select * from `member` m;
        List<Member> outerResult = em.createQuery(outerQuery, Member.class)
                .getResultList();

        System.out.println("===================================================================================");
        // theta 조인
        String thetaQuery = "select m from Member m, Team t where m.username = t.name";
        // select * from `member` m, team t where m.username = t.name;
        List<Member> thetaResult = em.createQuery(thetaQuery, Member.class)
                .getResultList();
        // member.setUsername("member1"); 를 "teamA"로 바꾸면 1이 나온다.
        System.out.println("thetaResult.size() = " + thetaResult.size());

        System.out.println("===================================================================================");
        // 조인 대상 필터링
        String query = "select m from Member m left join m.team t on t.name = 'teamA'";
        // select * from `member` m left join team t on t.id = m.team_id and t.name = 'teamA';
        List<Member> result = em.createQuery(query, Member.class)
                .getResultList();
        System.out.println("result.size() = " + result.size());

        System.out.println("===================================================================================");
        // 연관관계 없는 엔티티 외브 조인
        String q = "select m from Member m left join Team t on m.username = t.name";
        // select * from `member` m left join team t on m.username = t.name;
        List<Member> r = em.createQuery(q, Member.class)
                .getResultList();
        // 조건을 만족하는 것이 없으므로 team 쪽은 null 값으로 join 된다.
        System.out.println("r.size = " + r.size());

        System.out.println("===================================================================================");
        // 서브 쿼리 - 아래와 같은 형식으로 사용할 수 있다 정도로만
        // 서브 쿼리(현재는 from도 서브쿼리 가능)
//        String subQuery = "select (select avg(m1.age) from Member m1) as avgAge from Member m left join Team t on m.username = t.name";
        String subQuery = "select mm.age, mm.username" +
                    " from (select m.age, m.username from Member m) as avgAge from Member m left join Team t on m.username = t.name";
    }
}
