package com.j20_pos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String email;

    @NotBlank(message = "Role is required")
    private String role;

    @Builder.Default
    private Boolean active = true;
}
