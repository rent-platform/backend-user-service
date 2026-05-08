package ru.rentplatform.userservice.core.service.implement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.rentplatform.userservice.api.dto.response.UserResponse;
import ru.rentplatform.userservice.api.exception.AccessDeniedException;
import ru.rentplatform.userservice.core.dao.entity.User;
import ru.rentplatform.userservice.core.dao.repository.UserRepository;
import ru.rentplatform.userservice.core.mapper.UserMapper;
import ru.rentplatform.userservice.core.service.SessionService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getCurrentUser_shouldReturnUser_whenExists() {

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setNickname("testuser");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().id(userId).nickname("testuser").build());

        UserResponse result = userService.getCurrentUser(userId);

        assertNotNull(result);
        assertEquals("testuser", result.getNickname());
    }

    @Test
    void getCurrentUser_shouldThrow_whenNotFound() {

        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () ->
                userService.getCurrentUser(userId));
    }

    @Test
    void updateUserRole_shouldThrow_whenSelfRoleChange() {

        UUID userId = UUID.randomUUID();

        assertThrows(AccessDeniedException.class, () ->
                userService.updateUserRole(userId, "super_admin", userId, "admin"));
    }
}
