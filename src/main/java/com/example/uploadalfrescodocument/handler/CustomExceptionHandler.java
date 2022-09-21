package com.example.uploadalfrescodocument.handler;


import com.example.uploadalfrescodocument.alfresco.dto.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ControllerAdvice
public class CustomExceptionHandler {


    @ExceptionHandler
    public ResponseEntity<ResponseData> handle(RuntimeException exception) {


        return new ResponseEntity<>(new ResponseData(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
