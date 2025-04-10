package com.example.HotelBooking.respositories;

import com.example.HotelBooking.entities.Room;
import com.example.HotelBooking.enums.RoomTye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room,Long> {


    @Query("""
            SELECT r FROM Room r
            WHERE
               r.id NOT IN(
               SELECT b.room.id
               FROM Booking b
               WHERE :checkInDate<=b.checkOutDate
               AND :checkOutDate>= b.checkInDate
               AND b.bookingStatus IN ('BOOKED','CHECKED_IN')
               )
               AND(:roomType is NULL OR r.TYPE=:roomType)
            """)
    List<Room> findAvailableRoooms(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("roomType") RoomTye roomTye
            );
    @Query("""
            SELECT r FROM Room r
            WHERE CAST(r.roomNumber As string) LIKE %:searchParam%
               OR LOWER(r.type) LiKE LOWER(:searchParam)
               OR CAST(r.pricePerNight AS string) LIKE %:searchParam%
               OR CAST(r.capacity AS string) LIKE %:searchParam%
               OR LOWER(r.description)LIKE LOWER(CONCAT('%',:searchParam,'%'))
            """)
    List<Room> searchRooms(@Param("searchParam") String searchParam);
}
