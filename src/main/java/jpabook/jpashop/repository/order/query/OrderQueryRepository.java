package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
     * 컬렉션은 별도로 조회
     * Query: 루트 1번, 컬렉션 N 번
     * 단건 조회에서 많이 사용하는 방식
     *
     * @return
     */
    public List<OrderQueryDto> findOrderQueryDtos() {
        // 루트 조회 (toOne 코드를 모두 한번에 조회)
        List<OrderQueryDto> result = findOrders();

        // 루프를 돌면서 컬렉션 추가 (추가 쿼리 실행)
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return result;
    }

    /**
     * 최적화
     * Query: 루트 1번, 컬렉션 1번
     * 데이터를 한꺼번에 처리할 때 많이 사용하는 방식
     *
     * @return
     */
    public List<OrderQueryDto> findAllByDto_optimization() {
        // 루트 조회 (toOne 코드를 모두 한번에 조회)
        List<OrderQueryDto> result = findOrders();

        // orderItem 컬렉션을 MAP 한방에 조회
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

        // 루프를 돌면서 컬렉션 추가 (추가 쿼리 실행X)
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
            "SELECT NEW jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status,"
                    + " d.address, i.name, oi.orderPrice, oi.count)"
                    + " FROM Order o"
                    + " JOIN o.member m"
                    + " JOIN o.delivery d"
                    + " JOIN o.orderItems oi"
                    + " JOIN oi.item i", OrderFlatDto.class)
                .getResultList();
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
            "SELECT new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
                    + " FROM OrderItem oi"
                    + " JOIN oi.item i"
                    + " WHERE oi.order.id IN :orderIds", OrderItemQueryDto.class)
            .setParameter("orderIds", orderIds)
            .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }
    
    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    /**
     * 1:N 관계(컬렉션)를 제외한 나머지를 한번에 조회
     *
     * @param orderId
     * @return
     */
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "SELECT new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
                        + " FROM OrderItem oi"
                        + " JOIN oi.item i"
                        + " WHERE oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    /**
     * 1:N 관계인 orderItems 조회
     *
     * @return
     */
    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "SELECT new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"
                        + " FROM Order o"
                        + " join o.member m"
                        + " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }
}
