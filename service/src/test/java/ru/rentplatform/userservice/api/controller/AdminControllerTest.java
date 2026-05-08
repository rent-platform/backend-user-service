package ru.rentplatform.userservice.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rentplatform.userservice.api.dto.response.UserResponse;
import ru.rentplatform.userservice.config.JwtBeansConfig;
import ru.rentplatform.userservice.config.RsaKeyConfig;
import ru.rentplatform.userservice.config.SecurityConfig;
import ru.rentplatform.userservice.core.service.UserService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import({SecurityConfig.class, JwtBeansConfig.class, RsaKeyConfig.class})
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void updateUserRole_shouldReturnUpdatedUser_whenSuperAdmin() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        UserResponse user = UserResponse.builder()
                .id(targetUserId).nickname("moderator_user")
                .role("moderator").isActive(true).build();

        when(userService.updateUserRole(any(), eq("super_admin"), any(), eq("moderator")))
                .thenReturn(user);

        mockMvc.perform(put("/api/admin/users/" + targetUserId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"moderator\"}")
                        .with(jwt().jwt(j -> j
                                        .claim("sub", "9fafca75-3c3b-4fb2-9b83-ee3cfccc6905")
                                        .claim("role", "super_admin"))
                                .authorities(new SimpleGrantedAuthority("ROLE_super_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("moderator"));
    }

    @Test
    void updateUserRole_shouldReturnUpdatedUser_whenAdmin() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        UserResponse user = UserResponse.builder()
                .id(targetUserId).nickname("new_moderator")
                .role("moderator").isActive(true).build();

        when(userService.updateUserRole(any(), eq("admin"), any(), eq("moderator")))
                .thenReturn(user);

        mockMvc.perform(put("/api/admin/users/" + targetUserId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"moderator\"}")
                        .with(jwt().jwt(j -> j
                                        .claim("sub", "3227ee7b-775f-4743-8781-5563f352f9a7")
                                        .claim("role", "admin"))
                                .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("moderator"));
    }

    @Test
    void updateUserRole_shouldReturnForbidden_whenRegularUser() throws Exception {
        UUID targetUserId = UUID.randomUUID();

        mockMvc.perform(put("/api/admin/users/" + targetUserId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"moderator\"}")
                        .with(jwt().jwt(j -> j
                                        .claim("sub", "3227ee7b-775f-4743-8781-5563f352f9a7")
                                        .claim("role", "user"))
                                .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }
}
