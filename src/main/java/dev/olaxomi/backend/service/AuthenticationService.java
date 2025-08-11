package dev.olaxomi.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.olaxomi.backend.dto.UserDto;
import dev.olaxomi.backend.enums.ActionType;
import dev.olaxomi.backend.enums.Permission;
import dev.olaxomi.backend.enums.Role;
import dev.olaxomi.backend.enums.TargetType;
import dev.olaxomi.backend.mapper.UserMapper;
import dev.olaxomi.backend.model.UserPermission;
import dev.olaxomi.backend.request.LoginUserRequest;
import dev.olaxomi.backend.request.NewUserRequest;
//import dev.olaxomi.backend.requ.ResendDto;
//import dev.olaxomi.backend.dto.VerifyUserDto;
import dev.olaxomi.backend.model.User;
import dev.olaxomi.backend.repository.UserRepository;
//import dev.olaxomi.backend.response.MessageResponse;
import dev.olaxomi.backend.response.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PermissionService permissionService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserMapper userMapper;
    private final AdminActivityService activityService;
//    private final EmailService emailService;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder, PermissionService permissionService, JwtService jwtService, UserDetailsService userDetailsService, UserMapper userMapper, AdminActivityService activityService
//            EmailService emailService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
//        this.emailService = emailService;
        this.permissionService = permissionService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userMapper = userMapper;
        this.activityService = activityService;
    }

    public UserDto register(NewUserRequest input) throws JsonProcessingException {
        if(userRepository.existsByEmail(input.getEmail())){
            throw new RuntimeException("Email already exists.");
        }
        User user = new User();
        user.setFullName(input.getFullName());
        user.setEmail(input.getEmail());
        user.setPhone(input.getPhone());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setRole(Role.MINOR_ADMIN);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        Set<Permission> defaultPermissions = Set.of(Permission.VIEW_CUSTOMER, Permission.VIEW_PRODUCT, Permission.VIEW_TRANSACTION);
//        Set<Permission> defaultPermissions = Set.of(Permission.values());
        System.out.println(new ObjectMapper().writeValueAsString(defaultPermissions)); // Debugging
        permissionService.updatePermission(savedUser.getId(), defaultPermissions);

        String logDetails = String.format(
                "Created new user with user ID %s",
                savedUser.getId()
        );

        activityService.logActivity(
                ActionType.CREATE_USER,
                TargetType.USER,
                String.valueOf(savedUser.getId()),
                logDetails
        );

        return userMapper.toDto(user);
    }

    public LoginResponse authenticateAndRespond(LoginUserRequest input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account has been disabled. Please contact admin.");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getEmail(),
                            input.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid credentials");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(input.getEmail());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("permission", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        System.out.println(extraClaims);
        String jwtToken = jwtService.generateToken(extraClaims, userDetails);
        UserPermission permission = permissionService.getPermissions(user.getId());

        String logDetails = String.format(
                "User with ID %s logged in successfully",
                user.getId()
        );

        activityService.logActivity(
                user,
                ActionType.SIGN_IN_USER,
                TargetType.USER,
                user.getId().toString(),
                logDetails
        );

        return new LoginResponse("success", jwtToken, permission.getPermissions(), jwtService.getExpirationTime());
    }

//    public void verifyUser(VerifyUserDto input) {
//        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
//        if (optionalUser.isPresent()) {
//            User user = optionalUser.get();
//            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
//                throw new RuntimeException("Verification code has expired");
//            }
//            if (user.getVerificationCode().equals(input.getVerificationCode())) {
//                user.setEnabled(true);
//                user.setVerificationCode(null);
//                user.setVerificationCodeExpiresAt(null);
//                userRepository.save(user);
//            } else {
//                throw new RuntimeException("Invalid verification code");
//            }
//        } else {
//            throw new RuntimeException("User not found");
//        }
//    }
//    @Transactional
//    public void verifyUser(VerifyUserDto input) {
//        User user = userRepository.findByEmail(input.getEmail())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
//            throw new RuntimeException("Verification code has expired");
//        }
//
//        if (!user.getVerificationCode().equals(input.getVerificationCode())) {
//            throw new RuntimeException("Invalid verification code"+ input.getVerificationCode());
//        }
//
//        // Enable user and clear verification details
//        user.setEnabled(true);
//        user.setVerificationCode(null);
//        user.setVerificationCodeExpiresAt(null);
//
//        userRepository.save(user);
//    }
//
//    @Transactional
//    public void resendVerificationCode(ResendDto input) {
//        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
//        if (optionalUser.isPresent()) {
//            User user = optionalUser.get();
//            if (user.isEnabled()) {
//                throw new RuntimeException("Account is already verified");
//            }
//            user.setVerificationCode(generateVerificationCode());
//            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
//            sendVerificationEmail(user);
//            userRepository.save(user);
//        } else {
//            throw new RuntimeException("User not found");
//        }
//    }
//
//    private void sendVerificationEmail(User user) { //TODO: Update with company logo
//        String subject = "Account Verification";
//        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
//        String htmlMessage = "<html>"
//                + "<body style=\"font-family: Arial, sans-serif;\">"
//                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
//                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
//                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
//                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
//                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
//                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
//                + "</div>"
//                + "</div>"
//                + "</body>"
//                + "</html>";
//
//        try {
//            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
//        } catch (MessagingException e) {
//            // Handle email sending exception
//            e.printStackTrace();
//        }
//    }
//    private String generateVerificationCode() {
//        Random random = new Random();
//        int code = random.nextInt(900000) + 100000;
//        return String.valueOf(code);
//    }
}
