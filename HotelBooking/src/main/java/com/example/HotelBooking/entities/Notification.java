package com.example.HotelBooking.entities;

import com.example.HotelBooking.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name="notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Subject is required")
    private String subject;
    @NotBlank(message = "Recipient is required")
    private String recipient;
    private String body;
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
     private String bookingReference;
     private final LocalDate createdAt=LocalDate.now();
}
