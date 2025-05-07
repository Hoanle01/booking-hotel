package com.example.HotelBooking.services;

import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.dtos.RoomDTO;
import com.example.HotelBooking.enums.RoomType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {
    Response addRooms(RoomDTO roomDTO , MultipartFile multipartFile);
    Response updateRoom(RoomDTO roomDTO,MultipartFile multipartFile);
    Response getAllRooms();
    Response getRoomById(Long Id);
    Response deleteRoom(Long Id);
    Response GetAvailableRoom(LocalDate CheckInDate, LocalDate CheckOutDate, RoomType roomType);
    List<RoomType> getAllRoomTye();
    Response searchRoom(String input);
}
