package com.example.todolist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.todolist.model.Task;
import java.util.List;

public interface Repository extends JpaRepository<Task, Long>{
    List<Task> findByTitle(String title);

    List<Task> findAllByOrderByCreationDate();

    List<Task> findAllByOrderByTitleAsc();

    List<Task> findByTagId(Long id);
}
