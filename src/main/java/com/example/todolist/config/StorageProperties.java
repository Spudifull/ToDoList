package com.example.todolist.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "file.upload")
public class StorageProperties {
    private String  location;
}
