package carsharingapp.service;

import static carsharingapp.util.TestUtils.NOT_VALID_ID;
import static carsharingapp.util.TestUtils.VALID_ID;
import static carsharingapp.util.TestUtils.createPageable;
import static carsharingapp.util.TestUtils.createValidCar;
import static carsharingapp.util.TestUtils.createValidCarRequestDto;
import static carsharingapp.util.TestUtils.createValidCarResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import carsharingapp.dto.CarRequestDto;
import carsharingapp.dto.CarResponseDto;
import carsharingapp.exception.EntityNotFoundException;
import carsharingapp.mapper.CarMapper;
import carsharingapp.model.Car;
import carsharingapp.repository.CarRepository;
import carsharingapp.service.impl.CarServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CarServiceTest {
    @Mock
    private CarRepository carRepository;
    @Mock
    private CarMapper carMapper;
    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("save() method works")
    public void save_WithValidCarRequestDto_ReturnCarResponseDto() {
        //Given
        CarRequestDto requestDto = createValidCarRequestDto();
        CarResponseDto expected = createValidCarResponseDto();
        Car car = createValidCar();
        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expected);
        //When
        CarResponseDto actual = carService.addNewCar(requestDto);
        //Then
        assertEquals(actual, expected);
    }

    @Test
    @DisplayName("getAll() method works")
    public void getAll_WithValidPageable_ReturnCarResponseDtoList() {
        //Given
        Car car = createValidCar();
        CarResponseDto responseDto = createValidCarResponseDto();
        List<CarResponseDto> expected = List.of(responseDto);
        Pageable pageable = createPageable();
        when(carRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(car)));
        when(carMapper.toDto(car)).thenReturn(responseDto);
        //When
        List<CarResponseDto> actual = carService.getAllCars(pageable);
        //Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getById() method works")
    public void getById_WithValidId_ReturnCarResponseDto() {
        //Given
        Car car = createValidCar();
        CarResponseDto expected = createValidCarResponseDto();
        when(carRepository.findById(VALID_ID))
                .thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expected);
        //When
        CarResponseDto actual = carService.getCarById(VALID_ID);
        //Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getById() method with invalid 'id' throws EntityNotFoundException")
    public void getById_WithInvalidID_ThrowsEntityNotFoundException() {
        //Given
        when(carRepository.findById(NOT_VALID_ID))
                .thenReturn(Optional.empty());
        //Then
        assertThrows(EntityNotFoundException.class, () -> carService.getCarById(NOT_VALID_ID));
    }

    @Test
    @DisplayName("updateById() method works")
    public void updateById_WithValidIdAndRequestDto_ReturnCarResponseDto() {
        //Given
        Car existingCar = createValidCar();
        CarRequestDto requestDto = createValidCarRequestDto();
        CarResponseDto expected = createValidCarResponseDto();
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(existingCar));
        when(carRepository.save(existingCar)).thenReturn(existingCar);
        when(carMapper.updateCar(eq(requestDto), eq(existingCar)))
                .thenReturn(existingCar);
        when(carMapper.toDto(existingCar)).thenReturn(expected);
        //When
        CarResponseDto actual = carService.updateCarById(VALID_ID, requestDto);
        //Then
        assertEquals(expected, actual);
        verify(carMapper).updateCar(requestDto, existingCar);
        verify(carRepository).save(existingCar);
    }

    @Test
    @DisplayName("updateById() method with invalid 'id' throws EntityNotFoundException")
    public void updateById_WithInvalidId_ThrowsEntityNotFoundException() {
        //Given
        CarRequestDto requestDto = createValidCarRequestDto();
        //When
        when(carRepository.findById(NOT_VALID_ID)).thenReturn(Optional.empty());
        //Then
        assertThrows(EntityNotFoundException.class,
                () -> carService.updateCarById(NOT_VALID_ID, requestDto));
    }

    @Test
    @DisplayName("deleteById() method works")
    public void deleteById_WithValidId_ReturnCarResponseDto() {
        //When
        carService.deleteCarById(VALID_ID);
        //Then
        verify(carRepository).deleteById(VALID_ID);
    }
}
