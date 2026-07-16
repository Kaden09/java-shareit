package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        booker = User.builder().id(1L).name("Booker").email("booker@mail.com").build();
        owner = User.builder().id(2L).name("Owner").email("owner@mail.com").build();
        item = Item.builder().id(1L).name("Дрель").description("Мощная").available(true).owner(owner).build();

        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void create_success_shouldSaveBooking() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        var result = bookingService.create(1L, bookingDto);

        assertNotNull(result);
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void create_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.create(99L, bookingDto));
    }

    @Test
    void create_whenItemNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(1L, bookingDto));
    }

    @Test
    void create_whenItemNotAvailable_shouldThrowValidationException() {
        item.setAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(jakarta.validation.ValidationException.class, () -> bookingService.create(1L, bookingDto));
    }

    @Test
    void create_whenBookerIsOwner_shouldThrowNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.create(2L, bookingDto));
    }

    @Test
    void approve_whenTrue_shouldSetStatusApproved() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = bookingService.approve(owner.getId(), 1L, true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void approve_whenFalse_shouldSetStatusRejected() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = bookingService.approve(owner.getId(), 1L, false);

        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void approve_whenUserIsNotOwner_shouldThrowValidationException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(jakarta.validation.ValidationException.class, () -> bookingService.approve(99L, 1L, true));
    }

    @Test
    void approve_whenAlreadyApproved_shouldThrowValidationException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(jakarta.validation.ValidationException.class, () -> bookingService.approve(owner.getId(), 1L, true));
    }

    @Test
    void getById_whenUserIsBookerOrOwner_shouldReturnBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        var resultByBooker = bookingService.getById(booker.getId(), 1L);
        assertNotNull(resultByBooker);

        var resultByOwner = bookingService.getById(owner.getId(), 1L);
        assertNotNull(resultByOwner);
    }

    @Test
    void getById_whenUserNotAuthorized_shouldThrowNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getById(99L, 1L));
    }

    @Test
    void getAllByBooker_withDifferentStates_shouldCallCorrectRepositoryMethods() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));

        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any(Pageable.class))).thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any(Pageable.class))).thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any(Pageable.class))).thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any(Pageable.class))).thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any(Pageable.class))).thenReturn(List.of(booking));

        assertFalse(bookingService.getAllByBooker(1L, "ALL", 0, 10).isEmpty());
        assertFalse(bookingService.getAllByBooker(1L, "WAITING", 0, 10).isEmpty());
        assertFalse(bookingService.getAllByBooker(1L, "REJECTED", 0, 10).isEmpty());
        assertFalse(bookingService.getAllByBooker(1L, "PAST", 0, 10).isEmpty());
        assertFalse(bookingService.getAllByBooker(1L, "FUTURE", 0, 10).isEmpty());
        assertFalse(bookingService.getAllByBooker(1L, "CURRENT", 0, 10).isEmpty());
    }

    @Test
    void getAllByBooker_whenUnknownState_shouldThrowException() {
        assertThrows(RuntimeException.class, () -> bookingService.getAllByBooker(1L, "UNKNOWN_STATE", 0, 10));
    }

    @Test
    void getAllByBooker_withInvalidPagination_shouldThrowValidationException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));

        assertThrows(jakarta.validation.ValidationException.class, () ->
                bookingService.getAllByBooker(1L, "ALL", -1, 10)
        );
        assertThrows(jakarta.validation.ValidationException.class, () ->
                bookingService.getAllByBooker(1L, "ALL", 0, 0)
        );
    }

    @Test
    void getAllByOwner_withDifferentStates_shouldCallCorrectRepositoryMethods() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(anyLong(), any(Pageable.class))).thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any(Pageable.class))).thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any(Pageable.class))).thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any(Pageable.class))).thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any(Pageable.class))).thenReturn(List.of(booking));

        assertFalse(bookingService.getAllByOwner(2L, "ALL", 0, 10).isEmpty());
        assertFalse(bookingService.getAllByOwner(2L, "WAITING", 0, 10).isEmpty());
        assertFalse(bookingService.getAllByOwner(2L, "REJECTED", 0, 10).isEmpty());
        assertFalse(bookingService.getAllByOwner(2L, "PAST", 0, 10).isEmpty());
        assertFalse(bookingService.getAllByOwner(2L, "FUTURE", 0, 10).isEmpty());
        assertFalse(bookingService.getAllByOwner(2L, "CURRENT", 0, 10).isEmpty());
    }

    @Test
    void getAllByBooker_withUnsupportedState_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        assertThrows(RuntimeException.class, () -> bookingService.getAllByBooker(1L, "UNSUPPORTED", 0, 10));
    }

    @Test
    void approve_whenStatusIsWaiting_shouldSetApproved() {
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = bookingService.approve(owner.getId(), 1L, true);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }
}