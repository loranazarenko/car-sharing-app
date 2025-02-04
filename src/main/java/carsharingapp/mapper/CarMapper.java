package carsharingapp.mapper;

import carsharingapp.config.MapperConfig;
import carsharingapp.dto.CarRentedDto;
import carsharingapp.dto.CarRequestDto;
import carsharingapp.dto.CarResponseDto;
import carsharingapp.model.Car;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    @Mapping(target = "type", ignore = true)
    Car toModel(CarRequestDto requestDto);

    @AfterMapping
    default void setCarType(@MappingTarget Car car, CarRequestDto requestDto) {
        car.setType(Car.Type.valueOf(requestDto.getType().toUpperCase()));
    }

    CarResponseDto toDto(Car car);

    @Named(value = "toCarRentedDto")
    CarRentedDto toCarRentedDto(Car car);

    Car updateCar(CarRequestDto carRequestDto, @MappingTarget Car car);
}
