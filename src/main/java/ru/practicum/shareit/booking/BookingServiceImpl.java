package ru.practicum.shareit.booking;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingDto dto) {
        log.info("Начало процесса создания бронирования для пользователя с id:{}", userId);
        log.debug("Данные бронирования: {}", dto);

        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().equals(dto.getStart())) {
            log.warn("Ошибка валидации дат: дата окончания {} не может быть раньше или равна дате начала {}", dto.getEnd(), dto.getStart());
            throw new ValidationException("Дата окончания не может быть раньше или равна дате начала");
        }
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка создания бронирования: пользователь с id:{} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> {
                    log.warn("Ошибка создания бронирования: вещь с id:{} не найдена", dto.getItemId());
                    return new NotFoundException("Вещь не найдена");
                });

        if (!item.getAvailable()) {
            log.warn("Ошибка создания бронирования: вещь с id:{} недоступна для бронирования", item.getId());
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            log.warn("Ошибка создания бронирования: владелец с id:{} пытается забронировать свою же вещь с id:{}", userId, item.getId());
            throw new NotFoundException("Владелец не может забронировать свою вещь");
        }

        Booking booking = Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        BookingResponseDto response = BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
        log.info("Бронирование успешно создано с id:{} для пользователя с id:{}", response.getId(), userId);
        return response;
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long userId, Long bookingId, Boolean approved) {
        log.info("Начало процесса подтверждения бронирования с id:{} пользователем с id:{}", bookingId, userId);
        log.debug("Параметр подтверждения: approved={}", approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Ошибка подтверждения: бронирование с id:{} не найдено", bookingId);
                    return new NotFoundException("Бронирование не найдено");
                });

        if (booking.getBooker().getId().equals(userId)) {
            log.warn("Ошибка подтверждения: арендатор с id:{} не может одобрить собственное бронирование с id:{}", userId, bookingId);
            throw new NotFoundException("Арендатор не может подтвердить собственное бронирование");
        }

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Ошибка подтверждения: пользователь с id:{} не является владельцем вещи с id:{}", userId, booking.getItem().getId());
            throw new ValidationException("Только владелец вещи может подтверждать бронирование");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            log.warn("Ошибка подтверждения: у бронирования с id:{} уже изменен статус на {}", bookingId, booking.getStatus());
            throw new ValidationException("Статус уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        BookingResponseDto response = BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
        log.info("Статус бронирования с id:{} успешно изменен владельцем на {}", bookingId, response.getStatus());
        return response;
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        log.info("Запрос на получение бронирования по id:{} от пользователя с id:{}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Ошибка получения: бронирование с id:{} не найдено", bookingId);
                    return new NotFoundException("Бронирование не найдено");
                });

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Ошибка доступа: пользователь с id:{} не связан с бронированием с id:{}", userId, bookingId);
            throw new NotFoundException("Доступ запрещен");
        }
        log.info("Бронирование с id:{} успешно получено пользователем с id:{}", bookingId, userId);
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long userId, String stateString) {
        log.info("Запрос на получение списка бронирований создателя с id:{}", userId);
        log.debug("Параметр фильтрации state: {}", stateString);

        userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Ошибка получения списка бронирований: пользователь с id:{} не найден", userId);
            return new NotFoundException("Пользователь не найден");
        });
        BookingState state = parseState(stateString);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            default:
                log.warn("Неподдерживаемое состояние фильтрации: {}", stateString);
                throw new ValidationException("Unknown state: " + stateString);
        }

        log.info("Найдено бронирований создателя с id:{} в состоянии {}: {}", userId, state, bookings.size());
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long userId, String stateString) {
        log.info("Запрос на получение списка бронирований вещей владельца с id:{}", userId);
        log.debug("Параметр фильтрации state: {}", stateString);

        userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Ошибка получения списка бронирований владельца: пользователь с id:{} не найден", userId);
            return new NotFoundException("Пользователь не найден");
        });
        BookingState state = parseState(stateString);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            default:
                log.warn("Неподдерживаемое состояние фильтрации: {}", stateString);
                throw new ValidationException("Unknown state: " + stateString);
        }

        log.info("Найдено бронирований владельца с id:{} в состоянии {}: {}", userId, state, bookings.size());
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    private BookingState parseState(String state) {
        log.debug("Парсинг состояния бронирования из строки: {}", state);
        try {
            return BookingState.valueOf(state);
        } catch (Exception e) {
            log.warn("Ошибка парсинга состояния: не удалось преобразовать строку '{}' в BookingState", state);
            throw new ValidationException("Unknown state: " + state);
        }
    }
}