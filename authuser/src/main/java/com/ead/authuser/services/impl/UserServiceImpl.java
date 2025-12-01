package com.ead.authuser.services.impl;

import com.ead.authuser.dtos.UserRecordDto;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.exceptions.ConflictException;
import com.ead.authuser.exceptions.NotFoundException;
import com.ead.authuser.mapper.UserMapper;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.repositories.UserRepository;
import com.ead.authuser.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserModel> findAll() {
        return userRepository.findAll();
    }

    @Override
    public UserModel findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public void deleteById(UUID userId) {
        UserModel user = findById(userId); // já lança NotFoundException
        userRepository.delete(user);
    }

    @Override
    public UserModel registerUser(UserRecordDto dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new ConflictException("Username is already taken");
        }
        UserModel user = userMapper.toEntity(dto);
        user.setUserStatus(UserStatus.ACTIVE);
        user.setUserType(UserType.USER);
        user.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        user.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        return userRepository.save(user);
    }

}
