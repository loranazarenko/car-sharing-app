package carsharingapp.dto;

import carsharingapp.validation.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@PasswordMatch
@AllArgsConstructor
@Accessors(chain = true)
public class UserRegistrationRequestDto {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Length(min = 8, max = 20)
    private String password;
    @NotBlank
    @Length(min = 8, max = 20)
    private String repeatPassword;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
}
