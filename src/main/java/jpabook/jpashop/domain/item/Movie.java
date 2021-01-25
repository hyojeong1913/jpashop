package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * 상품 - 영화 엔티티
 */
@Entity
@Getter @Setter
@DiscriminatorValue("M")
public class Movie extends Item {

    private String director;
    private String actor;
}