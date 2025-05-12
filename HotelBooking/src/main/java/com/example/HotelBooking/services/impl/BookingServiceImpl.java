package com.example.HotelBooking.services.impl;

import com.example.HotelBooking.dtos.BookingDTO;
import com.example.HotelBooking.dtos.NotificationDTO;
import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.entities.Booking;
import com.example.HotelBooking.entities.Room;
import com.example.HotelBooking.enums.BookingStatus;
import com.example.HotelBooking.enums.PaymentStatus;
import com.example.HotelBooking.exceptions.InvalidBookingStateAndDateException;
import com.example.HotelBooking.exceptions.NotFoundException;
import com.example.HotelBooking.notification.NotificationService;
import com.example.HotelBooking.respositories.BookingRepository;
import com.example.HotelBooking.respositories.RoomRepository;
import com.example.HotelBooking.services.BookingCodeGenerator;
import com.example.HotelBooking.services.BookingService;
import com.example.HotelBooking.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.HotelBooking.entities.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService{
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final BookingCodeGenerator bookingCodeGenerator;
    @Override
    public Response getAllBookings() {

        List<Booking> bookingList=bookingRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        List<BookingDTO> bookingDTOList=modelMapper.map(bookingList,new TypeToken<List<BookingDTO>>(){}.getType());
        for(BookingDTO bookingDTO: bookingDTOList){
            bookingDTO.setUser(null);
            bookingDTO.setRoom(null);
        }
        return Response.builder()
                .status(200)
                .message("success")
                .bookings(bookingDTOList)
                .build();
    }

    @Override
    public Response createBooking(BookingDTO bookingDTO) {
       User  currentUser=userService.getCurrentLoggedInUser();
        Room room=roomRepository.findById(bookingDTO.getRoomId())
                .orElseThrow(()-> new NotFoundException("Room not found "));
        //Vallidation : ensure check in Date is not before today
        if(bookingDTO.getCheckInDate().isBefore(LocalDate.now())){
            throw new InvalidBookingStateAndDateException("check in date cannot be before today");
        }
        //Validation : ennsure check Out date is not before check in date
        if(bookingDTO.getCheckOutDate().isBefore(bookingDTO.getCheckInDate())){
            throw new InvalidBookingStateAndDateException("check out date must be before check in date");
        }
        //Validation: ensure check in date is not same as check out  date
        if(bookingDTO.getCheckInDate().isEqual(bookingDTO.getCheckOutDate())){
            throw new InvalidBookingStateAndDateException("Check In date cannot be equal to check out date");
        }
        // Valid room avalibity
        boolean isAvailable=bookingRepository.isRoomAvailable(room.getId(),bookingDTO.getCheckInDate(),bookingDTO.getCheckOutDate());
        if(!isAvailable){
            throw new InvalidBookingStateAndDateException("Room is not ");
        }
        BigDecimal totalPrice=calculateTotalPrice(room,bookingDTO);
        String bookingReference=bookingCodeGenerator.generateBookingReference();
        //create and save to db
        Booking booking=new Booking();
        booking.setUser(currentUser);
        booking.setRoom(room);
        booking.setCheckInDate(bookingDTO.getCheckInDate());
        booking.setCheckOutDate(bookingDTO.getCheckOutDate());
        booking.setTotalPrice(totalPrice);
        booking.setBookingReference(bookingReference);
        booking.setBookingStatus(BookingStatus.BOOKED);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setCreatedAt(LocalDate.now());
        bookingRepository.save(booking);
        String paymentLink="http://localhost:4200/payment"+bookingReference+"/"+totalPrice;
        log.info("Booking success payment link is",paymentLink);
        //send email to user via mail
        NotificationDTO notificationDTO= NotificationDTO.builder()
                .recipient(currentUser.getEmail())
                .subject("Booking Confirmation")
                .body(String.format("Your Booking has been created successfully.\n Please proceed with your payment using the payment"+"like below \n%s",paymentLink))
                .bookingReference(bookingReference)
                .build();
        notificationService.sendEmail(notificationDTO);
        return Response.builder()
                .status(200)
                .message("Booking is successful")
                .booking(bookingDTO)
                .build();
    }

    private BigDecimal calculateTotalPrice(Room room, BookingDTO bookingDTO) {
        BigDecimal pricePerNight=room.getPricePerNight();
        long days= ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(),bookingDTO.getCheckOutDate());
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }

    @Override
    public Response findBookingBookingByReferenceNo(String bookingReference) {

        Booking booking=bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(()->new NotFoundException("Booking Not Found"));
        BookingDTO bookingDTO=modelMapper.map(booking,BookingDTO.class);
        return Response.builder()
                .status(200)
                .message("success")
                .booking(bookingDTO)
                .build();
    }

    @Override
    public Response updateBooking(BookingDTO bookingDTO) {

        if(bookingDTO.getId()==null) throw new NotFoundException("Booking ID is Required");
        Booking existingBooking=bookingRepository.findById(bookingDTO.getId())
                .orElseThrow(()->new NotFoundException("booking not found"));
        if(bookingDTO.getBookingStatus()!=null){
            existingBooking.setBookingStatus(bookingDTO.getBookingStatus());

        }
        if(bookingDTO.getPaymentStatus()!=null){
            existingBooking.setPaymentStatus(bookingDTO.getPaymentStatus());

        }
        bookingRepository.save(existingBooking);

        return Response.builder()
                .status(200)
                .message("booking updated successfully")
                .build();
    }
}
