package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.UserDto;
import dev.olaxomi.backend.enums.Role;
import dev.olaxomi.backend.mapper.UserMapper;
import dev.olaxomi.backend.model.User;
import dev.olaxomi.backend.repository.UserRepository;
import dev.olaxomi.backend.request.UpdatePasswordRequest;
import dev.olaxomi.backend.request.UpdateUserRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, EmailService emailService, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return user;
    }

    public List<UserDto> allUsers() {
        List<User> users = userRepository.findAllOrderByCreatedAtDesc();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> filteredUsers = users.stream()
                .filter(user -> !user.getRole().equals(Role.MAIN_ADMIN)) // Exclude super admin by role
                .filter(user -> !user.getUsername().equals(currentUsername)) // Exclude current user
                .collect(Collectors.toList());

        return userMapper.toDtoList(filteredUsers);
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return userMapper.toDto(user);
    }

    public UserDto getUserByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return userMapper.toDto(user);
    }

    public UserDto updateUser(UpdateUserRequest request, Long userId){
        return  userRepository.findById(userId).map(existingUser ->{
            existingUser.setFullName(request.getFullName());
            existingUser.setPhone(request.getPhone());
            return userMapper.toDto(userRepository.save(existingUser));
        }).orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public UserDto updateOwnProfile(UpdateUserRequest request, String username) {
        return userRepository.findByEmail(username).map(existingUser -> {
                    existingUser.setFullName(request.getFullName());
                    existingUser.setPhone(request.getPhone());
                    return userMapper.toDto(userRepository.save(existingUser));
                }).orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }

    public UserDto updateOwnPassword(UpdatePasswordRequest request, String username){
        return  userRepository.findByEmail(username).map(existingUser ->{
            if (!passwordEncoder.matches(request.getOldPassword(), existingUser.getPassword())) {
                throw new RuntimeException("Old Password is incorrect. Please try again.");
            }
            if(!request.getNewPassword().equals(request.getConfirmPassword())){
                throw new RuntimeException("New Password and confirm password are not similar.");
            }
            existingUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
            return userMapper.toDto(userRepository.save(existingUser));
        }).orElseThrow(() -> new RuntimeException("User not found!"));
    }


    public UserDto updatePassword(UpdatePasswordRequest request, Long userId){
        return  userRepository.findById(userId).map(existingUser ->{
            existingUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
            return userMapper.toDto(userRepository.save(existingUser));
        }).orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public UserDto enableOrDisableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        user.setEnabled(!user.isEnabled());
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return; // Skip if empty
        }
        userRepository.deleteByIds(ids);
    }


}
