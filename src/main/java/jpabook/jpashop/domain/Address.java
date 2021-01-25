package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

/**
 * 주소 값 타입
 */
@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    // JPA 스펙 상 엔티티나 임베디드 타입은 기본 생성자를 public 또는 protected 로 설정해야 한다.
    // public 보다는 protected 로 설정하는 것이 보다 안전
    // JPA 가 이런 제약을 두는 이유는 JPA 구현 라이브러리가 객체를 생성할 때 리플렉션 같은 기술을 사용할 수 있도록 지원해야 하기 때문이다.
    protected Address() {
    }

    // 생성자에서 값을 모두 초기화하여 변경 불가능한 클래스 생성
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}