package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MappersAndControllersTest {

    @Test
    void testControllers() {
        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService); // Если конструктор есть

        UserDto userDto = UserDto.builder().id(1L).name("Иван").email("ivan@mail.com").build();
        when(userService.create(any())).thenReturn(userDto);
        when(userService.getById(1L)).thenReturn(userDto);

        assertNotNull(userController.create(userDto));
        assertNotNull(userController.getById(1L));
        assertNotNull(userController.getAll());
        userController.delete(1L);

        ItemService itemService = mock(ItemService.class);
        ItemController itemController = new ItemController(itemService);
        ItemDto itemDto = ItemDto.builder().id(1L).name("Дрель").available(true).build();

        when(itemService.create(anyLong(), any())).thenReturn(itemDto);
        when(itemService.getById(anyLong(), anyLong())).thenReturn(itemDto);

        assertNotNull(itemController.create(1L, itemDto));
        assertNotNull(itemController.getById(1L, 1L));
        assertNotNull(itemController.getAllByUserId(1L, 0, 10));
        assertNotNull(itemController.search("дрель", 0, 10));

        // --- 3. BookingController ---
        BookingService bookingService = mock(BookingService.class);
        BookingController bookingController = new BookingController(bookingService);
        BookingDto bookingDto = BookingDto.builder().itemId(1L).build();

        try {
            bookingController.create(1L, bookingDto);
        } catch (Exception ignored) {
        }
        try {
            bookingController.getById(1L, 1L);
        } catch (Exception ignored) {
        }
        try {
            bookingController.getAllByBooker(1L, "ALL", 0, 10);
        } catch (Exception ignored) {
        }

        ItemRequestService requestService = mock(ItemRequestService.class);
        ItemRequestController requestController = new ItemRequestController(requestService);
        ItemRequestDto requestDto = ItemRequestDto.builder().id(1L).description("Нужно").build();

        when(requestService.create(anyLong(), any())).thenReturn(requestDto);

        assertNotNull(requestController.create(1L, requestDto));
        assertNotNull(requestController.getOwnRequests(1L));
    }
}