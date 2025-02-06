package carsharingapp.controller;

import carsharingapp.dto.UserLoginRequestDto;
import carsharingapp.dto.UserLoginResponseDto;
import carsharingapp.dto.UserRegistrationRequestDto;
import carsharingapp.dto.UserResponseDto;
import carsharingapp.exception.RegistrationException;
import carsharingapp.security.AuthenticationService;
import carsharingapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication management", description = "Endpoints for managing authentication")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "Registration a new user",
            description = "Registration a new user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/registration")
    public UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        return userService.register(requestDto);
    }

    @Operation(summary = "Login the user",
            description = "Login the user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/login")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto request) {
        return authenticationService.authenticate(request);
    }
}

