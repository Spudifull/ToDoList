package com.example.todolist.repository;

import com.example.todolist.model.File;
import com.example.todolist.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long>{
    List<File> findByTaskId(Long id);

    File findFileById(Long id);

    List<File> findByOriginalFieldID(Long pdfId);
}
