package carsharingapp.service;

import carsharingapp.dto.CarRequestDto;
import carsharingapp.dto.CarResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto addNewCar(CarRequestDto requestDto);

    List<CarResponseDto> getAllCars(Pageable pageable);

    CarResponseDto getCarById(Long id);

    CarResponseDto updateCarById(Long id, CarRequestDto requestDto);

    void deleteCarById(Long id);
}
