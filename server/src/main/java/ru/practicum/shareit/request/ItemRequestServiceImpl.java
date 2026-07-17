package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        log.info("Начало процесса создания запроса вещи для пользователя с id: {}", userId);
        log.debug("Входящие данные запроса для сохранения: {}", dto);

        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }

        ItemRequest request = ItemRequestMapper.toItemRequest(dto);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);
        log.info("Успешно сохранен запрос вещи с id: {} для пользователя с id: {}", savedRequest.getId(), userId);
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        log.info("Запрос на получение списка собственных запросов пользователя с id: {}", userId);

        checkUserExists(userId);
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);

        log.info("Из БД извлечено {} собственных запросов для пользователя с id: {}", requests.size(), userId);
        return enrichRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        log.info("Запрос на получение списка всех чужих запросов. Инициатор id: {}, from: {}, size: {}", userId, from, size);

        checkUserExists(userId);
        Pageable pageable;
        if (from != null && size != null) {
            if (from < 0 || size <= 0) {
                throw new ValidationException("Невалидные данные пагинации");
            }
            pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        } else {
            pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("created").descending());
        }

        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNot(userId, pageable);
        log.info("Из БД извлечено {} чужих запросов на текущей странице пагинации", requests.size());
        return enrichRequestsWithItems(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.info("Запрос на получение детальной информации о запросе с id: {} от пользователя с id: {}", requestId, userId);
        checkUserExists(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Не найден запрос с id: " + requestId));

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        List<ItemDto> items = itemRepository.findAllByRequestId(requestId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        dto.setItems(items);

        log.info("Запрос с id: {} успешно найден и обогащен {} вещами-ответами", requestId, items.size());
        return dto;
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Проверка существования провалена. Пользователь с id: {} не найден в БД", userId);
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private List<ItemRequestDto> enrichRequestsWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        Map<Long, List<ItemDto>> itemsByRequestId = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.groupingBy(ItemDto::getRequestId));

        return requests.stream()
                .map(request -> {
                    ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
                    dto.setItems(itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}