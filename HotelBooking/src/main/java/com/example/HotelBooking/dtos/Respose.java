package com.example.HotelBooking.dtos;

import com.example.HotelBooking.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Respose {
    //generic
    private int status;
    private String message;

    //for login
    private String token;
    private UserRole role;
    private boolean active;
    private String expirationTime;

    //user data
    private UserDTO user;
    private List<UserDTO> users;

    //user Data
    private  BookingDTO booking;
    private List<BookingDTO> bookings;
    //room Data
    private  RoomDTO room;
    private List<RoomDTO> rooms;
    //room payments
    private String transactionId;
    private  PaymentDTO payment;
    private List<PaymentDTO> payments;
    //room notification
    private  NotificationDTO notification;
    private List<NotificationDTO> notificationDTOS;

    private final LocalDateTime timetamp=LocalDateTime.now();
}
