package ru.rentplatform.userservice.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Pattern(
            regexp = "^[A-Za-zА-Яа-яЁё\\s\\-]+$",
            message = "Full name can contain only letters, spaces and hyphens"
    )
    private String fullName;

    @Email(message = "Email must be valid and contain @")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    private String bio;

    @URL(message = "Avatar URL must be a valid URL")
    @Size(max = 1000, message = "Avatar URL must be at most 1000 characters")
    private String avatarUrl;
}