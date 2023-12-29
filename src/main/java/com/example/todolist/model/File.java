package com.example.todolist.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileType;

    private Long originalFieldID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @PostPersist
    public void setOriginalFieldID() {
        if (this.originalFieldID == null) {
            this.originalFieldID = this.id;
        }
    }
}
