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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class DeleteTaskTest {

    @Mock
    private ServiceTask serviceTask;

    @InjectMocks
    private APIController apiController;

    private Task existingTask;

    @BeforeEach
    void SetUp() {
        existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Existing Task");
        existingTask.setDescription("Existing Description");

        lenient().when(serviceTask.findTaskById(existingTask.getId())).thenReturn(existingTask);
    }

    @Test
    public void whenDeleteExistingTask_thenSuccess() {
        ResponseEntity<Void> response = apiController.deleteTask(existingTask.getId());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(serviceTask, times(1)).deleteTask(existingTask.getId());
    }

    @Test
    public void whenDeleteNonExistingTask_thenNotFound() {
        Long nonExistingId = 2L;
        when(serviceTask.findTaskById(nonExistingId)).thenReturn(null);

        ResponseEntity<Void> response = apiController.deleteTask(nonExistingId);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(serviceTask, never()).deleteTask(nonExistingId);
    }
}
