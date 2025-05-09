package com.example.HotelBooking.services.impl;

import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.dtos.RoomDTO;
import com.example.HotelBooking.entities.Room;
import com.example.HotelBooking.enums.RoomType;
import com.example.HotelBooking.exceptions.InvalidBookingStateAndDateException;
import com.example.HotelBooking.exceptions.NotFoundException;
import com.example.HotelBooking.respositories.RoomRepository;
import com.example.HotelBooking.services.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.imap.protocol.UID;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final static String IMAGE_DIRECTORY=System.getProperty("user.dir")+"/product-image";
    @Override
    public Response addRooms(RoomDTO roomDTO, MultipartFile imageFile) {
        Room roomToSave = modelMapper.map(roomDTO, Room.class);
        if (imageFile != null) {
            String imagePath = saveImage(imageFile);
            roomToSave.setImageUrl(imagePath);

        }
        roomRepository.save(roomToSave);

        return Response.builder()
                .status(200)
                .message("Room Successfully Added")
                .build();
    }

    @Override
    public Response updateRoom(RoomDTO roomDTO, MultipartFile imageFile) {
        Room existingRoom=roomRepository.findById(roomDTO.getId())
                .orElseThrow(()->new NotFoundException("Room does not exits"));
        if(imageFile!=null&& !imageFile.isEmpty()){
            String imagePath=saveImage(imageFile);
            existingRoom.setImageUrl(imagePath);
        }
        if(roomDTO.getRoomNumber()!=null && roomDTO.getRoomNumber()>=0){
            existingRoom.setRoomNumber(roomDTO.getRoomNumber());
        }
        if(roomDTO.getPricePerNight()!=null&&roomDTO.getPricePerNight().compareTo(BigDecimal.ZERO)>=0){
            existingRoom.setPricePerNight(roomDTO.getPricePerNight());
        }
        if(roomDTO.getCapacity()!=null && roomDTO.getCapacity()>0){
            existingRoom.setCapacity(roomDTO.getCapacity());

        }
        if(roomDTO.getType()!=null) existingRoom.setType(roomDTO.getType());
        if(roomDTO.getDescription()!=null) existingRoom.setDescription(roomDTO.getDescription());
        roomRepository.save(existingRoom);

        return Response.builder()
                .status(200)
                .message("Rooms updated successfully")
                .build();
    }

    @Override
    public Response getAllRooms() {
        List<Room> roomList=roomRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        List<RoomDTO> roomDTOList=modelMapper.map(roomList,new TypeToken<List<RoomDTO>>(){}.getType());
        return Response.builder()
                .status(200)
                .message("success")
                .rooms(roomDTOList)
                .build();
    }

    @Override
    public Response getRoomById(Long Id) {
        Room room=roomRepository.findById(Id)
                .orElseThrow(()->new NotFoundException("Room not found"));

        RoomDTO roomDTO=modelMapper.map(room,RoomDTO.class);
        return Response.builder()
                .status(200)
                .message("success")
                .room(roomDTO)
                .build();
    }

    @Override
    public Response deleteRoom(Long Id) {
        if(!roomRepository.existsById(Id)){
            throw new NotFoundException("Room not found");
        }
        roomRepository.deleteById(Id);
        return Response.builder()
                .status(200)
                .message("Delete success")

                .build();
    }

    @Override
    public Response GetAvailableRoom(LocalDate checkInDate, LocalDate checkOutDate, RoomType roomType) {
       //Vallidation : ensure check in Data is not before today
        if(checkInDate.isBefore(LocalDate.now())){
            throw new InvalidBookingStateAndDateException("check in date cannot be before today");
        }
        //Validation : ennsure check Out date is not before check in date
        if(checkOutDate.isBefore(checkInDate)){
            throw new InvalidBookingStateAndDateException("check out date must be before check in date");
        }
        //Validation: ensure check in date is not same as check out  date
        if(checkInDate.isEqual(checkOutDate)){
            throw new InvalidBookingStateAndDateException("Check In date cannot be equal to check out date");
        }
        List<Room> roomList=roomRepository.findAvailableRooms(checkInDate,checkOutDate,roomType);
        List<RoomDTO> roomDTOList=modelMapper.map(roomList,new TypeToken<List<RoomDTO>>(){}.getType());
        return Response.builder()
                .status(200)
                .message("success")
                .rooms(roomDTOList)
                .build();
    }

    @Override
    public List<RoomType> getAllRoomTye() {
        return Arrays.asList(RoomType.values());
    }

    @Override
    public Response searchRoom(String input) {
        List<Room> roomList=roomRepository.searchRooms(input);
        List<RoomDTO> roomDTOList=modelMapper.map(roomList,new TypeToken<List<RoomDTO>>(){}.getType());
        return Response.builder()
                .status(200)
                .message("success")
                .rooms(roomDTOList)
                .build();

    }
    //Save Image to Directory
    private String saveImage(MultipartFile imageFile){
        if(!imageFile.getContentType().startsWith("image/")){
            throw new IllegalArgumentException("Only Image file is a Allowed");
        }
        //Generate unique file name for the image
        String uniqueFileName= UUID.randomUUID()+"_"+imageFile.getOriginalFilename();
        String imagePath=IMAGE_DIRECTORY+uniqueFileName;
        try {
            File destinationFile=new File(imagePath);
            imageFile.transferTo(destinationFile);
        }catch (Exception ex){
            throw new IllegalArgumentException(ex.getMessage());
        }
        return imagePath;
    }


}
