package ru.rentplatform.userservice.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentplatform.userservice.api.dto.response.UserPublicResponse;
import ru.rentplatform.userservice.core.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserPublicController {

    private final UserService userService;

    @GetMapping("/{userId}/public")
    public UserPublicResponse getPublicUser(
            @PathVariable UUID userId
    ) {
        return userService.getPublicProfile(userId);
    }
}
