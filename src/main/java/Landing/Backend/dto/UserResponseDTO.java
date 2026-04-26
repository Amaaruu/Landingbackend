package Landing.Backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Integer userId;
    private String uuid;
    private String name;
    private String lastName;
    private String email;
    private String role;
    private LocalDateTime registeredAt;
}