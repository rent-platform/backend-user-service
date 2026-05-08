package ru.rentplatform.userservice.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rentplatform.userservice.api.dto.response.UserPublicResponse;
import ru.rentplatform.userservice.core.service.UserService;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getPublicUser_shouldReturnProfile() throws Exception {

        UUID userId = UUID.randomUUID();
        UserPublicResponse profile = UserPublicResponse.builder()
                .id(userId).nickname("public_user")
                .avatarUrl("https://example.com/avatar.jpg")
                .overallRating(4.5)
                .build();

        when(userService.getPublicProfile(userId)).thenReturn(profile);

        mockMvc.perform(get("/api/users/" + userId + "/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("public_user"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.overallRating").value(4.5));
    }
}