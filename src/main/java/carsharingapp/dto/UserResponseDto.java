package carsharingapp.dto;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class UserResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<Long> roleIds = new HashSet<>();
}
