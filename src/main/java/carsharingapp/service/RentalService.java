package carsharingapp.service;

import carsharingapp.dto.RentalRequestDto;
import carsharingapp.dto.RentalResponseDto;
import carsharingapp.model.User;
import java.util.List;

public interface RentalService {
    RentalResponseDto save(RentalRequestDto requestDto);

    RentalResponseDto getRentalById(Long rentalId);

    RentalResponseDto setActualReturnDate(Long rentalId);

    List<RentalResponseDto> getRentalsByUserId(
            Long userId, Boolean isActive
    );

    void checkUserHasOpenRentals(User user);
}
