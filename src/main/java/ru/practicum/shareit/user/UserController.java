package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService service;

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("Запрос на создание пользователя (POST /users)");
        log.debug("Данные для создания: {}", userDto);

        return service.create(userDto);
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        log.info("Запрос на получение пользователя по id:{}; (GET /users/{})", id, id);

        return service.getById(id);
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info("Запрос на получение всех пользователей (GET /users)");

        return service.getAll();
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto userDto) {
        log.info("Запрос на обновление пользователя по id:{}; (PATCH /users/{})", id, id);
        log.debug("Данные для обновления: {}", userDto);

        return service.update(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Запрос на удаление пользователя по id:{}; (DELETE /users/{})", id, id);

        service.delete(id);
    }
}
