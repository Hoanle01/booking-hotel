package com.example.HotelBooking.services.impl;

import com.example.HotelBooking.dtos.*;
import com.example.HotelBooking.entities.Booking;
import com.example.HotelBooking.entities.User;
import com.example.HotelBooking.enums.UserRole;
import com.example.HotelBooking.exceptions.InvalidCredentialException;
import com.example.HotelBooking.exceptions.NotFoundException;
import com.example.HotelBooking.respositories.BookingRepository;
import com.example.HotelBooking.respositories.UserRepository;
import com.example.HotelBooking.security.JwtUtils;
import com.example.HotelBooking.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;
    @Override
    public Response registerUser(RegistrationRequest registrationRequest) {
        UserRole role=UserRole.CUSTOMER;
        if(registrationRequest.getRole()!=null){
            role=registrationRequest.getRole();
        }
        User userToSave=User.builder()
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .role(role)
                .active(true)
                .build();
        userRepository.save(userToSave);
        return Response.builder()
                .status(200)
                .message("User Registered Successfully")
                .build();
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {


        log.info("INSIDE loginUser()"+loginRequest.getPassword());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Email Not Found"));
        log.info(user.getPassword());
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Password does not match");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        return Response.builder()
                .status(200)
                .message("user logged in successfully")
                .role(user.getRole())
                .token(token)
                .active(user.isActive())
                .expirationTime("6 month")
                .build();

    };


    @Override
    public Response getAllUser() {
        List<User> users=userRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        List<UserDTO> userDTOList=modelMapper.map(users,new TypeToken<List<UserDTO>>(){}.getType());
        return  Response.builder()
                .status(200)
                .message("success")
                .users(userDTOList)
                .build();
    }

    @Override
    public Response getOwnAccountDetails() {
        log.info("INSIDE getOWAccountDetails");
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("user not found"));
        UserDTO userDTO=modelMapper.map(user,UserDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .user(userDTO)
                .build();
    }

    @Override
    public User getCurrentLoggedInUser() {
        log.info("INSIZE getCurrentLoggedInUser");
        String email =SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("user not found"));
    }

    @Override
    public Response updateOwnAccount(UserDTO userDTO) {
        log.info("INSIDE updateOwnAccount");
        User existingUser =getCurrentLoggedInUser();
        if(userDTO.getEmail()!=null) existingUser.setEmail(userDTO.getEmail());
        if(userDTO.getFirstName()!=null) existingUser.setFirstName(userDTO.getFirstName());
        if(userDTO.getLastName()!=null) existingUser.setLastName(userDTO.getLastName());
        if(userDTO.getPhoneNumber()!=null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());

        if(userDTO.getPassword()!=null &&!userDTO.getPassword().isEmpty()){
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        userRepository.save(existingUser);
        return Response.builder()
                .status(200)
                .message("User update Suceessfully")
                .build();


    }

    @Override
    public Response deleteOwnAccount() {
        log.info("INSIZE DELETEownaccount");
        User existingUser=getCurrentLoggedInUser();
        userRepository.delete(existingUser);
        return Response.builder()
                .status(200)
                .message("User Deleted Successfully")
                .build();
    }

    @Override
    public Response getMyBookingHistory() {
        log.info("INSIDE getMyBookingHistory");
        User currentUser=getCurrentLoggedInUser();
        List<Booking> bookingList=bookingRepository.findByUserId(currentUser.getId());
        List<BookingDTO> bookingDTOList=modelMapper.map(bookingList,new TypeToken<List<BookingDTO>>(){}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .bookings(bookingDTOList)
                .build();
    }
}
