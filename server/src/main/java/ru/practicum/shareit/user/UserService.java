package ru.practicum.shareit.user;

import java.util.List;

import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto getById(Long id);

    List<UserDto> getAll();

    UserDto update(Long id, UserDto userDto);

    void delete(Long id);
}