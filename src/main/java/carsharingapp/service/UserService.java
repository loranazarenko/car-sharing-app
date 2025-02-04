package carsharingapp.service;

import carsharingapp.dto.UserRegistrationRequestDto;
import carsharingapp.dto.UserResponseDto;
import carsharingapp.dto.UserUpdateInfoRequestDto;
import carsharingapp.dto.UserUpdateRoleRequestDto;
import carsharingapp.model.User;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto);

    UserResponseDto updateUserRole(Long userId, UserUpdateRoleRequestDto requestDto);

    UserResponseDto getUserInfo();

    UserResponseDto updateUserInfo(
            UserUpdateInfoRequestDto requestDto
    );

    void deleteUser(Long userId);

    User getCurrentUser();
}
