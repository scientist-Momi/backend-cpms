package dev.olaxomi.backend.controller;

import dev.olaxomi.backend.dto.UserDto;
import dev.olaxomi.backend.model.UserPermission;
import dev.olaxomi.backend.request.UpdatePasswordRequest;
import dev.olaxomi.backend.request.UpdatePermissionRequest;
import dev.olaxomi.backend.request.UpdateUserRequest;
import dev.olaxomi.backend.response.MessageResponse;
import dev.olaxomi.backend.service.PermissionService;
import dev.olaxomi.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequestMapping("/v1/user")
@RestController
public class UserController {
    private final UserService userService;
    private final PermissionService permissionService;
    private final UserDetailsService userDetailsService;

    public UserController(UserService userService, PermissionService permissionService, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.permissionService = permissionService;
        this.userDetailsService = userDetailsService;
    }

    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping("/all")
    public ResponseEntity<MessageResponse> all(){
        List<UserDto> users = userService.allUsers();
        return ResponseEntity.ok(new MessageResponse("success", users));
    }

    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/{userId}/update")
    public ResponseEntity<MessageResponse> updateUser(@RequestBody UpdateUserRequest updateRequest, @PathVariable Long userId){
        try{
            UserDto user = userService.updateUser(updateRequest, userId);
            return ResponseEntity.ok(new MessageResponse("success", user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(CONFLICT).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("isAuthenticated()") // or use a specific authority if you have one
    @PutMapping("/me/update")
    public ResponseEntity<MessageResponse> updateOwnProfile(@RequestBody UpdateUserRequest updateRequest) {
        try {
            // Get the current authenticated user's ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserDto user = userService.updateOwnProfile(updateRequest, username);
            return ResponseEntity.ok(new MessageResponse("success", user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/password/update")
    public ResponseEntity<MessageResponse> updateOwnPassword(@RequestBody UpdatePasswordRequest updateRequest){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserDto user = userService.updateOwnPassword(updateRequest, username);
            return ResponseEntity.ok(new MessageResponse("success", user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }


    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/{userId}/password/update")
    public ResponseEntity<MessageResponse> updateUserPassword(@RequestBody UpdatePasswordRequest updateRequest, @PathVariable Long userId){
        try{
            UserDto user = userService.updatePassword(updateRequest, userId);
            return ResponseEntity.ok(new MessageResponse("success", user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/{userId}/enable")
    public ResponseEntity<MessageResponse> enableUser(@PathVariable Long userId){
        try{
            UserDto user = userService.enableOrDisableUser(userId);
            return ResponseEntity.ok(new MessageResponse("success", user));
        }catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<MessageResponse> getUser(@PathVariable Long userId){
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(new MessageResponse("success", user));
    }

    @GetMapping("/me")
    public ResponseEntity<MessageResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDto user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("success", user));
    }

    @GetMapping("/me/permissions")
    public ResponseEntity<MessageResponse> getCurrentUserPermissions(@AuthenticationPrincipal UserDetails userDetails) {
        UserPermission permissions = permissionService.getPermissionsByEmail(userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("success", permissions.getPermissions()));
    }

    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/{userId}/permission/update")
    public ResponseEntity<MessageResponse> updatePermission(@PathVariable Long userId, @RequestBody UpdatePermissionRequest request){
        try{
            UserPermission updatedPermission = permissionService.updatePermission(userId, request.getPermissions());
            return ResponseEntity.ok(new MessageResponse("success", updatedPermission.getPermissions()));
        }catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @GetMapping("/{userId}/permissions")
    public ResponseEntity<MessageResponse> userPermission(@PathVariable Long userId){
        try{
            UserPermission permissions = permissionService.getPermissions(userId);
            return ResponseEntity.ok(new MessageResponse("success", permissions.getPermissions()));
        }catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping("/delete")
    public ResponseEntity<MessageResponse> deleteUsers(@RequestBody List<Long> ids) {
        userService.deleteUsersByIds(ids);
        return ResponseEntity.ok(new MessageResponse("success", null));
    }
}
