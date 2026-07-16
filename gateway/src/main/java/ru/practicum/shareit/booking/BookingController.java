package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ValidationException;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookingController {
    private final BookingClient bookingClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                                @Valid @RequestBody BookingDto bookingDto) {
        log.info("Запрос на создание бронирования от пользователя с id:{}; (POST /bookings)", userId);
        log.debug("Данные для бронирования: {}", bookingDto);

        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ValidationException("Начальная и конечная даты не могут быть null");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) || bookingDto.getEnd().isEqual(bookingDto.getStart())) {
            throw new ValidationException("Конечная дата не может быть больше начальной");
        }
        return bookingClient.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @PathVariable Long bookingId,
                                                 @RequestParam Boolean approved) {
        log.info("Запрос на изменение статуса бронирования по id:{}; от пользователя с id:{}; approved={} (PATCH /bookings/{})",
                bookingId, userId, approved, bookingId);

        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @PathVariable Long bookingId) {
        log.info("Запрос на получение бронирования по id:{}; от пользователя с id:{}; (GET /bookings/{})",
                bookingId, userId, bookingId);

        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBooker(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @RequestParam(defaultValue = "ALL") String state,
                                                 @RequestParam(required = false) @PositiveOrZero Integer from,
                                                 @RequestParam(required = false) @Positive Integer size) {
        log.info("Запрос на получение бронирований автора по id:{}; state:{}, from:{}, size:{} (GET /bookings)",
                userId, state, from, size);

        validateState(state);
        return bookingClient.getAllByBooker(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                                @RequestParam(defaultValue = "ALL") String state,
                                                @RequestParam(required = false) @PositiveOrZero Integer from,
                                                @RequestParam(required = false) @Positive Integer size) {
        log.info("Запрос на получение бронирований владельца по id:{}; state:{}, from:{}, size:{} (GET /bookings/owner)",
                userId, state, from, size);

        validateState(state);
        return bookingClient.getAllByOwner(userId, state, from, size);
    }

    private void validateState(String stateStr) {
        try {
            BookingState.valueOf(stateStr);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Неизвестное состояние: " + stateStr);
        }
    }
}