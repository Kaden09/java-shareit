package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoAndExceptionTest {

    @Test
    void testExceptions() {
        NotFoundException notFound = new NotFoundException("Не найдено");
        assertEquals("Не найдено", notFound.getMessage());
    }

    @Test
    void testUserDto() {
        UserDto dto1 = UserDto.builder().id(1L).name("User").email("u@m.com").build();
        UserDto dto2 = UserDto.builder().id(1L).name("User").email("u@m.com").build();
        UserDto dto3 = UserDto.builder().id(2L).name("User2").email("u2@m.com").build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());

        assertEquals(1L, dto1.getId());
        assertEquals("User", dto1.getName());
        assertEquals("u@m.com", dto1.getEmail());
    }

    @Test
    void testItemDto() {
        ItemDto dto1 = ItemDto.builder().id(1L).name("вещь").description("Описание").available(true).build();
        ItemDto dto2 = ItemDto.builder().id(1L).name("вещь").description("Описание").available(true).build();

        assertEquals(dto1, dto2);
        assertNotNull(dto1.toString());

        assertEquals(1L, dto1.getId());
        assertEquals("вещь", dto1.getName());
        assertEquals("Описание", dto1.getDescription());
        assertTrue(dto1.getAvailable());
    }

    @Test
    void testCommentDto() {
        LocalDateTime now = LocalDateTime.now();
        CommentDto dto1 = CommentDto.builder().id(1L).text("Текст").authorName("Автор").created(now).build();

        assertNotNull(dto1.toString());
        assertNotEquals(0, dto1.hashCode());

        assertEquals(1L, dto1.getId());
        assertEquals("Текст", dto1.getText());
        assertEquals("Автор", dto1.getAuthorName());
        assertEquals(now, dto1.getCreated());
    }

    @Test
    void testBookingDto() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BookingDto dto1 = BookingDto.builder().itemId(1L).start(start).end(end).build();

        assertNotNull(dto1.toString());
        assertNotEquals(0, dto1.hashCode());

        assertEquals(1L, dto1.getItemId());
        assertEquals(start, dto1.getStart());
        assertEquals(end, dto1.getEnd());
    }

    @Test
    void testItemRequestDto() {
        LocalDateTime created = LocalDateTime.now();
        ItemRequestDto dto1 = ItemRequestDto.builder().id(1L).description("Описание").created(created).build();

        assertNotNull(dto1.toString());
        assertNotEquals(0, dto1.hashCode());

        assertEquals(1L, dto1.getId());
        assertEquals("Описание", dto1.getDescription());
        assertEquals(created, dto1.getCreated());
    }
}