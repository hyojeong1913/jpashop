package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

/**
 * 주문 엔티티
 */
@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 서비스에서 무분별한 객체 생성 방지
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 주문 회원

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery; // 배송 정보

    private LocalDateTime orderDate; // 주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문 상태 [ORDER, CANCLE]

    /**
     * 양방향 연관 관계 메서드
     * @param member
     */
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    /**
     * 양방향 연관 관계 메서드
     * @param orderItem
     */
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    /**
     * 양방향 연관 관계 메서드
     * @param delivery
     */
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    /**
     * 생성 메서드
     * 주문 엔티티를 생성할 때 사용
     * 주문 회원, 배송 정보, 주문 상품의 정보를 받아서 실제 주문 엔티티를 생성
     * @param member
     * @param delivery
     * @param orderItems
     * @return
     */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();

        order.setMember(member);
        order.setDelivery(delivery);

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    /**
     * 비즈니스 로직
     * 주문 취소 시 사용
     */
    public void cancel() {
        // 만약 이미 배송을 완료한 상품이면 주문을 취소하지 못하도록 예외를 발생시킨다.
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송 완료된 상품은 주문 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCLE);

        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    /**
     * 조회 로직
     * 전체 주문 가격 조회
     * 전체 주문 가격을 알려면 각각의 주문 상품 가격을 알아야 한다.
     * 연관된 주문 상품들의 가격을 조회해서 더한 값을 반환
     * 실무에서는 주로 주문에 전체 주문 가격 필드를 두고 역정규화 한다.
     * @return
     */
    public int getTotalPrice() {
        int totalPrice = 0;

        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }

        return totalPrice;

        // 위 내용을 한줄로 요약 가능
//        return orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }
}