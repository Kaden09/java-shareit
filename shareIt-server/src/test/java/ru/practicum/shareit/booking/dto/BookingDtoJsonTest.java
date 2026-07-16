package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testBookingDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2027, 10, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2027, 10, 15, 10, 0);

        BookingDto dto = BookingDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);

        String expectedStart = start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String expectedEnd = end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(expectedStart);
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(expectedEnd);
    }
}