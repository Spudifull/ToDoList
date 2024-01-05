package com.example.todolist;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.todolist.controller.APIController;
import com.example.todolist.model.Task;
import com.example.todolist.servicetask.ServiceTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class SearchTasksByTitleTest {

    @Mock
    private ServiceTask serviceTask;

    @InjectMocks
    private APIController apiController;

    private List<Task> tasks;

    @BeforeEach
    void SetUp(){
        tasks = Arrays.asList(
                new Task(1L, "Clean house", "Description here"),
                new Task(2L, "Write report", "Description here")
        );

    }

    @Test
    public void whenSearchByTitle_Clean_thenReturnMatchingTasks() {
        when(serviceTask.findTasksByTitle("Clean")).thenReturn(Collections.singletonList(tasks.getFirst()));

        List<Task> result = apiController.searchTasksByTitle("Clean");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Clean house", result.getFirst().getTitle());
    }

    @Test
    public void whenSearchByTitle_Write_thenReturnMatchingTasks() {
        when(serviceTask.findTasksByTitle("Write")).thenReturn(Collections.singletonList(tasks.get(1)));
        List<Task> result = apiController.searchTasksByTitle("Write");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Write report", result.getFirst().getTitle());
    }

    @Test
    public void whenSearchByTitle_NoMatch_thenReturnEmptyList() {
        when(serviceTask.findTasksByTitle("NonExisting")).thenReturn(Collections.emptyList());

        List<Task> result = apiController.searchTasksByTitle("NonExisting");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
