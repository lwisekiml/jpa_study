package jpa.jpastudy.jpql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class JpqlTest {

    @PersistenceContext
    EntityManager em;

    @Test
    public void test() {
        Member member = new Member();
        member.setUsername("member1");
        em.persist(member);
    }

    @Test
    public void 기본문법과쿼리API() {
        Member member = new Member();
        member.setUsername("member1");
        member.setAge(10);
        em.persist(member);

//        Member member2 = new Member();
//        member2.setUsername("member2");
//        member2.setAge(30);
//        em.persist(member2);

        // TypedQuery : 반환 타입이 명확할 때 사용
        TypedQuery<Member> query1 = em.createQuery("select m from  Member m", Member.class);
        TypedQuery<String> query2 = em.createQuery("select m.username from Member m", String.class);
        TypedQuery<Integer> query3 = em.createQuery("select m.age from Member m", Integer.class);

        // Query : 반환 타입이 명확하지 않을 때 사용
        Query query4 = em.createQuery("select m.username, m.age from Member m");

        // 결과 조회
        TypedQuery<Member> query = em.createQuery("select m from  Member m", Member.class);
        Member result = query.getSingleResult();
        System.out.println("result = " + result);

        // 파라미터 바인딩
        Member bindingResult = em.createQuery("select m from Member m where m.username = :user", Member.class)
                .setParameter("user", "member1")
                .getSingleResult();
        System.out.println("result.getUsername() = " + bindingResult.getUsername());
    }

    @Test
    public void 프로젝션_SELECT() {
        Team team = new Team();
        team.setAge(25);
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUsername("member");
        member.setAge(10);
        member.setTeam(team);
        team.getMembers().add(member);

        em.persist(member);

        Member member2 = new Member();
        member2.setUsername("member2");
        member2.setAge(30);
        em.persist(member2);

        Order order = new Order();
        em.persist(order);
        Address address = new Address();
        address.setCity("city");
        address.setStreet("street");
        address.setZipcode("zipcode");
        order.setAddress(address);

        em.flush();
        em.clear();

        // select
        List<Member> selectResult = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        Member findMember = selectResult.get(0);
        findMember.setAge(20); // 20으로 수정

        System.out.println("===================================================================================");
        // join
        // 실제 쿼리에서 join이 들어가나 코드상에서 알기 어렵다
//        List<Team> result = em.createQuery("select m.team from Member m", Team.class)
//                                .getResultList();
        /**
         * select
         *         t1_0.id,
         *         t1_0.age,
         *         t1_0.username
         *     from
         *         member m1_0
         *     join
         *         team t1_0
         *             on t1_0.id=m1_0.team_id
         */

        // join이 들어간다는 것을 나타내었다.
        List<Team> result = em.createQuery("select t from Member m join m.team t", Team.class)
                .getResultList();

        System.out.println("===================================================================================");
        // 임베디드 타입 프로젝션
        Address singleResult = em.createQuery("select o.address from Order o", Address.class)
                .getSingleResult();

        System.out.println("===================================================================================");
        // 스칼라 타입 프로젝션 - 여러 값 조회
        // 1. Query 타입으로 조회
        List resultList = em.createQuery("select m.username, m.age from Member m").getResultList();

        Object o = resultList.get(0);
        Object[] ob = (Object[]) o;
        System.out.println("1 username = " + ob[0]); // member
        System.out.println("1 age = " + ob[1]); // 20

        // 2. Object[] 타입으로 조회
        List<Object[]> objectList = em.createQuery("select m.username, m.age from Member m").getResultList();
        Object[] objects = objectList.get(0);
        System.out.println("2 username = " + objects[0]);
        System.out.println("2 age = " + objects[1]);

        // 3. new 명령어 조회
        List<MemberDto> memberDtos = em.createQuery("select new jpa.jpastudy.jpql.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                                        .getResultList();
        MemberDto memberDTO = memberDtos.get(0);
        System.out.println("memberDTO username = " + memberDTO.getUsername());
        System.out.println("memberDTO age = " + memberDTO.getAge());
    }

    @Test
    public void 페이징() {
        for (int i = 0; i < 100; i++) {
            Member member = new Member();
            member.setUsername("member" + i);
            member.setAge(i);
            em.persist(member);
        }

        em.flush();
        em.clear();

        // 나이 많은 순 정렬
        List<Member> result = em.createQuery("select m from Member m order by m.age desc ", Member.class)
                .setFirstResult(90)
                .setMaxResults(100)
                .getResultList();

        System.out.println("result.size() = " + result.size());
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void JPQL_타입표현과_기타식() {
        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUsername("teamA");
        member.setAge(10);
        member.setType(MemberType.ADMIN);

        member.setTeam(team);

        em.persist(member);

        em.flush();
        em.clear();

        // 1
        String query1 = "select m.username, 'HELLO', TRUE from Member m " +
                        "where m.type = jpa.jpastudy.jpql.MemberType.ADMIN";
        List<Object[]> result1 = em.createQuery(query1)
                                    .getResultList();

        // 2
        String query2 = "select m.username, 'HELLO', TRUE from Member m " +
                        "where m.type = :userType";
//                        "where m.username is not null";
//                        "where m.age between 0 and 10";
        List<Object[]> result2 = em.createQuery(query2)
                                    .setParameter("userType", MemberType.ADMIN)
                                    .getResultList();

        for (Object[] objects : result2) {
            System.out.println("objects[0] = " + objects[0]);
            System.out.println("objects[0] = " + objects[1]);
            System.out.println("objects[0] = " + objects[2]);
        }

        System.out.println("===================================================================================");

        Book book = new Book();
        book.setName("JPA");
        book.setAuthor("kim");

        em.persist(book);

        List<Item> resultList = em.createQuery("select i from Item i where type (i) = Book", Item.class)
                .getResultList();
        for (Item item : resultList) {
            System.out.println("item = " + item);
        }

    }
}
