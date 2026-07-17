package ru.practicum.shareit;

import org.junit.jupiter.api.Test;

class ShareItAppTest {
    @Test
    void main() {
        System.setProperty("server.port", "0");
        ShareItApp.main(new String[] {});
    }
}