package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("Запрос пользователя с id:{} на добавление новой вещи (POST /items)", userId);
        log.debug("Данные новой вещи от пользователя с id {}: {}", userId, itemDto);

        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Запрос пользователя с id:{} на обновление вещи с id:{}; (PATCH /items/{})", userId, itemId, itemId);
        log.debug("Данные для обновления вещи c id:{} от пользователя с id {}: {}", itemId, userId, itemDto);

        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId, @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Запрос на получение данных о вещи с id:{}; (GET /items/{})", itemId, itemId);

        return itemService.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllByUserId(@RequestHeader(USER_ID_HEADER) Long userId,
                                        @RequestParam(required = false) Integer from,
                                        @RequestParam(required = false) Integer size) {
        log.info("Запрос пользователя с id:{} на получение списка всех своих вещей (GET /items)", userId);

        return itemService.getAllByOwnerId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text,
                                @RequestParam(required = false) Integer from,
                                @RequestParam(required = false) Integer size) {
        log.info("Запрос на поиск вещей по тексту: '{}'; (GET /items/search)", text);
        log.debug("Данные фильтрации: from={}, size={}", from, size);

        return itemService.search(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto commentDto) {
        log.info("Запрос на создание комментария к вещи с id:{} от пользователя с id:{}", itemId, userId);
        log.debug("Данные для создания комментария: {}", commentDto);

        return itemService.addComment(userId, itemId, commentDto);
    }
}
