package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserRecordDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specification.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    Logger logger = LogManager.getLogger(UserController.class);
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(SpecificationTemplate.UserSpec spec, Pageable pageable) {
        logger.info("GET getAllUsers - page: {}, pageSize: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserModel> page = userService.findAll(spec, pageable);
        logger.debug("Found {} users", page.getTotalElements());
        page.forEach(user ->
                user.add(linkTo(methodOn(UserController.class)
                        .getUserById(user.getUserId()))
                        .withSelfRel())
        );
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserModel> getUserById(@PathVariable UUID userId) {
        logger.info("GET getUserById - userId: {}", userId);
        UserModel user = userService.findById(userId);
        logger.debug("User found: {}", user.getUserId());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        logger.info("DELETE deleteUser - userId: {}", userId);
        userService.deleteById(userId);
        logger.debug("User deleted successfully: {}", userId);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserModel> updateUser(@PathVariable UUID userId,
                                                @RequestBody @Validated(UserRecordDto.UserView.UserPut.class)
                                                @JsonView(UserRecordDto.UserView.UserPut.class)
                                                @Valid UserRecordDto dto) {
        logger.info("PUT updateUser - userId: {} - fullName: {}", userId, dto.fullName());
        UserModel user = userService.findById(userId);
        UserModel updatedUser = userService.updateUser(dto, user);
        logger.debug("User updated successfully: {}", userId);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable UUID userId,
                                               @RequestBody @Validated(UserRecordDto.UserView.PasswordPut.class)
                                               @JsonView(UserRecordDto.UserView.PasswordPut.class)
                                               @Valid UserRecordDto dto) {
        logger.info("PUT updatePassword - userId: {}", userId);
        UserModel user = userService.findById(userId);
        userService.updatePassword(dto, user);
        logger.debug("Password updated successfully for user: {}", userId);
        return ResponseEntity.noContent().build(); //204
    }


    @PutMapping("/{userId}/image")
    public ResponseEntity<Void> updateImage(@PathVariable UUID userId,
                                            @RequestBody @Validated(UserRecordDto.UserView.ImagePut.class)
                                            @JsonView(UserRecordDto.UserView.ImagePut.class)
                                            @Valid UserRecordDto dto) {
        logger.info("PUT updateImage - userId: {} - imageUrl: {}", userId, dto.imageUrl());
        userService.updateImage(userId, dto.imageUrl());
        logger.debug("Image updated successfully for user: {}", userId);
        return ResponseEntity.noContent().build();
    }

}