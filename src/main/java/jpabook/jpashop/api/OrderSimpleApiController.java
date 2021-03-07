package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

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

    /**
     * V2
     * 엔티티를 조회해서 DTO로 변환 (fetch join 사용X)
     *
     * 단점)
     * 지연로딩으로 쿼리 N번 호출
     *
     * 엔티티를 DTO로 변환하는 일반적인 방법
     *
     * 쿼리가 총 1 + N + N번 실행된다. (v1과 쿼리수 결과는 같다.)
     * - order 조회 1번(order 조회 결과 수가 N이 된다.)
     * - order -> member 지연 로딩 조회 N 번
     * - order -> delivery 지연 로딩 조회 N 번
     * - 예)
     *      order의 결과가 4개면 최악의 경우 1 + 4 + 4번 실행된다.(최악의 경우)
     *
     * 지연로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생략한다.
     *
     * @return
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {

        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * V3
     * 엔티티를 페치 조인(fetch join)을 사용해서 쿼리 1번에 조회
     *
     * 페치 조인으로 order -> member, order -> delivery 는 이미 조회 된 상태 이므로 지연로딩 X
     *
     * @return
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {

        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * V4
     * JPA에서 DTO로 바로 조회
     *
     * 쿼리 한 번 호출
     * SELECT 절에서 원하는 데이터만 선택해서 조회
     *
     * V3 vs V4
     * V3 : 엔티티로 조회하 리포지토리 재사용성도 좋고, 개발도 단순해진다.
     * V4 : 애플리케이션 네트워크 용량 최적화
     *
     * 쿼리 방식 선택 권장 순서
     * - 1) 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
     * - 2) 필요하면 페치 조인으로 성능을 최적화 한다. (여기서 대부분의 성능 이슈가 해결된다.)
     * - 3) 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
     * - 4) 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
     *
     * @return
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {

        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {

            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
