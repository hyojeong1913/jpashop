package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * 일반적인 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회
 *
 * 장점)
 * SELECT 절에서 원하는 데이터를 직접 선택하므로 DB 애플리케이션 네트워크 용량 최적화(그러나 생각보다 미비)
 *
 * 단점)
 * 리포지토리 재사용성 떨어진다.
 * API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점
 */
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    public List<OrderSimpleQueryDto> findOrderDtos() {

        // new 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환
        return em.createQuery(
                "SELECT new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " FROM Order o" +
                        " JOIN o.member m" +
                        " JOIN o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
