package ru.rentplatform.userservice.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.userservice.api.dto.request.UpdateProfileRequest;
import ru.rentplatform.userservice.api.dto.response.UserResponse;
import ru.rentplatform.userservice.core.service.UserService;

import java.util.UUID;

import static ru.rentplatform.userservice.api.ApiPaths.USERS;

@RestController
@RequestMapping(USERS)
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public UserResponse getById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userService.getCurrentUser(userId);
    }

    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt,
                                 @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userService.updateCurrentUser(userId, request);
    }


    @GetMapping("/test")
    @SecurityRequirement(name = "")
    public String test() {
        return "user-service route ok";
    }
}
