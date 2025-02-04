package carsharingapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class UserUpdateInfoRequestDto {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
}
