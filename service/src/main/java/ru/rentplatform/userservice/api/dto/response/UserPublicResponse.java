package ru.rentplatform.userservice.api.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicResponse {

    private UUID id;

    private String nickname;

    private String avatarUrl;

    private Double rating;
}
