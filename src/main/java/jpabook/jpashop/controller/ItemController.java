package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * 상품 등록 페이지로 이동
     * @param model
     * @return
     */
    @GetMapping("/items/new")
    public String createForm(Model model) {

        model.addAttribute("form", new BookForm());

        return "items/createItemForm";
    }

    /**
     * 상품 등록
     * @param form
     * @return
     */
    @PostMapping("/items/new")
    public String create(BookForm form) {

        Book book = new Book();

        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);

        return "redirect:/";
    }

    /**
     * 상품 목록 페이지로 이동
     * @param model
     * @return
     */
    @GetMapping("/items")
    public String list(Model model) {

        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);

        return "items/itemList";
    }

    /**
     * 상품 수정 페이지로 이동
     * @param itemId
     * @param model
     * @return
     */
    @GetMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {

        // 수정할 상품 조회
        Book item = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();

        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        // 조회 결과를 모델에 담아서 뷰에 전달
        model.addAttribute("form", form);

        return "items/updateItemForm";
    }

    /**
     * 상품 수정
     *
     * 트랜잭션이 있는 서비스 계층에 식별자(id)와 변경할 데이터를 명확하게 전달 (파라미터 OR DTO)
     * 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고 엔티티의 데이터를 직접 변경
     * 트랜잭션 커밋 시점에 변경 감지 실행
     *
     * @param itemId
     * @param form
     * @return
     */
    @PostMapping("items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {

//        Book book = new Book();

//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());
//
//        itemService.saveItem(book);

        // 컨트롤러에서는 어설프게 엔티티 생성해서 사용하는 것은 좋지 않음.
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());

        return "redirect:/items";
    }
}
