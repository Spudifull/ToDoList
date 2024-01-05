package com.example.todolist;

import com.example.todolist.controller.APIController;
import com.example.todolist.model.File;
import com.example.todolist.model.Task;
import com.example.todolist.servicetask.FileSystemStorage;
import com.example.todolist.servicetask.ServiceTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.DelegatingServletOutputStream;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExportTaskTest {

    @Mock
    private ServiceTask serviceTask;
    @Mock
    private FileSystemStorage fileSystemStorage;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private APIController apiController;

    private Task task;

    private ByteArrayOutputStream baos;
    private ZipOutputStream zippedOut;

    @BeforeEach
    void SetUp() throws IOException {
        task = new Task();
        task.setId(1L);
        task.setTitle("Sample Task");

        File file1 = new File();
        file1.setFileType("text/plain");
        file1.setFilePath("/path/to/file1.txt");
        List<File> files = List.of(file1);

        // Добавлена переменная для содержимого JSON
        String jsonContent = "Task JSON content";  // Инициализация переменной содержимым JSON
        when(serviceTask.findTaskById(task.getId())).thenReturn(task);
        when(fileSystemStorage.getFilesByTaskId(task.getId())).thenReturn(files);
        when(serviceTask.convertTaskToJson(any(Task.class), anyList())).thenReturn(jsonContent);

        baos = new ByteArrayOutputStream();
        zippedOut = new ZipOutputStream(baos);

        when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(zippedOut));
    }

    @Test
    public void whenExportTask_thenZipCreated() throws Exception {
        // Имитация метода exportTask
        apiController.exportTask(task.getId(), response);

        // Закрытие ZipOutputStream для финализации записи данных
        zippedOut.close();

        // Проверка создания ZIP-архива
        byte[] zipBytes = baos.toByteArray();
        assertTrue(zipBytes.length > 0, "ZIP data should have been written");

        // Проверка содержимого ZIP-архива
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry = zis.getNextEntry();
            assertNotNull(entry, "Should have at least one entry in the zip");
            assertEquals(task.getTitle() + ".json", entry.getName(), "Entry name should match expected JSON file name");
        }

        // Проверка установки заголовков ответа
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).addHeader(eq("Content-Disposition"), anyString());
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("application/zip");  // Изменено на application/zip

        // Проверка вызовов сервиса
        verify(serviceTask, times(1)).convertTaskToJson(any(Task.class), anyList());
    }
}