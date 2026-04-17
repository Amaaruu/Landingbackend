package Landing.Backend.dto;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String name;
    private String lastname;
    private String email;
    private String password;
    private String role; // admin, user
    
}
