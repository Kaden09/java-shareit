package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingResponseDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                                     @RequestBody BookingDto dto) {
        log.info("Запрос на создание бронирования от пользователя с id:{}; (POST /bookings)", userId);
        log.debug("Данные для создания: {}", dto);

        return bookingService.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@RequestHeader(USER_ID_HEADER) Long userId,
                                      @PathVariable Long bookingId,
                                      @RequestParam Boolean approved) {
        log.info("Запрос на изменение статуса бронирования по id:{} от пользователя с id:{}; (PATCH /bookings/{})", bookingId, userId, bookingId);
        log.debug("Параметр подтверждения: approved={}", approved);

        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                      @PathVariable Long bookingId) {
        log.info("Запрос на получение бронирования по id:{} от пользователя с id:{}; (GET /bookings/{})", bookingId, userId, bookingId);

        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByBooker(@RequestHeader(USER_ID_HEADER) Long userId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(required = false) Integer from,
                                                   @RequestParam(required = false) Integer size) {
        log.info("Запрос на получение всех бронирований создателя по id:{}; (GET /bookings)", userId);
        log.debug("Параметры фильтрации: state={}, from={}, size={}", state, from, size);

        return bookingService.getAllByBooker(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                                  @RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(required = false) Integer from,
                                                  @RequestParam(required = false) Integer size) {
        log.info("Запрос на получение всех бронирований вещей владельца по id:{}; (GET /bookings/owner)", userId);
        log.debug("Параметр фильтрации: state={}, from={}, size={}", state, from, size);

        return bookingService.getAllByOwner(userId, state, from, size);
    }
}
