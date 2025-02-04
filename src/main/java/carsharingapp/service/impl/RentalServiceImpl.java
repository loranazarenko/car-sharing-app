package carsharingapp.service.impl;

import carsharingapp.dto.RentalRequestDto;
import carsharingapp.dto.RentalResponseDto;
import carsharingapp.exception.CarAvailableException;
import carsharingapp.exception.RentalException;
import carsharingapp.mapper.RentalMapper;
import carsharingapp.model.Car;
import carsharingapp.model.Rental;
import carsharingapp.model.User;
import carsharingapp.repository.CarRepository;
import carsharingapp.repository.RentalRepository;
import carsharingapp.repository.UserRepository;
import carsharingapp.service.RentalService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Setter
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final RentalMapper rentalMapper;
    private final TelegramNotificationService notificationService;

    @Value("${telegram.chat.id}")
    private String chatId;

    @Override
    @Transactional
    public RentalResponseDto save(
            RentalRequestDto requestDto
    ) {
        User user = userRepository.getReferenceById(requestDto.getUserId());
        checkUserHasOpenRentals(user);
        Car car = findCarById(requestDto.getCarId());
        checkIsCarAvailable(car);
        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);

        Rental rental = createNewRental(requestDto, car, user);
        rentalRepository.save(rental);
        Long chatId = user.getTelegramChatId();
        String username = user.getUsername();
        notificationService.sendNotification(chatId,
                "New rental created for user " + username);
        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public RentalResponseDto getRentalById(Long rentalId) {
        Rental rental = findRentalById(rentalId);
        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public RentalResponseDto setActualReturnDate(Long rentalId) {
        Rental rental = findRentalById(rentalId);
        checkRentalIsClosed(rental);
        rental.setActualReturnDate(LocalDate.now());
        rentalRepository.save(rental);

        Car car = findCarById(rental.getCar().getId());
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);
        notificationService.sendNotification(rental.getUser().getTelegramChatId(),
                "You have just returned the rental ");
        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public List<RentalResponseDto> getRentalsByUserId(
            Long userId, Boolean isActive
    ) {
        return rentalRepository.findAll().stream()
                .filter(rental -> userId == null || rental.getUser().getId().equals(userId))
                .filter(rental -> {
                    if (isActive == null) {
                        return true;
                    }
                    return isActive ? rental.getActualReturnDate() == null :
                            rental.getActualReturnDate() != null;
                })
                .map(rentalMapper::toDto)
                .toList();
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void checkOverdueRentals() {
        List<Rental> overdueRentals =
                rentalRepository.findAll()
                .stream()
                .filter(rental -> rental.getReturnDate().isBefore(LocalDate.now())
                        && rental.getActualReturnDate() == null)
                .toList();

        if (overdueRentals.isEmpty()) {
            notificationService.sendNotification(Long.valueOf(chatId),
                    "No rentals overdue today!");
        } else {
            for (Rental rental : overdueRentals) {
                notificationService.sendNotification(Long.valueOf(chatId),
                        "Overdue rental with id: " + rental.getId());
            }
        }
    }

    @Override
    public void checkUserHasOpenRentals(User user) {
        List<Rental> rentalList = rentalRepository.findOpenRental(user.getId());
        if (!rentalList.isEmpty()) {
            throw new RentalException("Sorry, but you already have open rental.");
        }
    }

    private Rental createNewRental(
            RentalRequestDto requestDto, Car car, User user
    ) {
        return new Rental()
                .setRentalDate(requestDto.getRentalDate())
                .setReturnDate(requestDto.getReturnDate())
                .setActualReturnDate(null)
                .setUser(user)
                .setCar(car);
    }

    private static void checkIsCarAvailable(Car car) {
        if (car.getInventory() == 0) {
            throw new CarAvailableException("Sorry, car with id "
                    + car.getId() + " is not available for the rent. Please take another one."
            );
        }
    }

    private Car findCarById(Long carId) {
        return carRepository.findById(carId).orElseThrow(
                () -> new RentalException("Can't find a car by ID: " + carId)
        );
    }

    private Rental findRentalById(Long rentalId) {
        return rentalRepository.findById(rentalId).orElseThrow(
                () -> new RentalException("Can't find a rental by ID: " + rentalId));
    }

    private static void checkRentalIsClosed(Rental rental) {
        if (rental.getActualReturnDate() != null) {
            throw new RentalException("This rental is closed.");
        }
    }
}
