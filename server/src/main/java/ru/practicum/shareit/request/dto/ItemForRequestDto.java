package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.user.User;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemForRequestDto {
    private Long id;
    private String name;
    private User owner;
}