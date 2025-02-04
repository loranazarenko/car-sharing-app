package carsharingapp.service.impl;

import carsharingapp.dto.UserRegistrationRequestDto;
import carsharingapp.dto.UserResponseDto;
import carsharingapp.dto.UserUpdateInfoRequestDto;
import carsharingapp.dto.UserUpdateRoleRequestDto;
import carsharingapp.exception.RegistrationException;
import carsharingapp.mapper.UserMapper;
import carsharingapp.model.Role;
import carsharingapp.model.User;
import carsharingapp.repository.RoleRepository;
import carsharingapp.repository.UserRepository;
import carsharingapp.security.JwtUtil;
import carsharingapp.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDetailsService userDetailsService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        checkUserByEmailExists(requestDto);
        User user = userMapper.toEntity(requestDto);

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        Role role = roleRepository.findByRoleName(Role.RoleName.ROLE_CUSTOMER);
        Set<Role> roles = user.getRoles();
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserRole(Long userId, UserUpdateRoleRequestDto requestDto) {
        User user = findUserById(userId);
        Set<Role> userRoles = new HashSet<>(roleRepository.findAllById(requestDto.getRoleIds()));
        user.setRoles(userRoles);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getUserInfo() {
        User user = getCurrentUser();
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserInfo(
            UserUpdateInfoRequestDto requestDto
    ) {
        User user = getCurrentUser();
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public void updateTelegramChatId(Long chatId) {
        User currentUser = getCurrentUser();
        currentUser.setTelegramChatId(chatId);
        userRepository.save(currentUser);
    }

    @Override
    public User getCurrentUser() {
        String token = jwtUtil.getToken();
        System.out.println("Checking token " + token);
        String username = jwtUtil.getUsername(token);
        System.out.println("Checking username " + username);
        return (User) userDetailsService.loadUserByUsername(username);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with this ID: " + userId)
        );
    }

    private void checkUserByEmailExists(UserRegistrationRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("User with email " + requestDto.getEmail()
                    + " already exists");
        }
    }
}

