package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Test
    void create_shouldSaveUserAndReturnDto() {
        UserDto inputDto = UserDto.builder().name("Пользователь").email("user@mail.com").build();
        User savedUser = User.builder().id(1L).name("Пользователь").email("user@mail.com").build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.create(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Пользователь", result.getName());
        assertEquals("user@mail.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getById_whenUserExists_shouldReturnUserDto() {
        User user = User.builder().id(1L).name("Пользователь").email("user@mail.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Пользователь", result.getName());
    }

    @Test
    void getById_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(99L));
    }

    @Test
    void getAllUsers_whenUsersExist_shouldReturnList() {
        User user = User.builder().id(1L).name("Пользователь").email("user@mail.com").build();
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Пользователь", result.getFirst().getName());
    }

    @Test
    void getAllUsers_whenNoUsers_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void update_whenUserNotFound_shouldThrowNotFoundException() {
        UserDto updateDto = UserDto.builder().name("Новое имя").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(99L, updateDto));
    }

    @Test
    void update_withAllFields_shouldUpdateAllFields() {
        User existingUser = User.builder().id(1L).name("Старое имя").email("old@mail.com").build();
        UserDto updateDto = UserDto.builder().name("Новое имя").email("new@mail.com").build();
        User updatedUser = User.builder().id(1L).name("Новое имя").email("new@mail.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.update(1L, updateDto);

        assertNotNull(result);
        assertEquals("Новое имя", result.getName());
        assertEquals("new@mail.com", result.getEmail());
    }

    @Test
    void update_withNameOnly_shouldUpdateOnlyName() {
        User existingUser = User.builder().id(1L).name("Старое имя").email("old@mail.com").build();
        UserDto updateDto = UserDto.builder().name("Новое имя").email(null).build();
        User updatedUser = User.builder().id(1L).name("Новое имя").email("old@mail.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.update(1L, updateDto);

        assertNotNull(result);
        assertEquals("Новое имя", result.getName());
        assertEquals("old@mail.com", result.getEmail());
    }

    @Test
    void update_withEmailOnly_shouldUpdateOnlyEmail() {
        User existingUser = User.builder().id(1L).name("Старое имя").email("old@mail.com").build();
        UserDto updateDto = UserDto.builder().name(null).email("new@mail.com").build();
        User updatedUser = User.builder().id(1L).name("Старое имя").email("new@mail.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.update(1L, updateDto);

        assertNotNull(result);
        assertEquals("Старое имя", result.getName());
        assertEquals("new@mail.com", result.getEmail());
    }


    @Test
    void deleteUser_shouldInvokeRepositoryDelete() {
        User mockUser = User.builder().id(1L).name("Пользователь").build();

        lenient().when(userRepository.existsById(1L)).thenReturn(true);
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        userService.delete(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}