package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@Validated(UserDto.Create.class) @RequestBody UserDto userDto) {
        log.info("Запрос на создание пользователя (POST /users)");
        log.debug("Данные для создания: {}", userDto);

        return userClient.createUser(userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable Long userId) {
        log.info("Запрос на получение пользователя по id:{}; (GET /users/{})", userId, userId);

        return userClient.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Запрос на получение всех пользователей (GET /users)");

        return userClient.getAllUsers();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId, @Validated(UserDto.Update.class) @RequestBody UserDto userDto) {
        log.info("Запрос на обновление пользователя по id:{}; (PATCH /users/{})", userId, userId);
        log.debug("Данные для обновления: {}", userDto);

        return userClient.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long userId) {
        log.info("Запрос на удаление пользователя по id:{}; (DELETE /users/{})", userId, userId);

        return userClient.deleteUser(userId);
    }
}