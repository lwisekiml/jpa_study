package jpa.jpastudy.jpql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpa.jpastudy.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

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
}
