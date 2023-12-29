package com.example.todolist.servicetask;

import com.example.todolist.model.Category;
import com.example.todolist.model.Tag;
import com.example.todolist.model.Task;
import com.example.todolist.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServiceCategory {
    private final CategoryRepository categoryRepository;

    public ServiceCategory(CategoryRepository repository){
        this.categoryRepository = repository;
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteTask(Long id)
    {
        categoryRepository.deleteById(id);
    }

    public Category findTaskById(Long id)
    {
        return categoryRepository.findById(id).orElse(null);
    }

    public Long getIdByName(String name) {
        Optional<Category> entity = categoryRepository.findByName(name);
        return entity.map(Category::getId).orElse(null);
    }
}
