package dev.olaxomi.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.olaxomi.backend.dto.UserDto;
import dev.olaxomi.backend.model.User;
import dev.olaxomi.backend.model.UserPermission;
import dev.olaxomi.backend.request.LoginUserRequest;
import dev.olaxomi.backend.request.NewUserRequest;
import dev.olaxomi.backend.response.LoginResponse;
import dev.olaxomi.backend.response.MessageResponse;
import dev.olaxomi.backend.service.AuthenticationService;
import dev.olaxomi.backend.service.JwtService;
import dev.olaxomi.backend.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CONFLICT;

@RequestMapping("/v1/auth")
@RestController
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final PermissionService permissionService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, PermissionService permissionService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.permissionService = permissionService;
    }

    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody NewUserRequest registerUserDto) {
        try{
            UserDto registeredUser = authenticationService.register(registerUserDto);
            return ResponseEntity.ok(new MessageResponse("success", registeredUser));
        } catch (RuntimeException e) {
            return ResponseEntity.status(CONFLICT).body(new MessageResponse(e.getMessage(), null));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserRequest loginUserDto) {
        try {
            LoginResponse response = authenticationService.authenticateAndRespond(loginUserDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(CONFLICT).body(new LoginResponse(e.getMessage(), null, null, 0));
        }
    }

//    @PostMapping("/authenticate")
//    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserRequest loginUserDto){
//        try {
//            User authenticatedUser = authenticationService.authenticate(loginUserDto);
//            String jwtToken = jwtService.generateToken(authenticatedUser);
//            UserPermission permission = permissionService.getPermissions(authenticatedUser.getId());
//            LoginResponse loginResponse = new LoginResponse("success", jwtToken, permission.getPermissions(), jwtService.getExpirationTime());
//            return ResponseEntity.ok(loginResponse);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(CONFLICT).body(new LoginResponse(e.getMessage(),null,null, 0));
//        }
//
//    }
}
