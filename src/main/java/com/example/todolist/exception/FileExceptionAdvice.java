package com.example.todolist.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.example.todolist.model.ResponseError;

@ControllerAdvice
public class FileExceptionAdvice extends ResponseEntityExceptionHandler{

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Object> handleFileNotFoundException(FileNotFoundException exception)
    {
        List<String> details = new ArrayList<String>();
        details.add(exception.getMessage());
        ResponseError error = new ResponseError(LocalDateTime.now(), "File not found", details);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

 }
