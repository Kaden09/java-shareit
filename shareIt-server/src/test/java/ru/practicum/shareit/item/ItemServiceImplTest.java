package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
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
class ItemServiceImplTest {

    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;


    @Test
    void create_whenUserExists_shouldSaveItem() {
        ItemDto inputDto = ItemDto.builder().name("Дрель").description("Мощная").available(true).build();
        User owner = User.builder().id(1L).name("Иван").build();
        Item savedItem = Item.builder().id(1L).name("Дрель").description("Мощная").available(true).owner(owner).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.create(1L, inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Дрель", result.getName());
    }

    @Test
    void create_whenUserNotFound_shouldThrowNotFoundException() {
        ItemDto inputDto = ItemDto.builder().name("Дрель").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(99L, inputDto));
    }


    @Test
    void update_whenItemNotFound_shouldThrowNotFoundException() {
        ItemDto updateDto = ItemDto.builder().name("Новое имя").build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.update(1L, 99L, updateDto));
    }

    @Test
    void update_whenUserIsNotOwner_shouldThrowNotFoundException() {
        User owner = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).owner(owner).build();
        ItemDto updateDto = ItemDto.builder().name("Новое имя").build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.update(2L, 1L, updateDto));
    }

    @Test
    void update_withAllFields_shouldUpdateEverything() {
        User owner = User.builder().id(1L).build();
        Item existingItem = Item.builder().id(1L).name("Старая").description("Старая").available(false).owner(owner).build();
        ItemDto updateDto = ItemDto.builder().name("Новая").description("Новая").available(true).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertEquals("Новая", result.getName());
        assertEquals("Новая", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void update_withPartialFields_shouldUpdateOnlyNonNullFields() {
        User owner = User.builder().id(1L).build();
        ItemDto updateDto = ItemDto.builder().description("Обновленное описание").build();
        Item existingItem = Item.builder().id(1L).name("Дрель").description("Старая").available(true).owner(owner).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertEquals("Дрель", result.getName());
        assertEquals("Обновленное описание", result.getDescription());
        assertTrue(result.getAvailable());
    }


    @Test
    void getById_whenUserIsOwner_shouldReturnItemWithBookingsAndComments() {
        User owner = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).name("Дрель").owner(owner).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(1L)).thenReturn(Collections.emptyList());

        when(bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.empty());
        when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(anyLong(), any(), any()))
                .thenReturn(Optional.empty());

        ItemDto result = itemService.getById(1L, 1L);

        assertNotNull(result);
        assertEquals("Дрель", result.getName());
        assertNotNull(result.getComments());
    }

    @Test
    void getById_whenUserIsNotOwner_shouldReturnItemWithoutBookings() {
        User owner = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).name("Дрель").owner(owner).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(1L)).thenReturn(Collections.emptyList());

        ItemDto result = itemService.getById(1L, 2L);

        assertNotNull(result);
        assertEquals("Дрель", result.getName());
        assertNotNull(result.getComments());
    }

    @Test
    void getById_whenNotFound_shouldThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getById(99L, 1L));
    }

    @Test
    void getAllByOwnerId_shouldReturnList() {
        User owner = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).name("Дрель").owner(owner).build();

        when(itemRepository.findAllByOwnerIdOrderByIdAsc(eq(1L), any(Pageable.class))).thenReturn(List.of(item));

        List<ItemDto> result = itemService.getAllByOwnerId(1L, 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }


    @Test
    void search_whenTextIsBlank_shouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("   ", 0, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_whenTextNotBlank_shouldReturnMatches() {
        Item item = Item.builder().id(1L).name("Дрель").available(true).build();
        when(itemRepository.search(eq("дрель"), any(Pageable.class))).thenReturn(List.of(item));

        List<ItemDto> result = itemService.search("дрель", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }


    @Test
    void addComment_success_shouldSaveComment() {
        CommentDto commentDto = CommentDto.builder().text("Супер дрель!").build();
        User mockUser = User.builder().id(1L).name("Иван").build();
        Item mockItem = Item.builder().id(1L).name("Дрель").build();
        Booking mockBooking = new Booking();
        Comment savedComment = Comment.builder().id(1L).text("Супер дрель!").author(mockUser).item(mockItem).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(mockItem));
        when(bookingRepository.findFirstByItemIdAndBookerIdAndStatusAndEndBefore(
                anyLong(), anyLong(), any(), any()
        )).thenReturn(Optional.of(mockBooking));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto result = itemService.addComment(1L, 1L, commentDto);

        assertNotNull(result);
        assertEquals("Супер дрель!", result.getText());
        assertEquals("Иван", result.getAuthorName());
    }

    @Test
    void addComment_whenUserHasNoBooking_shouldThrowValidationException() {
        CommentDto commentDto = CommentDto.builder().text("Хорошая вещь").build();
        User mockUser = User.builder().id(1L).name("User").build();
        Item mockItem = Item.builder().id(1L).name("Drill").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(mockItem));
        when(bookingRepository.findFirstByItemIdAndBookerIdAndStatusAndEndBefore(
                anyLong(), anyLong(), any(), any()
        )).thenReturn(Optional.empty());

        assertThrows(jakarta.validation.ValidationException.class, () ->
                itemService.addComment(1L, 1L, commentDto)
        );
    }
}