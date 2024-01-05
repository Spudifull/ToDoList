package com.example.todolist;

import com.example.todolist.controller.APIController;
import com.example.todolist.servicetask.ServiceTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.todolist.model.Task;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class GetTaskByIdTest {

    @Mock
    private ServiceTask serviceTask;

    @InjectMocks
    private APIController apiController;

    private Task existingTask;
    private final Long existingId = 1L;

    @BeforeEach
    void SetUp(){
        existingTask = new Task();
        existingTask.setId(existingId);
        existingTask.setTitle("Existing Task");
    }

    @Test
    public void whenTaskExists_thenRespondWithTaskAndOkStatus() {
        when(serviceTask.findTaskById(existingId)).thenReturn(existingTask);

        ResponseEntity<Task> response = apiController.getTaskById(existingId);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getId(), existingId);
        assertEquals(response.getBody().getTitle(), "Existing Task");
    }

    @Test
    public void whenTaskDoesNotExist_thenRespondWithNotFoundStatus() {
        Long nonExistingId = 2L;
        when(serviceTask.findTaskById(nonExistingId)).thenReturn(null);
        ResponseEntity<Task> response = apiController.getTaskById(nonExistingId);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
        assertNull(response.getBody());
    }
}
