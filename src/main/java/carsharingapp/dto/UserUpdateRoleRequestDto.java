package carsharingapp.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class UserUpdateRoleRequestDto {
    @NotEmpty
    private Set<Long> roleIds = new HashSet<>();
}
