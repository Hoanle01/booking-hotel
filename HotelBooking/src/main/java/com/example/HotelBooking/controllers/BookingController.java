package com.example.HotelBooking.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.HotelBooking.services.BookingService;
import com.example.HotelBooking.dtos.Response;

import com.example.HotelBooking.dtos.BookingDTO;
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')") //Admin alone have access to this api
    private ResponseEntity<Response> getAllBookings(){
        return ResponseEntity.ok(bookingService.getAllBookings());
    }
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CUSTOMER')") //Admin alone have access to this api
    private ResponseEntity<Response> createBooking(@RequestBody BookingDTO bookingDTO){
        return ResponseEntity.ok(bookingService.createBooking(bookingDTO));
    }
    @GetMapping("/{reference}")
    private ResponseEntity<Response> findBookingByReference(@PathVariable String reference){
        return ResponseEntity.ok(bookingService.findBookingBookingByReferenceNo(reference));
    }
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')") //Admin alone have access to this api
    private ResponseEntity<Response> updateBooking(@RequestBody BookingDTO bookingDTO){
        return ResponseEntity.ok(bookingService.updateBooking(bookingDTO));
    }
}
