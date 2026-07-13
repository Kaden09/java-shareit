package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Не удалось создать вещь: пользователь с id:{} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);

        log.info("Пользователь с id:{} успешно создал вещь с id:{}", userId, savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Не удалось обновить вещь: вещь с id:{} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });

        if (!existing.getOwner().getId().equals(userId)) {
            log.warn("Попытка несанкционированного доступа, пользователь с id:{} пытался обновить чужую вещь c id:{}", userId, itemId);
            throw new NotFoundException("Только владелец может редактировать вещь");
        }

        if (itemDto.getName() != null) existing.setName(itemDto.getName());
        if (itemDto.getDescription() != null) existing.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) existing.setAvailable(itemDto.getAvailable());

        Item updatedItem = itemRepository.update(existing);
        log.info("Вещь с id:{} успешно обновлена пользователем с id:{}", itemId, userId);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Запрошена несуществующая вещь с id:{}", itemId);
                    return new NotFoundException("Вещь не найдена");
                });

        log.debug("Вещь с id:{} получена из базы данных", itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllByUserId(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerId(userId);

        log.debug("Для пользователя с id:{} из базы извлечено {} вещей", userId, items.size());

        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        List<Item> items = itemRepository.search(text);

        log.debug("Поиск по тексту '{}' вернул {} результатов из БД", text, items.size());

        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}