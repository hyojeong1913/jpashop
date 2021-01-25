package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 주문상품 엔티티
 */
@Entity
@Getter @Setter
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item; // 주문 상품

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order; // 주문

    private int orderPrice; // 주문 가격
    private int count; // 주문 수량
}