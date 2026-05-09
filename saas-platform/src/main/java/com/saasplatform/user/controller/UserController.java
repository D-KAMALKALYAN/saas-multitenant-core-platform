package com.saasplatform.user.controller;

import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.user.dto.UserRequest;
import com.saasplatform.user.dto.UserResponse;
import com.saasplatform.user.dto.UserUpdateRequest;
import com.saasplatform.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "User API",
        description = "APIs for managing users within a tenant."
)
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN') " +
                    "or hasAuthority('users:write')"
    )
    @Operation(
            summary = "Create user",
            description = "Creates a new user for the current tenant."
    )
    public ResponseEntity<StandardApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MEMBER', 'VIEWER') " +
                    "or hasAuthority('users:read')"
    )
    @Operation(
            summary = "Get all users",
            description = "Fetches all users for the current tenant."
    )
    public ResponseEntity<StandardApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                userService.getAllUsers(page, size)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MEMBER', 'VIEWER') " +
                    "or hasAuthority('users:read')"
    )
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a specific user by ID."
    )
    public ResponseEntity<StandardApiResponse<UserResponse>> getUserById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MEMBER') " +
                    "or hasAuthority('users:write')"
    )
    @Operation(
            summary = "Update user",
            description = "Updates user details."
    )
    public ResponseEntity<StandardApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {

        return ResponseEntity.ok(
                userService.updateUser(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('SUPER_ADMIN', 'ADMIN') " +
                    "or hasAuthority('users:delete')"
    )
    @Operation(
            summary = "Delete user",
            description = "Soft deletes a user."
    )
    public ResponseEntity<StandardApiResponse<Void>> deleteUser(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                userService.deleteUser(id)
        );
    }
}