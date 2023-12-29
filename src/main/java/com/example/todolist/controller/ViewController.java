package com.example.todolist.controller;

import com.example.todolist.servicetask.ServiceTask;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.todolist.model.Task;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ViewController {

    private final ServiceTask service;

    public ViewController(ServiceTask service) {
        this.service = service;
    }

    /**
     * Обрабатывает запрос на получение страницы со всеми задачами. Извлекает список всех задач из сервиса
     * и добавляет его в модель представления, которая передается в шаблон страницы задач.
     *
     * @param model Модель представления, используемая для передачи данных задач в шаблон.
     * @return Имя шаблона страницы задач, используемое для отображения списка задач.
     */
    @GetMapping("/tasks")
    public String showTasks(Model model){
        // Получение списка всех задач из сервиса
        List<Task> tasks = service.findAllTasks();

        // Добавление списка задач в модель представления
        model.addAttribute("tasks", service.findAllTasks());

        // Возвращение имени шаблона страницы задач для отображения
        return "taskpage";
    }

    /**
     * Обрабатывает запрос на получение страницы задач, отсортированных по дате. Извлекает отсортированный список задач
     * из сервиса и добавляет его в модель представления, которая передается в шаблон страницы задач.
     *
     * @param model Модель представления, используемая для передачи отсортированных данных задач в шаблон.
     * @return Имя шаблона страницы задач, используемое для отображения списка отсортированных задач.
     */
    @GetMapping("/tasks/sortedByDate")
    public String getTasksSortedByDate(Model model) {
        // Получение отсортированного списка задач по дате
        List<Task> tasks = service.findAllTasksSortedByDate();

        // Добавление списка задач в модель представления
        model.addAttribute("tasks", tasks);

        // Возвращение имени шаблона страницы задач для отображения
        return "taskpage";
    }

    /**
     * Обрабатывает запрос на получение страницы задач, отсортированных по названию. Извлекает отсортированный список задач
     * из сервиса и добавляет его в модель представления, которая передается в шаблон страницы задач.
     *
     * @param model Модель представления, используемая для передачи отсортированных данных задач в шаблон.
     * @return Имя шаблона страницы задач, используемое для отображения списка отсортированных задач.
     */
    @GetMapping("/tasks/sortedByTitle")
    public String getTasksSortedByTitle(Model model) {
        // Получение отсортированного списка задач по названию
        List<Task> tasks = service.findAllTasksSortedByTitle();

        // Добавление списка задач в модель представления
        model.addAttribute("tasks", tasks);

        return "taskpage"; // имя вашего шаблона списка задач
    }

    /**
     * Возвращает имя шаблона страницы задач, обновляя модель данными о задачах. Если указан идентификатор тэга,
     * фильтрует задачи по этому тэгу. В противном случае возвращает все задачи. Используется для отображения списка задач
     * с возможностью фильтрации по тэгу.
     *
     * @param model Модель представления для передачи атрибутов в шаблон.
     * @param tagId Опциональный параметр для фильтрации задач по идентификатору тэга.
     * @return Имя шаблона страницы с задачами для отображения.
     */
    @GetMapping("/tasks/filterByTagId")
    public String getTasksFilteredAndSorted(Model model,
                                            @RequestParam(value = "tagId", required = false) Long tagId) {

        // Получение и фильтрация задач по идентификатору тэга
        List<Task> tasks;
        if (tagId != null) {
            tasks = service.findTasksByTagId(tagId); // Метод для получения задач по ID тэга
        } else {
            tasks = service.findAllTasks(); // Получение всех задач, если тэг не указан
        }

        // Добавление списка задач в модель представления
        model.addAttribute("tasks", tasks);

        // Возвращение имени шаблона страницы задач для отображения
        return "taskpage"; // имя вашего шаблона списка задач
    }

}
