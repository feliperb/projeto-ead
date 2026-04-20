package com.ead.authuser.mapper;

import com.ead.authuser.dtos.UserRecordDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserMapper Tests")
class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
    }

    @Test
    @DisplayName("toEntity should map all fields from DTO to Entity")
    void toEntity_MapsAllFields() {
        var dto = new UserRecordDto(
                "testuser",
                "test@example.com",
                "MyP@ssw0rd",
                "oldpass",
                "newpass",
                "Test User",
                "11999999999",
                "image.jpg"
        );

        var entity = mapper.toEntity(dto);

        assertThat(entity)
                .isNotNull()
                .hasFieldOrPropertyWithValue("username", "testuser")
                .hasFieldOrPropertyWithValue("email", "test@example.com")
                .hasFieldOrPropertyWithValue("password", "MyP@ssw0rd")
                .hasFieldOrPropertyWithValue("fullName", "Test User")
                .hasFieldOrPropertyWithValue("phoneNumber", "11999999999")
                .hasFieldOrPropertyWithValue("imageUrl", "image.jpg");
    }

    @Test
    @DisplayName("toEntity should map username correctly")
    void toEntity_MapsUsername() {
        var dto = new UserRecordDto("myusername", "test@test.com", "pass", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getUsername()).isEqualTo("myusername");
    }

    @Test
    @DisplayName("toEntity should map email correctly")
    void toEntity_MapsEmail() {
        var dto = new UserRecordDto("user", "email@test.com", "pass", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getEmail()).isEqualTo("email@test.com");
    }

    @Test
    @DisplayName("toEntity should map password correctly")
    void toEntity_MapsPassword() {
        var dto = new UserRecordDto("user", "test@test.com", "SecurePass123", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getPassword()).isEqualTo("SecurePass123");
    }

    @Test
    @DisplayName("toEntity should map fullName correctly")
    void toEntity_MapsFullName() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, "João Silva", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getFullName()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("toEntity should map phoneNumber correctly")
    void toEntity_MapsPhoneNumber() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, "Name", "11987654321", null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getPhoneNumber()).isEqualTo("11987654321");
    }

    @Test
    @DisplayName("toEntity should map imageUrl correctly")
    void toEntity_MapsImageUrl() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, "Name", null, "http://example.com/image.jpg");

        var entity = mapper.toEntity(dto);

        assertThat(entity.getImageUrl()).isEqualTo("http://example.com/image.jpg");
    }

    @Test
    @DisplayName("toEntity should handle null username")
    void toEntity_NullUsername() {
        var dto = new UserRecordDto(null, "test@test.com", "pass", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getUsername()).isNull();
    }

    @Test
    @DisplayName("toEntity should handle null email")
    void toEntity_NullEmail() {
        var dto = new UserRecordDto("user", null, "pass", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getEmail()).isNull();
    }

    @Test
    @DisplayName("toEntity should handle null password")
    void toEntity_NullPassword() {
        var dto = new UserRecordDto("user", "test@test.com", null, null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getPassword()).isNull();
    }

    @Test
    @DisplayName("toEntity should handle null fullName")
    void toEntity_NullFullName() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, null, null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getFullName()).isNull();
    }

    @Test
    @DisplayName("toEntity should handle null phoneNumber")
    void toEntity_NullPhoneNumber() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("toEntity should handle null imageUrl")
    void toEntity_NullImageUrl() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getImageUrl()).isNull();
    }

    @Test
    @DisplayName("toEntity should not set userId (auto-generated)")
    void toEntity_NotSetsUserId() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getUserId()).isNull();
    }

    @Test
    @DisplayName("toEntity should not set userStatus (set by service)")
    void toEntity_NotSetsUserStatus() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getUserStatus()).isNull();
    }

    @Test
    @DisplayName("toEntity should not set userType (set by service)")
    void toEntity_NotSetsUserType() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getUserType()).isNull();
    }

    @Test
    @DisplayName("toEntity should not set creationDate (set by service)")
    void toEntity_NotSetsCreationDate() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getCreationDate()).isNull();
    }

    @Test
    @DisplayName("toEntity should not set lastUpdateDate (set by service)")
    void toEntity_NotSetsLastUpdateDate() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, "Name", null, null);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getLastUpdateDate()).isNull();
    }

    @Test
    @DisplayName("toEntity should return new instance for each call")
    void toEntity_ReturnsNewInstance() {
        var dto = new UserRecordDto("user", "test@test.com", "pass", null, null, "Name", null, null);

        var entity1 = mapper.toEntity(dto);
        var entity2 = mapper.toEntity(dto);

        assertThat(entity1)
                .isNotSameAs(entity2)
                .isEqualTo(entity2);
    }
}

