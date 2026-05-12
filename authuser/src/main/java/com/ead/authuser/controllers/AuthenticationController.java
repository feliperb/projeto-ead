package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserRecordDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

//@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    Logger logger = LogManager.getLogger(AuthenticationController.class);

    final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserModel> registerUser(@RequestBody
                                                      @Validated(UserRecordDto.UserView.RegistrationPost.class)
                                                      @JsonView(UserRecordDto.UserView.RegistrationPost.class)
                                                      @Valid
                                                      UserRecordDto userRecordDto){
        logger.info("POST registerUser - username: {}", userRecordDto.username());
        logger.debug("POST registerUser userDto received: {}", userRecordDto);
        UserModel user = userService.registerUser(userRecordDto);
        logger.info("POST registerUser - User registered successfully: {}", user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/logs")
    public String index() {
        logger.trace("TRACE - This is a trace message");
        logger.debug("DEBUG - This is a debug message");
        logger.info("INFO - This is an info message");
        logger.warn("WARN - This is a warning message");
        logger.error("ERROR - This is an error message");
        return "Logging Spring Boot...";
    }

}