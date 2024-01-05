package com.example.todolist;

import com.example.todolist.controller.APIController;
import com.example.todolist.model.File;
import com.example.todolist.servicetask.FileSystemStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DownloadFileTest {

    @Mock
    private FileSystemStorage fileSystemStorage;

    @InjectMocks
    private APIController apiController;

    @Test
    public void whenDownloadPdf_thenReturnZip() throws MalformedURLException {
        Long fileId = 1L;
        File pdfFile = new File();
        pdfFile.setId(fileId);
        pdfFile.setFileType("application/pdf");

        when(fileSystemStorage.findFileById(fileId)).thenReturn(pdfFile);
        when(fileSystemStorage.downloadPdfAsZip(pdfFile, null)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<Resource> response = apiController.downloadFile(fileId, null);

        verify(fileSystemStorage).downloadPdfAsZip(pdfFile, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void whenDownloadRegularFile_thenReturnFile() throws MalformedURLException {
        Long fileId = 2L;
        File regularFile = new File();
        regularFile.setId(fileId);
        regularFile.setFileType("text/plain");

        when(fileSystemStorage.findFileById(fileId)).thenReturn(regularFile);
        when(fileSystemStorage.downloadRegularFile(regularFile)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<Resource> response = apiController.downloadFile(fileId, null);

        verify(fileSystemStorage).downloadRegularFile(regularFile);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
