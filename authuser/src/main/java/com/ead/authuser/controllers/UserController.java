package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserRecordDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specification.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(SpecificationTemplate.UserSpec spec, Pageable pageable) {
        Page<UserModel> page = userService.findAll(spec, pageable);
        page.forEach(user ->
                user.add(linkTo(methodOn(UserController.class)
                        .getUserById(user.getUserId()))
                        .withSelfRel())
        );
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserModel> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.findById(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteById(userId);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserModel> updateUser(@PathVariable UUID userId,
                                                @RequestBody @Validated(UserRecordDto.UserView.UserPut.class)
                                                @JsonView(UserRecordDto.UserView.UserPut.class)
                                                @Valid UserRecordDto dto) {
        UserModel user = userService.findById(userId);
        return ResponseEntity.ok(userService.updateUser(dto, user));
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable UUID userId,
                                               @RequestBody @Validated(UserRecordDto.UserView.PasswordPut.class)
                                               @JsonView(UserRecordDto.UserView.PasswordPut.class)
                                               @Valid UserRecordDto dto) {
        UserModel user = userService.findById(userId);
        userService.updatePassword(dto, user);
        return ResponseEntity.noContent().build(); //204
    }


    @PutMapping("/{userId}/image")
    public ResponseEntity<Void> updateImage(@PathVariable UUID userId,
                                            @RequestBody @Validated(UserRecordDto.UserView.ImagePut.class)
                                            @JsonView(UserRecordDto.UserView.ImagePut.class)
                                            @Valid UserRecordDto dto) {
        userService.updateImage(userId, dto.imageUrl());
        return ResponseEntity.noContent().build();
    }

}