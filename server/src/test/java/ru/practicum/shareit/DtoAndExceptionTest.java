package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoAndExceptionTest {

    @Test
    void testExceptions() {
        NotFoundException notFound = new NotFoundException("Not Found");
        assertEquals("Not Found", notFound.getMessage());
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
        ItemDto dto1 = ItemDto.builder().id(1L).name("Item").description("Desc").available(true).build();
        ItemDto dto2 = ItemDto.builder().id(1L).name("Item").description("Desc").available(true).build();

        assertEquals(dto1, dto2);
        assertNotNull(dto1.toString());

        assertEquals(1L, dto1.getId());
        assertEquals("Item", dto1.getName());
        assertEquals("Desc", dto1.getDescription());
        assertTrue(dto1.getAvailable());
    }

    @Test
    void testCommentDto() {
        LocalDateTime now = LocalDateTime.now();
        CommentDto dto1 = CommentDto.builder().id(1L).text("Text").authorName("Author").created(now).build();

        assertNotNull(dto1.toString());
        assertNotEquals(0, dto1.hashCode());

        assertEquals(1L, dto1.getId());
        assertEquals("Text", dto1.getText());
        assertEquals("Author", dto1.getAuthorName());
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
        ItemRequestDto dto1 = ItemRequestDto.builder().id(1L).description("Desc").created(created).build();

        assertNotNull(dto1.toString());
        assertNotEquals(0, dto1.hashCode());

        assertEquals(1L, dto1.getId());
        assertEquals("Desc", dto1.getDescription());
        assertEquals(created, dto1.getCreated());
    }

    @Test
    void testUserEntity() {
        User user1 = User.builder().id(1L).name("Ivan").email("ivan@mail.com").build();

        assertNotNull(user1.toString());
        assertNotEquals(0, user1.hashCode());

        assertEquals(1L, user1.getId());
        assertEquals("Ivan", user1.getName());
        assertEquals("ivan@mail.com", user1.getEmail());

        // Покрываем NoArgsConstructor и Setter-методы
        User emptyUser = new User();
        emptyUser.setId(5L);
        emptyUser.setName("Empty");
        emptyUser.setEmail("empty@mail.com");
        assertEquals(5L, emptyUser.getId());
        assertEquals("Empty", emptyUser.getName());
        assertEquals("empty@mail.com", emptyUser.getEmail());
    }

    @Test
    void testItemEntity() {
        User owner = User.builder().id(1L).name("Ivan").build();
        ItemRequest request = ItemRequest.builder().id(1L).build();

        Item item1 = Item.builder().id(1L).name("Drill").description("Cool").available(true).owner(owner).request(request).build();

        assertNotNull(item1.toString());
        assertNotEquals(0, item1.hashCode());

        assertEquals(1L, item1.getId());
        assertEquals("Drill", item1.getName());
        assertEquals("Cool", item1.getDescription());
        assertTrue(item1.getAvailable());
        assertEquals(owner, item1.getOwner());
        assertEquals(request, item1.getRequest());

        Item emptyItem = new Item();
        emptyItem.setId(3L);
        emptyItem.setName("Empty");
        emptyItem.setDescription("Empty Desc");
        emptyItem.setAvailable(false);
        emptyItem.setOwner(owner);
        emptyItem.setRequest(request);

        assertEquals(3L, emptyItem.getId());
        assertEquals("Empty", emptyItem.getName());
        assertEquals("Empty Desc", emptyItem.getDescription());
        assertFalse(emptyItem.getAvailable());
        assertEquals(owner, emptyItem.getOwner());
        assertEquals(request, emptyItem.getRequest());
    }

    @Test
    void testBookingEntity() {
        User booker = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).build();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Booking booking1 = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        assertNotNull(booking1.toString());
        assertNotEquals(0, booking1.hashCode());

        assertEquals(1L, booking1.getId());
        assertEquals(start, booking1.getStart());
        assertEquals(end, booking1.getEnd());
        assertEquals(item, booking1.getItem());
        assertEquals(booker, booking1.getBooker());
        assertEquals(BookingStatus.APPROVED, booking1.getStatus());

        Booking emptyBooking = new Booking();
        emptyBooking.setId(2L);
        emptyBooking.setStart(start);
        emptyBooking.setEnd(end);
        emptyBooking.setItem(item);
        emptyBooking.setBooker(booker);
        emptyBooking.setStatus(BookingStatus.WAITING);

        assertEquals(2L, emptyBooking.getId());
        assertEquals(start, emptyBooking.getStart());
        assertEquals(end, emptyBooking.getEnd());
        assertEquals(item, emptyBooking.getItem());
        assertEquals(booker, emptyBooking.getBooker());
        assertEquals(BookingStatus.WAITING, emptyBooking.getStatus());
    }

    @Test
    void testCommentEntity() {
        User author = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).build();
        LocalDateTime created = LocalDateTime.now();

        Comment comment1 = Comment.builder().id(1L).text("Nice").item(item).author(author).created(created).build();

        assertNotNull(comment1.toString());
        assertNotEquals(0, comment1.hashCode());

        assertEquals(1L, comment1.getId());
        assertEquals("Nice", comment1.getText());
        assertEquals(item, comment1.getItem());
        assertEquals(author, comment1.getAuthor());
        assertEquals(created, comment1.getCreated());

        Comment emptyComment = new Comment();
        emptyComment.setId(2L);
        emptyComment.setText("Super");
        emptyComment.setItem(item);
        emptyComment.setAuthor(author);
        emptyComment.setCreated(created);

        assertEquals(2L, emptyComment.getId());
        assertEquals("Super", emptyComment.getText());
        assertEquals(item, emptyComment.getItem());
        assertEquals(author, emptyComment.getAuthor());
        assertEquals(created, emptyComment.getCreated());
    }

    @Test
    void testItemRequestEntity() {
        User requestor = User.builder().id(1L).build();
        LocalDateTime created = LocalDateTime.now();

        ItemRequest request1 = ItemRequest.builder().id(1L).description("Need").requestor(requestor).created(created).build();

        assertNotNull(request1.toString());
        assertNotEquals(0, request1.hashCode());

        assertEquals(1L, request1.getId());
        assertEquals("Need", request1.getDescription());
        assertEquals(requestor, request1.getRequestor());
        assertEquals(created, request1.getCreated());

        ItemRequest emptyRequest = new ItemRequest();
        emptyRequest.setId(2L);
        emptyRequest.setDescription("Want");
        emptyRequest.setRequestor(requestor);
        emptyRequest.setCreated(created);

        assertEquals(2L, emptyRequest.getId());
        assertEquals("Want", emptyRequest.getDescription());
        assertEquals(requestor, emptyRequest.getRequestor());
        assertEquals(created, emptyRequest.getCreated());
    }
}