package com.ead.authuser.services.impl;

import com.ead.authuser.dtos.UserRecordDto;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.exceptions.BusinessException;
import com.ead.authuser.exceptions.ConflictException;
import com.ead.authuser.exceptions.NotFoundException;
import com.ead.authuser.exceptions.UnauthorizedException;
import com.ead.authuser.mapper.UserMapper;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.repositories.UserRepository;
import com.ead.authuser.services.UserService;
import com.ead.authuser.validators.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    //private final PasswordEncoder passwordEncoder;

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
        if (userRepository.existsByUsername(dto.username())) throw new ConflictException("Username is already taken");
        if (userRepository.existsByEmail(dto.email())) throw new ConflictException("Email is already taken");

        UserModel user = userMapper.toEntity(dto);

        user.setUserStatus(UserStatus.ACTIVE);
        user.setUserType(UserType.USER);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        user.setCreationDate(now);
        user.setLastUpdateDate(now);

        //user.setPassword(passwordEncoder.encode(dto.password()));
        user.setPassword(dto.password());

        return userRepository.save(user);
    }

    @Override
    public UserModel updateUser(UserRecordDto dto, UserModel user) {
        if (dto.fullName() != null) user.setFullName(dto.fullName());
        if (dto.phoneNumber() != null) user.setPhoneNumber(dto.phoneNumber());
        user.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        return userRepository.save(user);
    }

//    @Override
//    public UserModel updatePassword(UserRecordDto dto, UserModel user) {
//        if (!passwordEncoder.matches(dto.oldPassword(), user.getPassword())) {
//            throw new UnauthorizedException("Old password does not match");
//        }
//        if (passwordEncoder.matches(dto.newPassword(), user.getPassword())) {
//            throw new ConflictException("New password must be different from the old password.");
//        }
//        user.setPassword(passwordEncoder.encode(dto.newPassword()));
//        user.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
//        return userRepository.save(user);
//    }

    @Override
    public void updatePassword(UserRecordDto dto, UserModel user) {
        PasswordValidator.validateNotBlank(dto.oldPassword(), dto.newPassword());
        PasswordValidator.validateDifferent(dto.oldPassword(), dto.newPassword());
        PasswordValidator.validateStrength(dto.newPassword());

        if (!dto.oldPassword().equals(user.getPassword())) throw new UnauthorizedException("Old password does not match.");

        user.setPassword(dto.newPassword());
        user.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        userRepository.save(user);
    }



    @Override
    @Transactional
    public void updateImage(UUID userId, String imageUrl) {

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (imageUrl.equals(user.getImageUrl())) {
            throw new BusinessException("Image is already the same");
        }

        user.setImageUrl(imageUrl);
        user.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
    }

    @Override
    public Page<UserModel> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

}