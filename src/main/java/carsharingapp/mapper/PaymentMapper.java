package carsharingapp.mapper;

import carsharingapp.config.MapperConfig;
import carsharingapp.dto.PaymentResponseDto;
import carsharingapp.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper (config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(source = "rental.id", target = "rentalId")
    PaymentResponseDto toDto(Payment payment);
}
