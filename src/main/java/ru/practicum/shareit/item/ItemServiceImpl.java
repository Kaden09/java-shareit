package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Начало процесса создания вещи для пользователя с id:{}", userId);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка создания вещи: пользователь с id:{} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        ItemDto savedItem = ItemMapper.toItemDto(itemRepository.save(item));

        log.info("Вещь успешно создана с id:{} для владельца с id:{}", savedItem.getId(), userId);
        return savedItem;
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Начало обновления вещи с id:{} пользователем с id:{}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Ошибка обновления: вещь с id={} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });

        if (!item.getOwner().getId().equals(userId)) {
            log.warn("Ошибка обновления: пользователь с id:{} не является владельцем вещи с id:{}", userId, itemId);
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            log.debug("Обновление имени вещи с id:{} : {} -> {}", itemId, item.getName(), itemDto.getName());
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            log.debug("Обновление описания вещи с id:{}", itemId);
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            log.debug("Обновление статуса доступности вещи с id:{}: {}", itemId, itemDto.getAvailable());
            item.setAvailable(itemDto.getAvailable());
        }

        ItemDto updatedItem = ItemMapper.toItemDto(itemRepository.save(item));
        log.info("Вещь с id:{} успешно обновлена", itemId);
        return updatedItem;
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        log.info("Запрос вещи с id:{} от пользователя с id={}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Ошибка получения: вещь с id:{} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });
        return constructItemDto(item, userId);
    }

    @Override
    public List<ItemDto> getAllByOwnerId(Long userId) {
        log.info("Запрос списка всех вещей владельца с id:{}", userId);
        return itemRepository.findAllByOwnerIdOrderByIdAsc(userId).stream()
                .map(item -> constructItemDto(item, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Запрос поиска вещей по тексту: '{}'", text);
        if (text == null || text.isBlank()) {
            log.debug("Текст поиска пустой, возвращен пустой список");
            return Collections.emptyList();
        }
        List<ItemDto> foundItems = itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.info("Поиск завершен. Найдено вещей: {}", foundItems.size());

        return foundItems;
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info("Пользователь с id:{} пытается оставить комментарий к вещи с id:{}", userId, itemId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка добавления комментария: пользователь с id:{} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Ошибка добавления комментария: вещь с id:{} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });

        Booking booking = bookingRepository.findFirstByItemIdAndBookerIdAndStatusAndEndBefore(
                        itemId, userId, BookingStatus.APPROVED, LocalDateTime.now())
                .orElseThrow(() -> {
                    log.warn("Ошибка добавления комментария: у пользователя с id:{} нет завершенных бронирований для вещи с id:{}", userId, itemId);
                    return new ValidationException("Нельзя оставить комментарий без завершенной аренды");
                });

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build();

        CommentDto savedComment = CommentMapper.toCommentDto(commentRepository.save(comment));
        log.info("Комментарий с id:{} успешно добавлен к вещи с id:{}", savedComment.getId(), itemId);
        return savedComment;
    }

    private ItemDto constructItemDto(Item item, Long userId) {
        log.debug("Сборка ItemDto для вещи с id:{} (запрос от пользователя с id:{})", item.getId(), userId);
        ItemDto dto = ItemMapper.toItemDto(item);

        List<CommentDto> comments = commentRepository.findAllByItemId(item.getId())
                .stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
        dto.setComments(comments);
        log.debug("Добавлено комментариев к ItemDto: {}", comments.size());

        if (item.getOwner().getId().equals(userId)) {
            log.debug("Пользователь с id:{} является владельцем вещи с id:{}, добавляем информацию о бронированиях", userId, item.getId());
            LocalDateTime now = LocalDateTime.now();
            bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(item.getId(), BookingStatus.APPROVED, now)
                    .ifPresent(b -> {
                        dto.setLastBooking(new BookingShortDto(b.getId(), b.getBooker().getId()));
                        log.debug("Добавлено последнее бронирование с id:{}", b.getId());
                    });

            bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(item.getId(), BookingStatus.APPROVED, now)
                    .ifPresent(b -> {
                        dto.setNextBooking(new BookingShortDto(b.getId(), b.getBooker().getId()));
                        log.debug("Добавлено следующее бронирование с id:{}", b.getId());
                    });
        }
        return dto;
    }
}