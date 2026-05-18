package Landing.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RoleUpdateDTO {

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "^(admin|user)$", message = "El rol solo puede ser 'admin' o 'user'")
    private String role;
}