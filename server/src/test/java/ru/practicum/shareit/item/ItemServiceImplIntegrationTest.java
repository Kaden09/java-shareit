package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:shareit_test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplIntegrationTest {

    private final ItemService itemService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Test
    void getAllByOwnerId_shouldReturnItemsWithPagination() {
        User owner = User.builder().name("Андрей").email("andrey@example.com").build();
        userRepository.save(owner);

        Item item1 = Item.builder().name("Дрель").description("Мощная дрель").available(true).owner(owner).build();
        Item item2 = Item.builder().name("Пила").description("Острая пила").available(true).owner(owner).build();
        Item item3 = Item.builder().name("Молоток").description("Тяжелый молоток").available(true).owner(owner).build();
        itemRepository.saveAll(List.of(item1, item2, item3));

        List<ItemDto> items = itemService.getAllByOwnerId(owner.getId(), 0, 2);

        assertThat(items, hasSize(2));
        assertThat(items.get(0).getName(), equalTo("Дрель"));
        assertThat(items.get(1).getName(), equalTo("Пила"));
    }
}