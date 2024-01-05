package com.example.todolist;

import com.example.todolist.controller.APIController;
import com.example.todolist.model.Category;
import com.example.todolist.model.Tag;
import com.example.todolist.model.Task;
import com.example.todolist.servicetask.FileSystemStorage;
import com.example.todolist.servicetask.ServiceCategory;
import com.example.todolist.servicetask.ServiceTag;
import com.example.todolist.servicetask.ServiceTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
public class UpdateTaskTest {

    @Mock
    private ServiceTask serviceTask;
    @Mock
    private FileSystemStorage fileSystemStorage;
    @Mock
    private ServiceCategory catService;
    @Mock
    private ServiceTag tagService;

    @InjectMocks
    private APIController apiController;

    private Task existingTask;
    private Category category;
    private Tag tag;
    private MultipartFile[] files;

    @BeforeEach
    void SetUp() {
        existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Existing Task");
        existingTask.setDescription("Existing Description");

        category = new Category();
        tag = new Tag();

        MultipartFile file1 = new MockMultipartFile("file1.txt", "file1.txt", "text/plain", "Some content".getBytes());
        files = new MultipartFile[]{file1};

        lenient().when(catService.findTaskById(anyLong())).thenReturn(category);
        lenient().when(tagService.findTaskById(anyLong())).thenReturn(tag);
        lenient().when(serviceTask.findTaskById(existingTask.getId())).thenReturn(existingTask);
        lenient().when(serviceTask.saveTask(any(Task.class))).thenReturn(existingTask);
    }

    @Test
    public void whenUpdateExistingTask_thenReturnUpdatedTask() {
        ResponseEntity<Task> response = apiController.updateTask(existingTask.getId(), "Updated Title", "Updated Description", 1L, 1L, files);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Task result = response.getBody();
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(category, result.getCategory());
        assertEquals(tag, result.getTag());

        for (MultipartFile file : files) {
            verify(fileSystemStorage, times(1)).saveFile(eq(file), any(Task.class));
        }
    }

    @Test
    public void whenUpdateNonExistingTask_thenNotFound() {
        Long nonExistingId = 2L;
        when(serviceTask.findTaskById(nonExistingId)).thenReturn(null);

        ResponseEntity<Task> response = apiController.updateTask(nonExistingId, "Title", "Description", 1L, 1L, files);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}