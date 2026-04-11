package com.saasplatform.user.controller;

import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.tenant.service.TenantService;
import com.saasplatform.user.dto.UserRequest;
import com.saasplatform.user.dto.UserResponse;
import com.saasplatform.user.dto.UserUpdateRequest;
import com.saasplatform.user.entity.User;
import com.saasplatform.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "User API",
        description = "APIs for managing users within a tenant. All endpoints require a valid X-Tenant-ID header."
)
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    @Operation(
            summary = "Create user",
            description = "Creates a new user for the current tenant. Email must be unique within the tenant."
    )
    public ResponseEntity<StandardApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRequest request){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Fetches a paginated list of users for the current tenant. Supports page and size parameters."
    )
    public ResponseEntity<StandardApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllUsers(page , size));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a specific user by ID within the current tenant."
    )
    public ResponseEntity<StandardApiResponse<UserResponse>> getUserById(
            @PathVariable UUID id){

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete user",
            description = "Soft deletes a user by marking them as inactive and setting deletedAt timestamp."
    )
    public ResponseEntity<StandardApiResponse<Void>> deleteUser(
            @PathVariable UUID id){

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.deleteUser(id));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update user",
            description = "Updates user details such as name, email, or role for the current tenant."
    )
    public ResponseEntity<StandardApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request){

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateUser(id , request));
    }
}