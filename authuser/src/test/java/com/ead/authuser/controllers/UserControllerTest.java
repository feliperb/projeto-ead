package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserRecordDto;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.exceptions.BusinessException;
import com.ead.authuser.exceptions.NotFoundException;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock private UserService userService;
    @InjectMocks private UserController controller;

    private UserModel createUser(UUID id) {
        return UserModel.builder()
                .userId(id)
                .username("testuser")
                .email("test@test.com")
                .password("MyP@ssw0rd")
                .fullName("Test User")
                .userStatus(UserStatus.ACTIVE)
                .userType(UserType.USER)
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .build();
    }

    // ============ GET /users ============

    @Test
    @DisplayName("getAllUsers should return list of users with status 200")
    void getAllUsers_ReturnsOk() {
        var users = List.of(createUser(UUID.randomUUID()), createUser(UUID.randomUUID()));
        when(userService.findAll()).thenReturn(users);

        var response = controller.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2).isEqualTo(users);
        verify(userService).findAll();
    }

    @Test
    @DisplayName("getAllUsers should return empty list when no users")
    void getAllUsers_EmptyList() {
        when(userService.findAll()).thenReturn(List.of());

        var response = controller.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // ============ GET /users/{userId} ============

    @Test
    @DisplayName("getUserById should return user with status 200")
    void getUserById_ReturnsOk() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        when(userService.findById(id)).thenReturn(user);

        var response = controller.getUserById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(user);
        verify(userService).findById(id);
    }

    @Test
    @DisplayName("getUserById should throw NotFoundException when user not found")
    void getUserById_NotFound() {
        var id = UUID.randomUUID();
        when(userService.findById(id)).thenThrow(new NotFoundException("User not found"));

        assertThatThrownBy(() -> controller.getUserById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    // ============ DELETE /users/{userId} ============

    @Test
    @DisplayName("deleteUser should return status 204 No Content")
    void deleteUser_ReturnsNoContent() {
        var id = UUID.randomUUID();

        var response = controller.deleteUser(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(userService).deleteById(id);
    }

    @Test
    @DisplayName("deleteUser should throw NotFoundException when user not found")
    void deleteUser_NotFound() {
        var id = UUID.randomUUID();
        doThrow(new NotFoundException("User not found")).when(userService).deleteById(id);

        assertThatThrownBy(() -> controller.deleteUser(id))
                .isInstanceOf(NotFoundException.class);
    }

    // ============ PUT /users/{userId} ============

    @Test
    @DisplayName("updateUser should update fullName and phoneNumber")
    void updateUser_Success() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        var dto = new UserRecordDto(null, null, null, null, null, "New Name", "11999999999", null);
        var updatedUser = createUser(id);
        updatedUser.setFullName("New Name");
        updatedUser.setPhoneNumber("11999999999");

        when(userService.findById(id)).thenReturn(user);
        when(userService.updateUser(dto, user)).thenReturn(updatedUser);

        var response = controller.updateUser(id, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFullName()).isEqualTo("New Name");
        assertThat(response.getBody().getPhoneNumber()).isEqualTo("11999999999");
        verify(userService).findById(id);
        verify(userService).updateUser(dto, user);
    }

    @Test
    @DisplayName("updateUser should throw NotFoundException when user not found")
    void updateUser_UserNotFound() {
        var id = UUID.randomUUID();
        var dto = new UserRecordDto(null, null, null, null, null, "New Name", null, null);
        when(userService.findById(id)).thenThrow(new NotFoundException("User not found"));

        assertThatThrownBy(() -> controller.updateUser(id, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("updateUser should only update provided fields")
    void updateUser_PartialUpdate() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        var dto = new UserRecordDto(null, null, null, null, null, "New Name", null, null);

        when(userService.findById(id)).thenReturn(user);
        when(userService.updateUser(dto, user)).thenReturn(user);

        var response = controller.updateUser(id, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userService).updateUser(dto, user);
    }

    // ============ PUT /users/{userId}/password ============

    @Test
    @DisplayName("updatePassword should return status 204 No Content")
    void updatePassword_Success() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        var dto = new UserRecordDto(null, null, null, "MyP@ssw0rd", "NewPass@123", null, null, null);

        when(userService.findById(id)).thenReturn(user);

        var response = controller.updatePassword(id, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(userService).findById(id);
        verify(userService).updatePassword(dto, user);
    }

    @Test
    @DisplayName("updatePassword should throw NotFoundException when user not found")
    void updatePassword_UserNotFound() {
        var id = UUID.randomUUID();
        var dto = new UserRecordDto(null, null, null, "oldpass", "newpass", null, null, null);
        when(userService.findById(id)).thenThrow(new NotFoundException("User not found"));

        assertThatThrownBy(() -> controller.updatePassword(id, dto))
                .isInstanceOf(NotFoundException.class);
    }

    // ============ PUT /users/{userId}/image ============

    @Test
    @DisplayName("updateImage should return status 204 No Content")
    void updateImage_Success() {
        var id = UUID.randomUUID();
        var dto = new UserRecordDto(null, null, null, null, null, null, null, "image.jpg");

        var response = controller.updateImage(id, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(userService).updateImage(id, "image.jpg");
    }

    @Test
    @DisplayName("updateImage should throw NotFoundException when user not found")
    void updateImage_UserNotFound() {
        var id = UUID.randomUUID();
        var dto = new UserRecordDto(null, null, null, null, null, null, null, "image.jpg");
        doThrow(new NotFoundException("User not found")).when(userService).updateImage(id, "image.jpg");

        assertThatThrownBy(() -> controller.updateImage(id, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("updateImage should throw BusinessException when image is same")
    void updateImage_SameImage() {
        var id = UUID.randomUUID();
        var dto = new UserRecordDto(null, null, null, null, null, null, null, "image.jpg");
        doThrow(new BusinessException("Image is already the same")).when(userService).updateImage(id, "image.jpg");

        assertThatThrownBy(() -> controller.updateImage(id, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Image is already the same");
    }

    // ============ RESPONSE VALIDATION ============

    @Test
    @DisplayName("All successful responses should contain user data")
    void responseBody_ContainsUserData() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        when(userService.findById(id)).thenReturn(user);

        var response = controller.getUserById(id);

        assertThat(response.getBody())
                .isNotNull()
                .hasFieldOrPropertyWithValue("userId", id)
                .hasFieldOrPropertyWithValue("username", "testuser")
                .hasFieldOrPropertyWithValue("email", "test@test.com");
    }
}

