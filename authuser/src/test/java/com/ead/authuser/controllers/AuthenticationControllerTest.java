package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserRecordDto;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController Tests")
class AuthenticationControllerTest {

    @Mock private UserService userService;
    @InjectMocks private AuthenticationController controller;

    private UserModel createUser() {
        return UserModel.builder()
            .userId(UUID.randomUUID())
            .username("testuser")
            .email("test@test.com")
            .password("pass123")
            .fullName("Test User")
            .userStatus(UserStatus.ACTIVE)
            .userType(UserType.USER)
            .creationDate(LocalDateTime.now())
            .lastUpdateDate(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("registerUser should call service and return created user")
    void registerUser_Success() {
        var dto = new UserRecordDto("testuser", "test@test.com", "pass123", null, null, "Test User", null, null);
        var user = createUser();
        when(userService.registerUser(any())).thenReturn(user);

        var response = controller.registerUser(dto);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull().isEqualTo(user);
    }

    @Test
    @DisplayName("registerUser should propagate service exceptions")
    void registerUser_ServiceException() {
        var dto = new UserRecordDto("testuser", "test@test.com", "pass123", null, null, "Test", null, null);
        when(userService.registerUser(any())).thenThrow(new RuntimeException("Test exception"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.registerUser(dto))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("registerUser response entity should have status CREATED")
    void registerUser_StatusCode() {
        var dto = new UserRecordDto("testuser", "test@test.com", "pass123", null, null, "Test", null, null);
        var user = createUser();
        when(userService.registerUser(any())).thenReturn(user);

        var response = controller.registerUser(dto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }
}

