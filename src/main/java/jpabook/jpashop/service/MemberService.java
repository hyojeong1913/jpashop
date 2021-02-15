package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 데이터의 변경이 없는 읽기 전용의 메서드에 사용
// 영속성 컨테스트를 플러시 하지 않으므로 약간의 성능 향상
// 데이터베이스 드라이버가 지원하면 DB 에서 성능 향상
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor // final 키워드가 추가된 필드만 생성자 만들어준다.
public class MemberService {

    // final 키워드를 추가하면 컴파일 시점에 memberRepository 를 설정하지 않는 오류를 체크 가능
    private final MemberRepository memberRepository;

    /**
     * 회원가입
     * @param member
     * @return
     */
    @Transactional // 데이터 변경 가능
    public Long join(Member member) {
        // 중복 회원 검증
        validateDuplicateMember(member);

        // 회원 저장
        memberRepository.save(member);

        return member.getId();
    }

    /**
     * 중복 회원 검증
     * @param member
     */
    private void validateDuplicateMember(Member member) {

        List<Member> findMembers = memberRepository.findByName(member.getName());

        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     * @return
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     * 회원 조회
     * @param memberId
     * @return
     */
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    /**
     * 회원 수정
     * @param id
     * @param name
     */
    @Transactional
    public void update(Long id, String name) {

        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}