package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void getItemById_withInvalidId_shouldThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getById(1L, 1L));
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void updateItem_byNonOwner_shouldThrowNotFoundException() {
        Item item = Item.builder().id(1L).owner(ru.practicum.shareit.user.User.builder().id(2L).build()).build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.update(1L, 1L, null));
    }

    @Test
    void addComment_whenUserHasNoBooking_shouldThrowValidationException() {
        CommentDto commentDto = CommentDto.builder().text("Хорошая вещь").build();
        ru.practicum.shareit.user.User mockUser = ru.practicum.shareit.user.User.builder().id(1L).name("User").build();
        Item mockItem = Item.builder().id(1L).name("Дрель").build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(mockItem));
        when(bookingRepository.findFirstByItemIdAndBookerIdAndStatusAndEndBefore(
                anyLong(), anyLong(), any(), any()
        )).thenReturn(Optional.empty());

        assertThrows(jakarta.validation.ValidationException.class, () ->
                itemService.addComment(1L, 1L, commentDto)
        );
    }
}
