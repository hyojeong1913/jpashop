package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * 상품 - 도서 엔티티
 */
@Entity
@Getter @Setter
@DiscriminatorValue("B")
public class Book extends Item {

    private String Author;
    private String isbn;
}