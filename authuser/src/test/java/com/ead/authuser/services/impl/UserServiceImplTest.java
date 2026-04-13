package com.ead.authuser.services.impl;

import com.ead.authuser.dtos.UserRecordDto;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.exceptions.ConflictException;
import com.ead.authuser.exceptions.NotFoundException;
import com.ead.authuser.exceptions.UnauthorizedException;
import com.ead.authuser.mapper.UserMapper;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock private UserRepository repository;
    @Mock private UserMapper mapper;
    @InjectMocks private UserServiceImpl service;

    private UserModel createUser(UUID id) {
        return UserModel.builder()
            .userId(id)
            .username("test")
            .email("test@test.com")
            .password("pass")
            .fullName("Test")
            .userStatus(UserStatus.ACTIVE)
            .userType(UserType.USER)
            .creationDate(LocalDateTime.now())
            .lastUpdateDate(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("findAll should return users list")
    void findAll_Success() {
        var users = List.of(createUser(UUID.randomUUID()), createUser(UUID.randomUUID()));
        when(repository.findAll()).thenReturn(users);

        var result = service.findAll();

        assertThat(result).hasSize(2).isEqualTo(users);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("findById should return user when exists")
    void findById_Success() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        when(repository.findById(id)).thenReturn(Optional.of(user));

        var result = service.findById(id);

        assertThat(result).isEqualTo(user);
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("findById should throw NotFoundException when user not found")
    void findById_NotFound() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("User not found");
    }

    @Test
    @DisplayName("deleteById should delete user when exists")
    void deleteById_Success() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.deleteById(id);

        verify(repository).delete(user);
    }

    @Test
    @DisplayName("deleteById should throw NotFoundException when user not exists")
    void deleteById_NotFound() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteById(id))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("registerUser should create user with unique credentials")
    void registerUser_Success() {
        var dto = new UserRecordDto("user", "user@test.com", "pass123", null, null, "User Name", null, null);
        var user = createUser(UUID.randomUUID());

        when(repository.existsByUsername("user")).thenReturn(false);
        when(repository.existsByEmail("user@test.com")).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(user);
        when(repository.save(any())).thenReturn(user);

        var result = service.registerUser(dto);

        assertThat(result).isNotNull();
        assertThat(result.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getUserType()).isEqualTo(UserType.USER);
        verify(repository).save(any());
    }

    @Test
    @DisplayName("registerUser should reject duplicate username")
    void registerUser_DuplicateUsername() {
        var dto = new UserRecordDto("user", "user@test.com", "pass123", null, null, "User", null, null);
        when(repository.existsByUsername("user")).thenReturn(true);

        assertThatThrownBy(() -> service.registerUser(dto))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Username is already taken");
    }

    @Test
    @DisplayName("registerUser should reject duplicate email")
    void registerUser_DuplicateEmail() {
        var dto = new UserRecordDto("user", "user@test.com", "pass123", null, null, "User", null, null);
        when(repository.existsByUsername("user")).thenReturn(false);
        when(repository.existsByEmail("user@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registerUser(dto))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Email is already taken");
    }

    @Test
    @DisplayName("updateUser should update user fields")
    void updateUser_Success() {
        var user = createUser(UUID.randomUUID());
        var dto = new UserRecordDto(null, null, null, null, null, "New Name", "11999999999", null);
        when(repository.save(any())).thenReturn(user);

        var result = service.updateUser(dto, user);

        assertThat(result).isNotNull();
        verify(repository).save(user);
    }

    @Test
    @DisplayName("updatePassword should change password successfully")
    void updatePassword_Success() {
        var user = createUser(UUID.randomUUID());
        user.setPassword("oldpass123");
        var dto = new UserRecordDto(null, null, null, "oldpass123", "newpass456", null, null, null);

        service.updatePassword(dto, user);

        assertThat(user.getPassword()).isEqualTo("newpass456");
        verify(repository).save(user);
    }

    @Test
    @DisplayName("updatePassword should reject wrong old password")
    void updatePassword_WrongOldPassword() {
        var user = createUser(UUID.randomUUID());
        user.setPassword("actualpass");
        var dto = new UserRecordDto(null, null, null, "wrongpass", "newpass456", null, null, null);

        assertThatThrownBy(() -> service.updatePassword(dto, user))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Old password does not match.");
    }

    @Test
    @DisplayName("updateImage should update image URL successfully")
    void updateImage_Success() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        user.setImageUrl("old.jpg");
        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.updateImage(id, "new.jpg");

        assertThat(user.getImageUrl()).isEqualTo("new.jpg");
    }

    @Test
    @DisplayName("updateImage should throw NotFoundException when user not found")
    void updateImage_NotFound() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateImage(id, "image.jpg"))
            .isInstanceOf(NotFoundException.class);
    }
}

