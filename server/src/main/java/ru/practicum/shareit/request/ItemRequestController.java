package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService requestService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @RequestBody ItemRequestDto dto) {
        log.info("Запрос на создание запроса вещи от пользователя с id:{}; (POST /requests)", userId);
        log.debug("Данные запроса вещи: {}", dto);

        return requestService.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Запрос на получение собственных запросов вещей пользователя с id:{}; (GET /requests)", userId);

        return requestService.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                               @RequestParam(required = false) Integer from,
                                               @RequestParam(required = false) Integer size) {
        log.info("Запрос на получение списка чужих запросов вещей для пользователя с id:{}; from:{}, size:{} (GET /requests/all)",
                userId, from, size);

        return requestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @PathVariable Long requestId) {
        log.info("Запрос на получение конкретного запроса вещи по id:{}; от пользователя с id:{}; (GET /requests/{})",
                requestId, userId, requestId);

        return requestService.getRequestById(userId, requestId);
    }
}