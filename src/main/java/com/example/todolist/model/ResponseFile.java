package com.example.todolist.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseFile {
    private String fileName;
    private String fileUrl;
    private String message;
}
