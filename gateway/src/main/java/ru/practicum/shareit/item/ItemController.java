package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {
    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @Validated(ItemDto.Create.class) @RequestBody ItemDto itemDto) {
        log.info("Запрос на создание вещи от пользователя с id:{}; (POST /items)", userId);
        log.debug("Данные для создания вещи: {}", itemDto);

        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @PathVariable Long itemId,
                                             @Validated(ItemDto.Update.class) @RequestBody ItemDto itemDto) {
        log.info("Запрос на обновление вещи по id:{}; от пользователя с id:{}; (PATCH /items/{})", itemId, userId, itemId);
        log.debug("Данные для обновления вещи: {}", itemDto);

        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @PathVariable Long itemId) {
        log.info("Запрос на получение вещи по id:{}; от пользователя с id:{}; (GET /items/{})", itemId, userId, itemId);

        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @RequestParam(required = false) @PositiveOrZero Integer from,
                                              @RequestParam(required = false) @Positive Integer size) {
        log.info("Запрос на получение всех вещей владельца с id:{}; from:{}, size:{} (GET /items)", userId, from, size);

        return itemClient.getAllItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text,
                                              @RequestParam(required = false) @PositiveOrZero Integer from,
                                              @RequestParam(required = false) @Positive Integer size) {
        log.info("Запрос на поиск вещей по тексту: \"{}\"; from:{}, size:{} (GET /items/search)", text, from, size);

        return itemClient.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentDto commentDto) {
        log.info("Запрос на добавление комментария к вещи по id:{}; от пользователя с id:{}; (POST /items/{}/comment)",
                itemId, userId, itemId);
        log.debug("Данные комментария: {}", commentDto);

        return itemClient.addComment(userId, itemId, commentDto);
    }
}