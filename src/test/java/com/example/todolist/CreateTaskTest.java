package com.example.todolist;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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

@ExtendWith(MockitoExtension.class)
public class CreateTaskTest {

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

    private Task preparedTask;

    private MultipartFile[] files;

    private MultipartFile pdfFile;

    @BeforeEach
    void SetUp() {
        preparedTask = new Task();
        preparedTask.setId(1L);
        preparedTask.setTitle("Sample Title");
        preparedTask.setDescription("Sample Description");

        Category category = new Category();
        Tag tag = new Tag();

        MultipartFile file1 = new MockMultipartFile("file1.txt", "file1.txt", "text/plain", "Some content".getBytes());
        MultipartFile file2 = new MockMultipartFile("file2.txt", "file2.txt", "text/plain", "Some content".getBytes());
        files = new MultipartFile[]{file1, file2};

        pdfFile = new MockMultipartFile("document.pdf", "document.pdf", "application/pdf", "PDF content".getBytes());

        when(catService.findTaskById(anyLong())).thenReturn(category);
        when(tagService.findTaskById(anyLong())).thenReturn(tag);
        when(serviceTask.saveTask(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(preparedTask.getId()); // Установка ID для возвращаемого объекта
            return task;
        });
    }

    @Test
    public void whenCreateTask_thenReturnNewTask() {
        ResponseEntity<?> response = apiController.createTask("New Task", "New Description", new MultipartFile[]{}, "1", "1");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Task result = (Task) response.getBody();
        assertEquals("New Task", result.getTitle());
        assertEquals("New Description", result.getDescription());
    }

    @Test
    public void whenCreateTaskWithFiles_thenFilesSavedSuccessfully() {
        ResponseEntity<?> response = apiController.createTask("New Task", "New Description", files, "1", "1");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Task result = (Task) response.getBody();
        assertEquals("New Task", result.getTitle());

        // Проверка, что метод saveFile был вызван для каждого файла
        for (MultipartFile file : files) {
            verify(fileSystemStorage, times(1)).saveFile(eq(file), any(Task.class));
        }
    }

    @Test
    public void whenCreateTaskAndFileSavingFails_thenInternalServerError() {
        // Настройка мока для генерации исключения при сохранении файла
        doThrow(new RuntimeException("File saving failed")).when(fileSystemStorage).saveFile(any(MultipartFile.class), any(Task.class));

        ResponseEntity<?> response = apiController.createTask("New Task", "New Description", files, "1", "1");

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        // Проверка, что тело ответа содержит ожидаемое сообщение об ошибке
        assertEquals("Failed to save file: file1.txt", response.getBody());
    }

    @Test
    public void whenCreateTaskWithPdf_thenPdfProcessedSuccessfully() {
        ResponseEntity<?> response = apiController.createTask("New Task", "New Description", new MultipartFile[]{pdfFile}, "1", "1");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Task result = (Task) response.getBody();
        assertEquals("New Task", result.getTitle());

        // Проверка, что метод saveFile был вызван для PDF-файла
        verify(fileSystemStorage, times(1)).saveFile(eq(pdfFile), any(Task.class));
    }
}