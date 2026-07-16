package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:shareit_test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplIntegrationTest {

    private final UserService userService;

    @Test
    void update_shouldModifyUserFieldsInDatabase() {
        UserDto initialUser = UserDto.builder().name("Алексей").email("alex@mail.com").build();
        UserDto savedUser = userService.create(initialUser);

        UserDto updateFields = UserDto.builder().name("Александр").email("alexander@mail.com").build();
        UserDto updatedUser = userService.update(savedUser.getId(), updateFields);

        assertThat(updatedUser.getId(), equalTo(savedUser.getId()));
        assertThat(updatedUser.getName(), equalTo("Александр"));
        assertThat(updatedUser.getEmail(), equalTo("alexander@mail.com"));

        UserDto dbUser = userService.getById(savedUser.getId());
        assertThat(dbUser.getName(), equalTo("Александр"));
    }
}