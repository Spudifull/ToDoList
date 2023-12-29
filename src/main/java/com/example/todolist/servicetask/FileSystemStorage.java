package com.example.todolist.servicetask;

import com.example.todolist.model.File;
import com.example.todolist.model.Task;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.zip.ZipInputStream;


public interface FileSystemStorage{
    void init();
    String saveFile(MultipartFile file, Task task);
    Resource loadFile(String fileName);
    List<File> getFilesByTaskId(Long taskId);

    File findFileById(Long id);

    ResponseEntity<Resource> downloadRegularFile(File fileEntity) throws MalformedURLException;

    ResponseEntity<Resource> downloadPdfAsZip(File fileEntity, HttpServletResponse response);

    java.io.File saveZipEntryToFile(ZipInputStream zipIn, String fileName) throws IOException;

    void saveFromZipFile(java.io.File file, Task task);
}
