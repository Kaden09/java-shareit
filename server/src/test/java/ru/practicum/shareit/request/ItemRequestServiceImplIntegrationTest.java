package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:file:./db/shareit")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplIntegrationTest {

    private final ItemRequestService requestService;
    private final UserRepository userRepository;

    @Test
    void getAllRequests_shouldReturnOtherUsersRequestsWithPagination() {
        User user1 = userRepository.save(User.builder().name("Пользователь1").email("user1@mail.com").build());
        User user2 = userRepository.save(User.builder().name("Пользователь2").email("user2@mail.com").build());

        requestService.create(user1.getId(), ItemRequestDto.builder().description("Запрос 1 от пользователя1").build());
        requestService.create(user1.getId(), ItemRequestDto.builder().description("Запрос 2 от пользователя1").build());

        List<ItemRequestDto> results = requestService.getAllRequests(user2.getId(), 0, 5);

        assertThat(results, hasSize(2));
        assertThat(results.getFirst().getDescription(), equalTo("Запрос 2 от пользователя1"));
    }
}