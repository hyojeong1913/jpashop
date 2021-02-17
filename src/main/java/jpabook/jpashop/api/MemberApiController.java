package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 등록 V1
     *
     * 요청 값으로 Member 엔티티를 직접 받는다.
     *
     * 문제점)
     * 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등)
     * 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기는 어렵다.
     * 엔티티가 변경되면 API 스펙이 변한다.
     *
     * API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는 것이 좋다.
     *
     * @param member
     * @return
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {

        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    /**
     * 등록 V2
     *
     * 요청 값으로 Member 엔티티 대신에 별도의 DTO를 받는다.
     *
     * 엔티티와 프레젠테이션 계층을 위한 로직을 분리할 수 있다.
     * 엔티티와 API 스펙을 명확하게 분리할 수 있다.
     * 엔티티가 변해도 API 스펙이 변하지 않는다.
     *
     * 참고) 실무에서는 엔티티를 API 스펙에 노출하면 안 된다.
     *
     * @param request
     * @return
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    /**
     * 회원 수정
     *
     * 회원 수정도 DTO를 요청 파라미터에 매핑
     *
     * 변경 감지를 사용해서 데이터를 수정
     *
     * PUT : 전체 업데이트할 때 사용
     * POST 또는 PATCH : 부분 업데이트할 때 사용
     * 하는 것이 REST 스타일에 맞다.
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        // 회원 정보 업데이트
        memberService.update(id, request.getName());

        // 업데이트된 회원 정보 조회
        Member findMember = memberService.findOne(id);

        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    /**
     * 회원 조회 V1
     *
     * 응답 값으로 엔티티를 직접 외부에 노출
     *
     * 문제점)
     * 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * 기본적으로 엔티티의 모든 값이 노출된다.
     * 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등)
     * 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
     * 엔티티가 변경되면 API 스펙이 변한다.
     * 추가로 컬렉션을 직접 반환하면 항후 API 스펙을 변경하기 어렵다. (별도의 Result 클래스 생성으로 해결)
     *
     * => API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
     *
     * @return
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {

        return memberService.findMembers();
    }

    /**
     * 회원 조회 V2
     *
     * 응답 값으로 엔티티가 아닌 별도의 DTO를 반환
     *
     * 엔티티가 변해도 API 스펙이 변경되지 않는다.
     *
     * @return
     */
    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();

        // 엔티티를 DTO 로 반환
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    /**
     * 회원 조회 V3
     *
     * V2 에서 추가적으로 결과 개수도 반환
     * Result2 클래스로 컬렉션을 감싸서 향후 필요한 필드(예: 결과 개수)를 추가하였다.
     *
     * @return
     */
    @GetMapping("/api/v3/members")
    public Result2 membersV3() {
        List<Member> findMembers = memberService.findMembers();

        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result2(collect, collect.size());
    }

    @Data
    static class CreateMemberRequest {

        private String name;
    }

    @Data
    static class CreateMemberResponse {

        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class UpdateMemberRequest {

        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {

        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {

        private T data;
    }

    @Data
    @AllArgsConstructor
    static class Result2<T> {

        private T data;
        private int count;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {

        private String name;
    }
}
