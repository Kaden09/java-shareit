package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemRequestController {
    private final ItemRequestClient requestClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(USER_ID_HEADER) Long userId,
                                                @Valid @RequestBody ItemRequestDto dto) {
        log.info("Запрос на создание запроса вещи от пользователя с id:{}; (POST /requests)", userId);
        log.debug("Данные запроса вещи: {}", dto);

        return requestClient.createRequest(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Запрос на получение собственных запросов вещей пользователя с id:{}; (GET /requests)", userId);

        return requestClient.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @RequestParam(required = false) @PositiveOrZero Integer from,
                                                 @RequestParam(required = false) @Positive Integer size) {
        log.info("Запрос на получение списка чужих запросов вещей для пользователя с id:{}; from:{}, size:{} (GET /requests/all)",
                userId, from, size);

        return requestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @PathVariable Long requestId) {
        log.info("Запрос на получение конкретного запроса вещи по id:{}; от пользователя с id:{}; (GET /requests/{})",
                requestId, userId, requestId);

        return requestClient.getRequestById(userId, requestId);
    }
}