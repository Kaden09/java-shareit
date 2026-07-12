package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public UserDto create(UserDto userDto) {
        if (repository.emailExists(userDto.getEmail(), null)) {
            log.warn("Отказ в регистрации: email '{}' уже существует в системе", userDto.getEmail());
            throw new ConflictException("Email уже существует");
        }
        User user = repository.save(UserMapper.toUser(userDto));
        log.info("Успешно зарегистрирован новый пользователь с id:{}", user.getId());

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Запрошен несуществующий пользователь с id:{}", id);
                    return new NotFoundException("Пользователь не найден");
                });

        log.debug("Данные пользователя с id:{} получены из БД", id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        List<User> users = repository.findAll();

        log.debug("Запрошен список всех пользователей. Возвращено {} записей", users.size());

        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existing = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Не удалось обновить: пользователь с id:{} не найден", id);
                    return new NotFoundException("Пользователь не найден");
                });

        if (userDto.getEmail() != null) {
            if (repository.emailExists(userDto.getEmail(), id)) {
                log.warn("Конфликт при обновлении пользователя с id:{}, email '{}' занят другим пользователем",
                        id, userDto.getEmail());
                throw new ConflictException("Email уже используется другим пользователем");
            }
            existing.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            existing.setName(userDto.getName());
        }

        User updatedUser = repository.update(existing);
        log.info("Профиль пользователя с id:{} успешно обновлен", id);

        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);

        log.info("Пользователь с id:{} был удален", id);
    }
}