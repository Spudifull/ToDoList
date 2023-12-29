package com.example.todolist.controller;

import com.example.todolist.model.Category;
import com.example.todolist.model.File;
import com.example.todolist.model.Tag;
import com.example.todolist.model.Task;
import com.example.todolist.servicetask.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.*;

@RestController
@RequestMapping("/api/tasks/")
public class APIController {

    private final ServiceTask service;
    private final FileSystemStorage fileSystemStorage;

    private final ServiceCategory catService;

    private final ServiceTag tagService;

    private final PDFService pdfService;

    public APIController(ServiceTask taskService, FileSystemStorage storage,
                         ServiceCategory catService, ServiceTag tagService,
                         PDFService pdfService) {
        this.service = taskService;
        this.fileSystemStorage = storage;
        this.catService = catService;
        this.tagService = tagService;
        this.pdfService = pdfService;
    }

    /**
     * Возвращает список всех задач. Не принимает параметров и возвращает полный список задач,
     * доступных в системе.
     *
     * @return Список всех задач.
     */
    @GetMapping("/")
    public List<Task> getAllTask(){
        return service.findAllTasks();
    }

    /**
     * Возвращает задачу по её идентификатору. Если задача с таким идентификатором существует,
     * возвращает её и статус OK. Если задача не найдена, возвращает статус 'Not Found'.
     *
     * @param id Идентификатор задачи, которую необходимо найти.
     * @return ResponseEntity с задачей и соответствующим статусом.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = service.findTaskById(id);
        if (task != null) {
            return ResponseEntity.ok(task);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ищет и возвращает список задач, чьи названия содержат заданную подстроку.
     * Полезно для функциональности поиска по названию задачи.
     *
     * @param title Подстрока для поиска в названии задач.
     * @return Список задач, соответствующих критериям поиска.
     */
    @GetMapping("/search")
    public List<Task> searchTasksByTitle(@RequestParam String title) {
        return service.findTasksByTitle(title);
    }

    /**
     * Создает новую задачу с указанными названием, описанием, категорией, тэгом и файлами.
     * Возвращает созданную задачу, если операция успешна, или соответствующий статус ошибки.
     *
     * @param title Название задачи.
     * @param description Описание задачи.
     * @param files Массив файлов, связанных с задачей.
     * @param categoryID Идентификатор категории задачи.
     * @param tagID Идентификатор тэга задачи.
     * @return ResponseEntity с созданной задачей или статусом ошибки.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestParam String title, @RequestParam String description,
                                           @RequestParam("files") MultipartFile[] files,
                                        @RequestParam String categoryID, @RequestParam String tagID) {
        // Инициализация новой задачи с данными
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);

        // Поиск и установка категории и тэга задачи
        Category category = catService.findTaskById(Long.parseLong(categoryID));
        Tag tag = tagService.findTaskById(Long.parseLong(tagID));
        task.setCategory(category);
        task.setTag(tag);

        // Сохранение задачи
        Task savedTask = service.saveTask(task);

        // Обработка и сохранение файлов, связанных с задачей
        for(MultipartFile file: files){
            if(!file.isEmpty()){
                try{
                    fileSystemStorage.saveFile(file, savedTask);
                } catch(Exception exception){
                    exception.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save file: " + file.getOriginalFilename());
                }
            }
        }

        // Проверка успешности сохранения задачи и возврат результата
        if (savedTask != null && savedTask.getId() != null) {
            return ResponseEntity.ok(savedTask);
        } else {
            // Возврат ошибки, если создание задачи не удалось
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Обновляет информацию о задаче по заданному идентификатору, включая название, описание, категорию, тэг и файлы.
     * Возвращает обновленную задачу, если операция успешна, или статус 'Не найдено', если задача с таким ID не существует.
     *
     * @param id Идентификатор задачи для обновления.
     * @param title Новое название задачи.
     * @param description Новое описание задачи.
     * @param categoryId Идентификатор новой категории задачи.
     * @param tagId Идентификатор нового тэга задачи.
     * @param files Массив файлов для связи с задачей.
     * @return ResponseEntity с обновленной задачей или статусом 'Не найдено'.
     */
    @PutMapping("/edit/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestParam("title") String title,
                                           @RequestParam("description") String description,
                                           @RequestParam("categoryId") Long categoryId,
                                           @RequestParam("tagId") Long tagId,
                                           @RequestParam("files") MultipartFile[] files) {

        // Поиск существующей задачи по ID
        Task task = service.findTaskById(id);
        if (task != null) {
            // Обновление основной информации задачи
            task.setTitle(title);
            task.setDescription(description);

            // Обновление категории задачи
            Category category = catService.findTaskById(categoryId);
            task.setCategory(category);

            // Обновление тэга задачи
            Tag tag = tagService.findTaskById(tagId);
            task.setTag(tag);

            // Обработка и сохранение прикрепленных файло
            for (MultipartFile file : files) {
                if(!file.isEmpty()){
                    try{
                        fileSystemStorage.saveFile(file, task);
                    } catch(Exception exception){
                        // Логирование ошибки при сохранении файл
                        exception.printStackTrace();
                    }
                }
            }

            // Сохранение обновленной задачи и возврат результата
            Task updatedTask = service.saveTask(task);
            return ResponseEntity.ok(updatedTask);
        } else {
            // Возврат статуса 'Не найдено', если задача с таким ID не существует
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Обрабатывает HTTP DELETE запрос для удаления задачи по заданному идентификатору.
     * Возвращает успешный статус, если задача найдена и удалена, или статус 'Не найдено', если задача не существует.
     *
     * @param id Идентификатор задачи, которую необходимо удалить.
     * @return ResponseEntity с соответствующим статусом операции.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        // Поиск задачи по идентификатору
        Task task = service.findTaskById(id);

        // Проверка, существует ли задача
        if (task != null) {
            // Удаление задачи, если она найдена
            service.deleteTask(id);
            // Возврат успешного статуса
            return ResponseEntity.ok().build();
        } else {
            // Возврат статуса 'Не найдено', если задача не существует
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Экспортирует задачу в формате ZIP, содержащем файлы задачи и её JSON представление.
     *
     * @param id Идентификатор задачи для экспорта.
     * @param response Объект HttpServletResponse для записи выходных данных.
     */
    @GetMapping("/export/{id}")
    public void exportTask(@PathVariable Long id,
                                             HttpServletResponse response){
        Task task = service.findTaskById(id);
        List<File> files = fileSystemStorage.getFilesByTaskId(id);
        System.out.println("Hello");

        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", "attachment; filename=\"task.zip\"");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())){
            // Создание записи для JSON файла задачи
            ZipEntry jsonEntry = new ZipEntry(task.getTitle() + ".json");
            zippedOut.putNextEntry(jsonEntry);

            // Конвертация задачи в JSON и запись в ZIP
            String jsonContent = service.convertTaskToJson(task, files);
            zippedOut.write(jsonContent.getBytes());
            zippedOut.closeEntry();

            // Обработка и добавление файлов к задач
            for (File fileEntiti: files){
                // Пропускаем PDF файлы, так как они не должны быть включены в экспорт(переводим PDF в фото)
                if(Objects.equals(fileEntiti.getFileType(), "application/pdf")){
                    continue;
                }

                // Добавляем файлы к ZIP
                Path file = Paths.get(fileEntiti.getFilePath());
                ZipEntry entry = new ZipEntry(file.getFileName().toString());
                zippedOut.putNextEntry(entry);

                Files.copy(file, zippedOut);
                zippedOut.closeEntry();
            }
        }catch(IOException exception){
            // Логирование исключения
            exception.printStackTrace();
        }
    }

