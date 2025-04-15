package com.example.HotelBooking.exceptions;

import com.example.HotelBooking.dtos.Respose;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Respose> handleAllUnknowmException(Exception e)
    {
        Respose respose=Respose.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(respose,HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Respose> handleNotFoundException(Exception e)
    {
        Respose respose=Respose.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(respose,HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(NameValueRequiredException.class)
    public ResponseEntity<Respose> handleNameValueRequiredException(Exception e)
    {
        Respose respose=Respose.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(respose,HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<Respose> handleInvalidBookingstateAndDateException(Exception e)
    {
        Respose respose=Respose.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(respose,HttpStatus.BAD_REQUEST);
    }

}
