package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:shareit_test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrationTest {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Test
    void getAllByBooker_shouldReturnBookingsWithPagination() {
        User owner = userRepository.save(User.builder().name("Owner").email("owner@mail.com").build());
        User booker = userRepository.save(User.builder().name("Booker").email("booker@mail.com").build());

        Item item = itemRepository.save(Item.builder().name("Item").description("Desc").available(true).owner(owner).build());

        Booking b1 = Booking.builder().start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2)).item(item).booker(booker).status(BookingStatus.WAITING).build();
        Booking b2 = Booking.builder().start(LocalDateTime.now().plusDays(3)).end(LocalDateTime.now().plusDays(4)).item(item).booker(booker).status(BookingStatus.WAITING).build();
        bookingRepository.saveAll(List.of(b1, b2));

        List<BookingResponseDto> results = bookingService.getAllByBooker(booker.getId(), "ALL", 0, 1);

        assertThat(results, hasSize(1));
        assertThat(results.getFirst().getId(), equalTo(b2.getId()));
    }
}