    /**
     * Обрабатывает запрос на скачивание файла по его идентификатору. Поддерживает обычные файлы и PDF.
     * Для PDF файлов возвращает ZIP архив, для остальных - файл в исходном формате.
     *
     * @param fileId Идентификатор файла для скачивания.
     * @param response Объект HttpServletResponse, используемый для установки заголовков ответа.
     * @return ResponseEntity, содержащий ресурс файла и статус ответа.
     * @throws MalformedURLException В случае ошибки формирования URL для файла.
     */
    @GetMapping("/files/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId, HttpServletResponse response) throws MalformedURLException {
        // Получение сущности файла по идентификатору
        File fileEntity = fileSystemStorage.findFileById(fileId);

        // Проверка типа файла и выбор метода для его скачивания
        if (fileEntity.getFileType().equals("application/pdf")) {
            // Для PDF файлов: конвертация в ZIP перед скачиванием
            return fileSystemStorage.downloadPdfAsZip(fileEntity, response);
        } else {
            // Для всех остальных типов файлов: скачивание в исходном формат
            return fileSystemStorage.downloadRegularFile(fileEntity);
        }
    }

    /**
     * Принимает ZIP-файл, содержащий JSON файл с информацией о задаче и связанные файлы.
     * Извлекает и сохраняет информацию о задаче и файлы в соответствующих сервисах.
     *
     * @param file MultipartFile содержащий ZIP-архив с данными задачи.
     * @return ResponseEntity со статусом обработки и сообщением.
     */
    @PostMapping("/processZip")
    public ResponseEntity<String> processZip(@RequestParam("file") MultipartFile file) {
        try {
            // Инициализация потока чтения ZIP-файла и парсера JSON
            ZipInputStream zipIn = new ZipInputStream(file.getInputStream());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode taskNode = null;
            List<java.io.File> fileList = new ArrayList<>();

            // Обработка каждого элемента в ZIP-файле
            ZipEntry entry;
            Task task = new Task();
            java.io.File savedFile = null;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    // Обработка JSON файла с данными задачи
                    if (entry.getName().endsWith(".json")) {

                        // Чтение и парсинг JSON файла
                        String jsonContent = new String(zipIn.readAllBytes());
                        taskNode = objectMapper.readTree(jsonContent);
                        String title = taskNode.path("title").asText();
                        String description = taskNode.path("description").asText();
                        String categoryName = taskNode.path("category").path("name").asText();
                        String tagName = taskNode.path("tag").path("name").asText();

                        Long categoryId = catService.getIdByName(categoryName);
                        Long tagId = tagService.getIdByName(tagName);

                        task.setTitle(title);
                        task.setDescription(description);

                        Category category = catService.findTaskById(categoryId);
                        Tag tag = tagService.findTaskById(tagId);
                        task.setCategory(category);
                        task.setTag(tag);

                        // Извлечение данных из JSON и их сохранение
                        service.saveTask(task);
                    } else {
                        // Сохранение остальных файлов из ZIP
                        savedFile = fileSystemStorage.saveZipEntryToFile(zipIn, entry.getName());
                        fileList.add(savedFile);
                    }
                }
                zipIn.closeEntry();
            }
            zipIn.close();

            // Обработка и сохранение извлеченных файлов
            for (java.io.File fileZip : fileList) {
                fileSystemStorage.saveFromZipFile(fileZip, task);  // Предполагается, что этот метод адаптирован для работы с java.io.File
                fileZip.delete();  // Удаление временного файла
            }

            return ResponseEntity.ok("Success");
        } catch (IOException e) {
            // Логирование и ответ с ошибкой в случае исключения
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }
}

