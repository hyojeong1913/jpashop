package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class) // 스프링과 테스트 통합
@SpringBootTest // 스프링부터 띄우고 테스트 (만약, 이 코드가 없으면 @Autowired 다 실패)
@Transactional // 반복 가능한 테스트 지원. 각각의 테스트를 실행할 때마다 트랜잭션을 시작하고 테스트
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
    public void 회원가입() throws Exception {
        // given
        Member member = new Member();
        member.setName("Kim");
        
        // when
        Long saveId = memberService.join(member);
        
        // then
        em.flush();
        assertEquals(member, memberRepository.findOne(saveId));
    
    }
    
    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        // given
        Member member1 = new Member();
        member1.setName("Kim");
        Member member2 = new Member();
        member2.setName("Kim");
        
        // when
        memberService.join(member1);
        memberService.join(member2); // 에러 발생

        // then
        fail("예외가 발생해야 한다.");
    }
}
