package ru.practicum.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.DuplicatedException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mappers.UserMapper;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Создание нового пользователя: {}", newUserRequest);
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new DuplicatedException("Пользовательс с email:" + newUserRequest.getEmail() + " уже существует");
        }
        User user = userMapper.mapToUser(newUserRequest);
        user = userRepository.save(user);
        log.info("userA" + user.isAllowSubscriptions());
        log.info("Успешное создание пользователя: {}", user);
        user.setAllowSubscriptions(true);
        return userMapper.mapToUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Попытка удаление пользователся в id:{}", id);
        userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id:" + id + " не найден"));
        userRepository.deleteById(id);
        log.info("Успешное удалении пользователя с id:{}", id);
    }

    @Override
    public List<UserDto> getAllUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        return userRepository.findAll(ids, pageable).stream().map(userMapper::mapToUserDto).toList();
    }


}
