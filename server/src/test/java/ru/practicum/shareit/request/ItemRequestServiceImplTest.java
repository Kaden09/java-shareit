package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Test
    void create_whenValid_shouldSaveRequest() {
        ItemRequestDto dto = ItemRequestDto.builder().description("Нужен молоток").build();
        User user = User.builder().id(1L).name("Иван").build();
        ItemRequest savedRequest = ItemRequest.builder().id(1L).description("Нужен молоток").requestor(user).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(savedRequest);

        ItemRequestDto result = requestService.create(1L, dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Нужен молоток", result.getDescription());
    }

    @Test
    void create_whenUserNotFound_shouldThrowNotFoundException() {
        ItemRequestDto dto = ItemRequestDto.builder().description("Нужен молоток").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.create(99L, dto));
    }

    @Test
    void create_whenDescriptionEmpty_shouldThrowValidationException() {
        ItemRequestDto dto = ItemRequestDto.builder().description("   ").build();
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> requestService.create(1L, dto));
    }

    @Test
    void getOwnRequests_whenUserExists_shouldReturnList() {
        User user = User.builder().id(1L).build();
        ItemRequest request = ItemRequest.builder().id(1L).description("Описание").build();

        Item item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .request(request)
                .build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findAllByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(request));
        when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of(item));

        List<ItemRequestDto> result = requestService.getOwnRequests(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getItems().size());
    }

    @Test
    void getOwnRequests_whenNoRequests_shouldReturnEmptyList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findAllByRequestorIdOrderByCreatedDesc(1L)).thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = requestService.getOwnRequests(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllRequests_withValidPagination_shouldReturnPage() {
        ItemRequest request = ItemRequest.builder().id(1L).description("Описание").build();
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findAllByRequestorIdNot(eq(1L), any(Pageable.class))).thenReturn(List.of(request));

        List<ItemRequestDto> result = requestService.getAllRequests(1L, 0, 5);

        assertFalse(result.isEmpty());
    }

    @Test
    void getAllRequests_withoutPagination_shouldReturnAll() {
        ItemRequest request = ItemRequest.builder().id(1L).description("Описание").build();
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findAllByRequestorIdNot(eq(1L), any(Pageable.class))).thenReturn(List.of(request));

        List<ItemRequestDto> result = requestService.getAllRequests(1L, null, null);

        assertFalse(result.isEmpty());
    }

    @Test
    void getAllRequests_withInvalidPagination_shouldThrowValidationException() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(ValidationException.class, () -> requestService.getAllRequests(1L, -1, 5));
        assertThrows(ValidationException.class, () -> requestService.getAllRequests(1L, 0, 0));
    }

    @Test
    void getRequestById_whenExists_shouldReturnRequest() {
        ItemRequest request = ItemRequest.builder().id(1L).description("Описание").build();
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequestId(1L)).thenReturn(Collections.emptyList());

        ItemRequestDto result = requestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals("Описание", result.getDescription());
    }

    @Test
    void getRequestById_whenRequestNotFound_shouldThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getRequestById(1L, 99L));
    }

    @Test
    void checkUserExists_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> requestService.getOwnRequests(1L));
    }
}