package ru.practicum.shareit.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);

        log.info("Успешно зарегистрирован новый пользователь с id:{}", user.getId());
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Запрошен несуществующий пользователь с id:{}", id);
                    return new NotFoundException("Пользователь не найден");
                });

        log.debug("Данные пользователя с id:{} получены из БД", id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();

        log.debug("Запрошен список всех пользователей. Возвращено {} записей", users.size());

        return users;
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Не удалось обновить: пользователь с id:{} не найден", id);
                    return new NotFoundException("Пользователь не найден");
                });

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }

        log.info("Профиль пользователя с id:{} успешно обновлен", id);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        userRepository.deleteById(id);

        log.info("Пользователь с id:{} был удален", id);
    }
}