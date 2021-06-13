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
        return em.createQuery(
                "SELECT o FROM Order o" +
                    " JOIN FETCH o.member m" +
                    " JOIN FETCH o.delivery d", Order.class
        ).getResultList();
    }

    /**
     * fetch join 으로 SQL 이 1번만 실행됨.
     *
     * distinct 를 사용한 이유
     * : 일대다 join 이 있으므로 데이터베이스 row 가 증가하며 그 결과 같은 order 엔티티의 조회 수도 증가하게 된다.
     * : JPA 의 distinct 는 SQL 에 distinct 를 추가하고, 더해서 같은 엔티티가 조회되면, 애플리케이션에서 중복을 걸러준다.
     * : 이 예에서 order 가 컬렉션 fetch join 때문에 중복 조회 되는 것을 막아준다.
     *
     * JPA 에서의 distinct 와 DB 에서의 distinct 의 차이점
     * : DB 에서는 row 의 모든 값들이 동일해야만 중복이 제거되나, JPA 에서는 id(PK) 가 같은 경우 중복 제거를 해 준다.
     *
     * 단점 : 페이징이 불가능
     *
     * 컬렉션 fetch join
     * : 컬렉션 fetch join 을 사용하면 페이징이 불가능하므로 매우 위험한 방법이다.
     * : hibernate 는 경고 로그를 남기면서 모든 데이터를 DB 에서 읽어오고, 메모리에서 페이징 해버린다.
     * : 컬렉션 fetch join 은 1개만 사용할 수 있다.
     * : 데이터가 부정합하게 조회될 수 있어 컬렉션 둘 이상에 fetch join 을 사용하면 안 된다.
     *
     * @return
     */
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "SELECT distinct o FROM Order o" +
                    " JOIN FETCH o.member m" +
                    " JOIN FETCH o.delivery d" +
                    " JOIN FETCH o.orderItems oi" +
                    " JOIN FETCH oi.item i", Order.class
        ).getResultList();
    }

    /**
     * 먼저 ToOne(OneToOne, ManyToOne) 관계를 모두 fetch join
     * ToOne 관계는 row 수를 증가시키지 않으므로 페이징 쿼리에 영향을 주지 않는다.
     *
     * 컬렉션은 지연 로딩으로 조회
     *
     * 지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size, @BatchSize 를 적용
     *
     * @param offset
     * @param limit
     * @return
     */
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "SELECT o FROM Order o" +
                        " JOIN FETCH o.member m" +
                        " JOIN FETCH o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
