package ru.rentplatform.userservice.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rentplatform.userservice.api.dto.response.MessageResponse;
import ru.rentplatform.userservice.api.dto.response.UserResponse;
import ru.rentplatform.userservice.core.service.UserService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getMe_shouldReturnCurrentUser() throws Exception {

        UserResponse user = UserResponse.builder()
                .id(UUID.randomUUID()).nickname("testuser").role("user").isActive(true).build();

        when(userService.getCurrentUser(any())).thenReturn(user);

        mockMvc.perform(get("/api/users/me")
                        .with(jwt().jwt(j -> j.claim("sub", "3227ee7b-775f-4743-8781-5563f352f9a7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("testuser"));
    }

    @Test
    void updateMe_shouldReturnUpdatedUser() throws Exception {

        UserResponse user = UserResponse.builder()
                .id(UUID.randomUUID()).nickname("updated").role("user").isActive(true).build();

        when(userService.updateCurrentUser(any(), any())).thenReturn(user);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Updated Name\"}")
                        .with(jwt().jwt(j -> j.claim("sub", "3227ee7b-775f-4743-8781-5563f352f9a7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("updated"));
    }

    @Test
    void changePassword_shouldReturnMessage() throws Exception {

        MessageResponse message = MessageResponse.builder()
                .message("Password changed successfully").build();

        when(userService.changePassword(any(), any())).thenReturn(message);

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "currentPassword": "old",
                            "newPassword": "newpass123",
                            "confirmNewPassword": "newpass123"
                        }
                        """)
                        .with(jwt().jwt(j -> j.claim("sub", "3227ee7b-775f-4743-8781-5563f352f9a7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }
}
