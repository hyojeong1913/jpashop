package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * xToOne(ManyToOne, OneToOne) 관계 최적화
 *
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /**
     * V1
     * 엔티티 직접 노출
     *
     * Hibernate5Module 모듈 등록, LAZY=null 처리
     * 양방향 관계 문제 발생 -> @JsonIgnore
     *
     * 엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은 꼭 한 곳을 @JsonIgnore 처리해야 한다.
     * 그렇지 않으면 양쪽을 서로 호출하면서 무한 루프가 걸린다.
     *
     * 엔티티를 API 응답으로 외부로 노출하는 것은 좋지 않다.
     * 따라서 Hibernate5Module 를 사용하기 보다는 DTO로 변환해서 반환하는 것이 더 좋은 방법이다.
     *
     * 주의)
     * 지연 로딩(LAZY)을 피하기 위해 즉시 로딩(EARGR)으로 설정하면 안 된다.
     * 즉시 로딩 때문에 연관관계가 필요 없는 경우에도 데이터를 항상 조회해서 성능 문제가 발생할 수 있다.
     * 즉시 로딩으로 설정하면 성능 튜닝이 매우 어려워진다.
     *
     * 항상 지연 로딩을 기본으로 하고, 성능 최적화가 필요한 경우에는 페치 조인(fetch join)을 사용하는 것이 좋다.
     *
     * @return
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {

        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for (Order order : all) {
            order.getMember().getName(); // LAZY 강제 초기화
            order.getDelivery().getAddress(); // LAZY 강제 초기화
        }

        return all;
    }
}
