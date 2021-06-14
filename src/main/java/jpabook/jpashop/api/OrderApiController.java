package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final OrderQueryRepository orderQueryRepository;

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
            orderItems.stream().forEach(o -> o.getItem().getName()); // Lazy 강제 초기화
        }

        return all;
    }

    /**
     * 주문 조회 V2
     * 엔티티를 DTO로 변환
     *
     * 지연 로딩으로 너무 많은 SQL 실행
     * SQL 실행 수
     *  - order 1번
     *  - member , address N번(order 조회 수 만큼)
     *  - orderItem N번(order 조회 수 만큼)
     *  - item N번(orderItem 조회 수 만큼)
     *
     * 참고)
     * 지연 로딩은 영속성 컨텍스트에 있으면 영속성 컨텍스트에 있는 엔티티를 사용하고 없으면 SQL을 실행한다.
     * 따라서 같은 영속성 컨텍스트에서 이미 로딩한 회원 엔티티를 추가로 조회하면 SQL을 실행하지 않는다.
     *
     * @return
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * 주문 조회 V3
     * 엔티티를 DTO 로 변환 - fetch join 최적화
     *
     * fetch join 으로 SQL 이 1번만 실행됨.
     *
     * @return
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * 주문 조회 V3.1
     * 엔티티를 DTO로 변환 - 페이징과 한계 돌파
     *
     * ToOne(OneToOne, ManyToOne) 관계만 우선 모두 fetch join 으로 최적화
     * 컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize 로 최적화
     *
     * default_batch_fetch_size 옵션 (현재 application.yml 에 global 하게 설정되어 있음.)
     * : 100 ~ 1000 사이를 선택하는 것을 권장
     * : 이 전략을 SQL IN 절을 사용하는데, 데이터베이스에 따라 IN 절 파라미터를 1000 으로 제한하기도 한다.
     * : 1000 으로 잡으면 한번에 1000 개를 DB 에서 애플리케이션에 불러오므로 DB 에 순간 부하가 증가할 수 있다.
     * : 하지만 애플리케이션은 100 이든 1000 이든 결국 전체 데이터를 로딩해야 하므로 메모리 사용량이 같다.
     * : 1000 으로 설정하는 것이 성능상 가장 좋지만, 결국 DB 든 애플리케이션이든 순간 부하를 어디까지 견딜 수 있는지로 결정하면 된다.
     * : 장점)
     * - 쿼리 호출 수가 1+N, 1+1 로 최적화된다.
     * - join 보다 DB 데이터 전송량이 최적화 된다.
     *   (Order 와 OrderItem 을 join 하면 Order 가 OrderItem 만큼 중복해서 조회되는데, 이 방법은 각각 조회하므로 전송해야할 중복 데이터가 없다.)
     * - fetch join 방식과 비교해서 쿼리 호출 수가 약간 증가하지만, DB 데이터 전송량이 감소한다.
     * - 컬렉션 fetch join 은 페이징이 불가능 하지만 이 방법은 페이징이 가능하다.
     *
     * 결론
     * ToOne 관계는 fetch join 해도 페이징에 영향을 주지 않으므로,
     * ToOne 관계는 fetch join 으로 쿼리 수를 줄이고 해결하고, 나머지는 hibernate.default_batch_fetch_size 로 최적화
     *
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit)
    {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * 주문 조회 V4
     * JPA에서 DTO 직접 조회
     *
     * Query: 루트 1번, 컬렉션 N 번 실행
     *
     * ToOne (N:1, 1:1) 관계들을 먼저 조회하고, ToMany (1:N) 관계는 각각 별도로 처리한다.
     * - ToOne 관계는 조인해도 데이터 row 수가 증가하지 않는다.
     * - ToMany (1:N) 관계는 조인하면 row 수가 증가한다.
     *
     * row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화 하기 쉬우므로 한번에 조회하고,
     * ToMany 관계는 최적화 하기 어려우므로 findOrderItems() 같은 별도의 메서드로 조회한다.
     *
     * @return
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * 주문 조회 V5
     * JPA 에서 DTO 직접 조회 - 컬렉션 조회 최적화
     *
     * Query: 루트 1번, 컬렉션 1번
     * ToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId 로 ToMany 관계인 OrderItem 을 한꺼번에 조회
     *
     * MAP 을 사용해서 매칭 성능 향상 (O(1))
     *
     * @return
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
