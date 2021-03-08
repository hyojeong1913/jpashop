package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 주문내역에서 추가로 주문한 상품 정보를 추가로 조회
 * Order 기준으로 컬렉션인 OrderItem 와 Item 이 필요
 *
 * 컬렉션인 일대다 관계(OneToMany)를 조회 및 최적화
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * 주문 조회 V1
     * 엔티티 직접 노출
     *
     * 엔티티가 변하면 API 스펙이 변한다.
     * 트랜잭션 안에서 지연 로딩 필요
     * 양방향 연관관계 문제
     *
     * Hibernate5Module 모듈 등록, LAZY=null 처리
     * 양방향 관계 문제 발생 -> @JsonIgnore
     *
     * orderItem , item 관계를 직접 초기화하면 Hibernate5Module 설정에 의해 엔티티를 JSON으로 생성한다.
     * 양방향 연관관계면 무한 루프에 걸리지 않게 한곳에 @JsonIgnore 를 추가해야 한다.
     * 엔티티를 직접 노출하므로 좋은 방법은 아니다.
     *
     * @return
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {

        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기

            List<OrderItem> orderItems = order.getOrderItems();

//            for (OrderItem orderItem : orderItems) {
//                orderItem.getItem().getName();
//            }

            // 위 반복문을 lambda 로 변경
            orderItems.stream().forEach(o -> o.getItem().getName()); // Lazy 강제 초기
        }

        return all;
    }
}
