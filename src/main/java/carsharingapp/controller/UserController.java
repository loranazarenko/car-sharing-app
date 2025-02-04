package carsharingapp.controller;

import carsharingapp.dto.UserResponseDto;
import carsharingapp.dto.UserUpdateInfoRequestDto;
import carsharingapp.dto.UserUpdateRoleRequestDto;
import carsharingapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User management", description = "Endpoints for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Change user's roles", description = "Change user's roles")
    public UserResponseDto updateUserRole(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateRoleRequestDto requestDto
    ) {
        return userService.updateUserRole(userId, requestDto);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(
            summary = "Get the user's profile info",
            description = "Get the user's profile info"
    )
    public UserResponseDto getUserInfo() {
        return userService.getUserInfo();
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(
            summary = "Update the user's profile info",
            description = "Update the user's profile info"
    )
    public UserResponseDto updateUserInfo(
            @RequestBody UserUpdateInfoRequestDto requestDto
    ) {
        return userService.updateUserInfo(requestDto);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete the user", description = "Delete the user")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}
