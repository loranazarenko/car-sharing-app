package carsharingapp.controller;

import carsharingapp.dto.RentalRequestDto;
import carsharingapp.dto.RentalResponseDto;
import carsharingapp.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
@Tag(name = "Rental management", description = "Endpoints for managing rentals")
@SecurityRequirement(name = "bearerAuth")
public class RentalController {
    private final RentalService rentalService;

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Add a new rental", description = "Add a new rental")
    @PostMapping()
    public RentalResponseDto addRental(
            @RequestBody @Valid RentalRequestDto requestDto
    ) {
        return rentalService.save(requestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping
    @Operation(summary = "Get rentals by user ID and whether the rental is still active or not",
            description = "Get rentals by user ID and whether the rental is still active or not")
    public List<RentalResponseDto> getRentalsByUserId(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean isActive
    ) {
        return rentalService.getRentalsByUserId(userId, isActive);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/{rentalId}")
    @Operation(summary = "Get specific rental",
            description = "Get specific rental")
    public RentalResponseDto getRentalById(
            @PathVariable Long rentalId
    ) {
        return rentalService.getRentalById(rentalId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/{rentalId}/return")
    @Operation(summary = "Set an actual return date for a rental",
            description = "Set an actual return date for a rental")
    public RentalResponseDto setActualReturnDate(
            @PathVariable Long rentalId) {
        return rentalService.setActualReturnDate(rentalId);
    }
}

