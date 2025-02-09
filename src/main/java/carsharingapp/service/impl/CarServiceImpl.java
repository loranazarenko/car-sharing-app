package carsharingapp.service.impl;

import carsharingapp.dto.CarRequestDto;
import carsharingapp.dto.CarResponseDto;
import carsharingapp.exception.EntityNotFoundException;
import carsharingapp.mapper.CarMapper;
import carsharingapp.model.Car;
import carsharingapp.repository.CarRepository;
import carsharingapp.service.CarService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarMapper carMapper;
    private final CarRepository carRepository;

    @Override
    @Transactional
    public CarResponseDto addNewCar(CarRequestDto requestDto) {
        checkIfCarTypeIsValid(requestDto.getType());
        Car car = carMapper.toModel(requestDto);
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public List<CarResponseDto> getAllCars(Pageable pageable) {
        return carRepository.findAll(pageable).stream()
                .map(carMapper::toDto)
                .toList();
    }

    @Override
    public CarResponseDto getCarById(Long id) {
        return carMapper.toDto(findCarById(id));
    }

    @Override
    @Transactional
    public CarResponseDto updateCarById(Long id, CarRequestDto requestDto) {
        checkIfCarTypeIsValid(requestDto.getType());
        Car car = findCarById(id);
        car = carMapper.updateCar(requestDto, car);
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public void deleteCarById(Long id) {
        carRepository.deleteById(id);
    }

    private void checkIfCarTypeIsValid(String carType) {
        boolean isValidType = Arrays.stream(Car.Type.values())
                .anyMatch(type -> type.name().equalsIgnoreCase(carType));

        if (!isValidType) {
            throw new IllegalArgumentException("There is no such type of car: " + carType
                    + ". You can use only sedan, suv, hatchback or universal.");
        }
    }

    private Car findCarById(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find a car by this ID: " + id)
        );
    }
}
