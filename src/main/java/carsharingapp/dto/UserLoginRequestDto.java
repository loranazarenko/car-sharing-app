package carsharingapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Setter
@Getter
@Accessors(chain = true)
public class UserLoginRequestDto {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
}
