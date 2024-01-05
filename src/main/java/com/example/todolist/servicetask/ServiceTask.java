package com.example.todolist.servicetask;

import com.example.todolist.model.File;
import com.example.todolist.model.Task;
import com.example.todolist.repository.Repository;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;


@Service
public class ServiceTask {
    private final Repository repository;

    public ServiceTask(Repository repository){
        this.repository = repository;
    }

    public List<Task> findAllTasks(){
        return repository.findAll();
    }

    public Task findTaskById(Long id){
        return repository.findById(id).orElse(null);
    }

    public List<Task> findTasksByTitle(String title) {
        return repository.findByTitle(title);
    }


    public Task saveTask(Task task) {
        return repository.save(task);
    }

    public void deleteTask(Long id) {
        repository.deleteById(id);
    }

    public List<Task> findAllTasksSortedByTitle() {
        return repository.findAllByOrderByTitleAsc();
    }

    public List<Task> findAllTasksSortedByDate() {
        return repository.findAllByOrderByCreationDate();
    }

    /**
     * Конвертирует объект задачи и связанные с ней файлы в отформатированную строку JSON.
     * Включает информацию о задаче, ее категории, тэге и связанных файлах PDF. Опционально настраивает
     * кодирование не-ASCII символов в JSON и обрабатывает исключения при конвертации.
     *
     * @param task Объект задачи для конвертации в JSON.
     * @param files Список файлов, связанных с задачей.
     * @return Отформатированная строка JSON, представляющая задачу и ее файлы.
     * @throws RuntimeException Если происходит ошибка при конвертации в JSON.
     */
    public String convertTaskToJson(Task task, List<File> files){

        ObjectMapper mapper = new ObjectMapper();

        // Конфигурация для обработки не-ASCII символов, если требуется
        mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false); //не уверен, что эта строка нужна, но пока оставил

        // Создание корневого узла JSON
        ObjectNode rootNode = mapper.createObjectNode();

        // Добавление информации о задаче
        rootNode.put("id", task.getId());
        rootNode.put("title", task.getTitle());
        rootNode.put("description", task.getDescription());
        rootNode.put("creationDate", task.getCreationDate().toString());

        // Добавление информации о категории задачи
        ObjectNode categoryNode = rootNode.putObject("category");
        categoryNode.put("name", task.getCategory().getName());
        categoryNode.put("description", task.getCategory().getDescription());

        // Добавление информации о тэге задачи
        ObjectNode tagNode = rootNode.putObject("tag");
        tagNode.put("name", task.getTag().getName());
        tagNode.put("description", task.getTag().getDescription());

        // Обработка связанных файлов PDF и добавление информации о страницах
        if (!files.isEmpty() && Objects.equals(files.getFirst().getFileType(), "application/pdf")){
            ArrayNode pdf = mapper.createArrayNode();

            files.forEach(file -> {
                ObjectNode fileNode = pdf.addObject();
                fileNode.put("namePage", Paths.get(file.getFilePath()).getFileName().toString());
            });

            rootNode.set("pdfPages", pdf);
        }

        // Конвертация и возврат JSON строки
        try{
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        }catch (Exception exception){

            // Обработка исключений при конвертации в JSON
            throw new RuntimeException("Error to convert json", exception);
        }
    }

    public List<Task> findTasksByTagId(Long tagId) {
        return repository.findByTagId(tagId);
    }
}
