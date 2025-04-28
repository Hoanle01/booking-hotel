package com.example.HotelBooking.notification;

import com.example.HotelBooking.dtos.NotificationDTO;
import com.example.HotelBooking.entities.Notification;
import com.example.HotelBooking.enums.NotificationType;
import com.example.HotelBooking.respositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final JavaMailSender javaMailSender;
    private  final NotificationRepository notificationENotificationRepository;
    @Override
    @Async
    public void sendEmail(NotificationDTO notificationDTO) {
        log.info("Sending email");
        SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
        simpleMailMessage.setTo(notificationDTO.getRecipient());
        simpleMailMessage.setSubject(notificationDTO.getSubject());
        simpleMailMessage.setText(notificationDTO.getBody());
        javaMailSender.send(simpleMailMessage);//sending emailing
        //save to database
        Notification nptifiNotificationToSave=Notification.builder()
                .recipient(notificationDTO.getRecipient())
                .subject(notificationDTO.getSubject())
                .body(notificationDTO.getBody())
                .bookingReference(notificationDTO.getBookingReference())
                .notificationType(NotificationType.EMAIL)
                .build();
        notificationENotificationRepository.save(nptifiNotificationToSave);
    }

    @Override
    public void sendSms() {

    }

    @Override
    public void sendWhatapps() {

    }
}
