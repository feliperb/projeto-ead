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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    //private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserModel> findAll(Specification<UserModel> spec, Pageable pageable) {
        logger.debug("findAll - Searching for users with specification");
        Page<UserModel> page = userRepository.findAll(spec, pageable);
        logger.debug("findAll - Found {} users in total", page.getTotalElements());
        return page;
    }

    @Override
    public UserModel findById(UUID userId) {
        logger.debug("findById - Searching for user with ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("findById - User not found with ID: {}", userId);
                    return new NotFoundException("User not found");
                });
    }

    @Override
    public void deleteById(UUID userId) {
        logger.info("deleteById - Deleting user with ID: {}", userId);
        UserModel user = findById(userId); // já lança NotFoundException
        userRepository.delete(user);
        logger.info("deleteById - User deleted successfully: {}", userId);
    }

    @Override
    public UserModel registerUser(UserRecordDto dto) {
        logger.info("registerUser - Registering new user with username: {}", dto.username());

        if (userRepository.existsByUsername(dto.username())) {
            logger.warn("registerUser - Username already taken: {}", dto.username());
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(dto.email())) {
            logger.warn("registerUser - Email already taken: {}", dto.email());
            throw new ConflictException("Email is already taken");
        }

        UserModel user = userMapper.toEntity(dto);

        user.setUserStatus(UserStatus.ACTIVE);
        user.setUserType(UserType.USER);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        user.setCreationDate(now);
        user.setLastUpdateDate(now);

        //user.setPassword(passwordEncoder.encode(dto.password()));
        user.setPassword(dto.password());

        UserModel savedUser = userRepository.save(user);
        logger.info("registerUser - User registered successfully: {} ({})", savedUser.getUserId(), savedUser.getUsername());
        return savedUser;
    }

    @Override
    public UserModel updateUser(UserRecordDto dto, UserModel user) {
        logger.info("updateUser - Updating user: {}", user.getUserId());
        if (dto.fullName() != null) {
            logger.debug("updateUser - Updating fullName from {} to {}", user.getFullName(), dto.fullName());
            user.setFullName(dto.fullName());
        }
        if (dto.phoneNumber() != null) {
            logger.debug("updateUser - Updating phoneNumber to {}", dto.phoneNumber());
            user.setPhoneNumber(dto.phoneNumber());
        }
        user.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        UserModel updatedUser = userRepository.save(user);
        logger.info("updateUser - User updated successfully: {}", user.getUserId());
        return updatedUser;
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
        logger.info("updatePassword - Updating password for user: {}", user.getUserId());
        try {
            PasswordValidator.validateNotBlank(dto.oldPassword(), dto.newPassword());
            PasswordValidator.validateDifferent(dto.oldPassword(), dto.newPassword());
            PasswordValidator.validateStrength(dto.newPassword());

            if (!dto.oldPassword().equals(user.getPassword())) {
                logger.warn("updatePassword - Old password does not match for user: {}", user.getUserId());
                throw new UnauthorizedException("Old password does not match.");
            }

            user.setPassword(dto.newPassword());
            user.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

            userRepository.save(user);
            logger.info("updatePassword - Password updated successfully for user: {}", user.getUserId());
        } catch (BusinessException e) {
            logger.warn("updatePassword - Password validation failed for user: {} - {}", user.getUserId(), e.getMessage());
            throw e;
        }
    }



    @Override
    @Transactional
    public void updateImage(UUID userId, String imageUrl) {
        logger.info("updateImage - Updating image for user: {} - imageUrl: {}", userId, imageUrl);

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("updateImage - User not found with ID: {}", userId);
                    return new NotFoundException("User not found");
                });

        if (imageUrl.equals(user.getImageUrl())) {
            logger.warn("updateImage - Image is already the same for user: {}", userId);
            throw new BusinessException("Image is already the same");
        }

        user.setImageUrl(imageUrl);
        user.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        logger.info("updateImage - Image updated successfully for user: {}", userId);
    }

}