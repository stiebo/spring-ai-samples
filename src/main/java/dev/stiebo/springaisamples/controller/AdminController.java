package dev.stiebo.springaisamples.controller;

import dev.stiebo.springaisamples.dto.PasswordResetOutDto;
import dev.stiebo.springaisamples.dto.RoleChangeDto;
import dev.stiebo.springaisamples.dto.UserInDto;
import dev.stiebo.springaisamples.exception.ErrorResponse;
import dev.stiebo.springaisamples.exception.ValidationErrorResponse;
import dev.stiebo.springaisamples.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminController {

    private final AdminUserService adminUserService;

    @Autowired
    public AdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(
            summary = "Add user credentials",
            description = "Creates a new user. " +
                    "The very first call (when no users exist) requires no authentication and creates an admin. " +
                    "All subsequent calls require an authenticated admin and create regular users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed for request body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required (after first user exists)"),
            @ApiResponse(responseCode = "403", description = "Admin role required"),
            @ApiResponse(responseCode = "409", description = "Username already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public void addUser(@RequestBody @Valid UserInDto request) {
        adminUserService.addUser(request.username(), request.password());
    }

    @Operation(
            summary = "Delete a user",
            description = "Deletes an existing user. Requires admin authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/users/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String username) {
        adminUserService.deleteUser(username);
    }

    @Operation(
            summary = "Change user role",
            description = "Promotes or demotes a user by changing their role to ADMIN or USER. Requires admin authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role changed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed for request body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/users/{username}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeUserRole(@PathVariable String username, @RequestBody @Valid RoleChangeDto request) {
        adminUserService.changeUserRole(username, request.role());
    }

    @Operation(
            summary = "Reset user password",
            description = "Generates and sets a new random password for an existing user. Returns the new password. Requires admin authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PasswordResetOutDto.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin role required",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/users/{username}/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public PasswordResetOutDto resetUserPassword(@PathVariable String username) {
        String newPassword = adminUserService.resetUserPassword(username);
        return new PasswordResetOutDto(
                username,
                newPassword,
                "Password reset successfully. Please share this password securely with the user."
        );
    }
}
