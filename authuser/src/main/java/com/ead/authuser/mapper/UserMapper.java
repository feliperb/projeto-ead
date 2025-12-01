package com.ead.authuser.mapper;

import com.ead.authuser.dtos.UserRecordDto;
import com.ead.authuser.models.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserModel toEntity(UserRecordDto dto) {
        return UserModel.builder()
                .username(dto.username())
                .email(dto.email())
                .password(dto.password())
                .fullName(dto.fullName())
                .phoneNumber(dto.phoneNumber())
                .imageUrl(dto.imageUrl())
                .build();
    }
}
