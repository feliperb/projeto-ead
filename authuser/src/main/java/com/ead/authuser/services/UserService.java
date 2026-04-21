package com.ead.authuser.services;

import com.ead.authuser.dtos.UserRecordDto;
import com.ead.authuser.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserModel> findAll();
    UserModel findById(UUID userId);
    void deleteById(UUID userModel);
    UserModel registerUser(UserRecordDto userRecordDto);
    UserModel updateUser(UserRecordDto dto, UserModel user);
    void updatePassword(UserRecordDto dto, UserModel user);
    void updateImage(UUID userId, String image);
    Page<UserModel> findAll(Pageable pageable);
}
