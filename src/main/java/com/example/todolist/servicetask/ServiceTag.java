package com.example.todolist.servicetask;

import com.example.todolist.model.Category;
import com.example.todolist.model.Tag;
import com.example.todolist.model.Task;
import com.example.todolist.repository.TagRepository;
import com.example.todolist.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServiceTag {

    private final TagRepository repository;

    public ServiceTag(TagRepository repository){
        this.repository = repository;
    }

    public Tag saveTag(Tag tag){
        return repository.save(tag);
    }

    public void deleteTask(Long id)
    {
        repository.deleteById(id);
    }

    public Tag findTaskById(Long id)
    {
        return repository.findById(id).orElse(null);
    }

    public Long getIdByName(String name) {
        Optional<Tag> entity = repository.findByName(name);
        return entity.map(Tag::getId).orElse(null);
    }
}
