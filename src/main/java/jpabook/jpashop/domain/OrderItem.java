package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

/**
 * 주문상품 엔티티
 */
@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 서비스에서 무분별한 객체 생성 방지
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item; // 주문 상품

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // 주문

    private int orderPrice; // 주문 가격
    private int count; // 주문 수량

    /**
     * 생성 메서드
     * 주문 상품, 가격, 수량 정보를 사용해서 주문 상품 엔티티를 생성
     * @param item
     * @param orderPrice
     * @param count
     * @return
     */
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();

         orderItem.setItem(item);
         orderItem.setOrderPrice(orderPrice);
         orderItem.setCount(count);

         // 주문한 수량만큼 상품의 재고를 줄인다.
         item.removeStock(count);

         return orderItem;
    }

    /**
     * 비즈니스 로직
     * 주문 취소 시 취소한 주문 수량만큼 상품의 재고를 증가시킨다.
     */
    public void cancel() {
        getItem().addStock(count);
    }

    /**
     * 조회 로직
     * 주문 상품 전체 가격 조회
     * 주문 가격에 수량을 곱한 값 반환
     * @return
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}