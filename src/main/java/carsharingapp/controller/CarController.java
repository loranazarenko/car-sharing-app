package carsharingapp.controller;

import carsharingapp.dto.CarRequestDto;
import carsharingapp.dto.CarResponseDto;
import carsharingapp.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
@Tag(name = "Car management", description = "Endpoints for managing cars")
@SecurityRequirement(name = "bearerAuth")
public class CarController {
    private final CarService carService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Add a new car",
            description = "Add a new car",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public CarResponseDto addNewCar(@RequestBody @Valid CarRequestDto requestDto) {
        return carService.addNewCar(requestDto);
    }

    @GetMapping
    @Operation(summary = "Get a list of cars", description = "Get a list of cars")
    public List<CarResponseDto> getAllCars(Pageable pageable) {
        return carService.getAllCars(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car's detailed information",
            description = "Get car's detailed information")
    public CarResponseDto getCarById(@PathVariable Long id) {
        return carService.getCarById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Update a car by id",
            description = "Update a car by id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public CarResponseDto updateCarById(
            @PathVariable Long id,
            @RequestBody @Valid CarRequestDto requestDto
    ) {
        return carService.updateCarById(id, requestDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a car by id",
            description = "Delete a car by id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public void deleteCarById(@PathVariable Long id) {
        carService.deleteCarById(id);
    }
}
