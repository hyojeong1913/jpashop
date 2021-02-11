package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 상품 서비스는 상품 리포지토리에 단순히 위임만 하는 클래스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    /**
     * merge 방법
     *
     * 모든 데이터 필드를 수정
     * 넘어온 필드의 데이터가 없는 경우 null 로 처리 => 쓰지 않는 것이 좋음.
     *
     * @param item
     */
    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    /**
     * 변경 감지 기능 사용
     *
     * 영속성 컨텍스트가 자동 변경
     *
     * 파라미터가 많은 경우 뒤에 파라미터들보다는 updateItemDto 로 넘기는 것이 유지보수에 좋음.
     * @param itemId
     * @param name
     * @param price
     * @param stockQuantity
     */
    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {

        Item findItem = itemRepository.findOne(itemId);

        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);

        // 의미 있는 함수로 만들어서 변경하는 것이 좋음.
//        findItem.change(name, price, stockQuantity);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
