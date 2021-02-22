package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    /**
     * 주문 엔티티 저장
     * @param order
     */
    public void save(Order order) {
        em.persist(order);
    }

    /**
     * 단일 주문 검색
     * @param id
     * @return
     */
    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * JPQL 로 처리
     * 단점 : JPQL 쿼리를 문자로 생성하기는 번거롭고, 실수로 인한 버그가 충분히 발생할 수 있다.
     * @param orderSearch
     * @return
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {

        String jpql = "SELECT o FROM Order o JOIN o.member m";
        boolean isFirstCondition = true;

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " WHERE";
                isFirstCondition = false;
            } else {
                jpql += " AND ";
            }

            jpql += " o.status = :status";
        }

        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " WHERE";
                isFirstCondition = false;
            } else {
                jpql += " AND ";
            }

            jpql += " m.name LIKE :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class).setMaxResults(1000); // 최대 1000건

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }

        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    /**
     * JPA Criteria
     * 단점 : JPA 표준 스펙이지만, 쿼리가 쉽게 예상되지 않아 유지보수가 어렵다.
     * @param orderSearch
     * @return
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER); // 회원과 조인

        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);

        return query.getResultList();
    }

//    /**
//     * Querydsl 로 처리
//     * @param orderSearch
//     * @return
//     */
//    public List<Order> findAll(OrderSearch orderSearch) {
//
//        QOrder order = Qorder.order;
//        Qmember member = Qmember.member;
//
//        return query
//                .select(order)
//                .from(order)
//                .join(order.member, member)
//                .where(statusEq(orderSearch.getOrderStatus()),
//                        nameLike(orderSearch.getMemberName()))
//                .limit(1000)
//                .fetch();
//    }
//
//    private BooleanExpression statusEq(OrderStatus statusCond) {
//
//        if (statusCond == null) {
//            return null;
//        }
//
//        return order.status.eq(statusCond);
//    }
//
//    private BooleanExpression nameLike(String nameCond) {
//
//        if (!StringUtils.hasText(nameCond)) {
//            return null;
//        }
//
//        return member.name.like(nameCond);
//    }

    /**
     * 엔티티를 페치 조인(fetch join)을 사용해서 쿼리 1번에 조회
     *
     * @return
     */
    public List<Order> findAllWithMemberDelivery() {

        return em.createQuery("SELECT o FROM Order o" +
                                        " JOIN FETCH o.member m" +
                                        " JOIN FETCH o.delivery d", Order.class)
                .getResultList();
    }
}
