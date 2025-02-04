package carsharingapp.mapper;

import carsharingapp.config.MapperConfig;
import carsharingapp.dto.RentalRequestDto;
import carsharingapp.dto.RentalResponseDto;
import carsharingapp.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = CarMapper.class)
public interface RentalMapper {
    @Mapping(source = "car", target = "car", qualifiedByName = "toCarRentedDto")
    @Mapping(source = "user.id", target = "userId")
    RentalResponseDto toDto(Rental rental);

    Rental toModel(RentalRequestDto requestDto);
}
