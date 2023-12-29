package com.example.todolist.controller;

import com.example.todolist.model.Category;
import com.example.todolist.model.Tag;
import com.example.todolist.servicetask.ServiceCategory;
import com.example.todolist.servicetask.ServiceTag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cat/")
public class TagAndCategoryController {

    private final ServiceCategory serviceCategory;

    private final ServiceTag serviceTag;

    public TagAndCategoryController(ServiceCategory serviceCategory, ServiceTag serviceTag){
        this.serviceCategory = serviceCategory;
        this.serviceTag = serviceTag;
    }

    /**
     * Создает новую категорию с указанным именем и описанием. Возвращает созданную категорию с ее идентификатором,
     * если операция прошла успешно. В случае неудачи возвращает статус ошибки 'Bad Request'.
     *
     * @param name Имя новой категории.
     * @param description Описание новой категории.
     * @return ResponseEntity с созданной категорией и статусом ответа.
     */
    @PostMapping("/category")
    public ResponseEntity<Category> createCategory(@RequestParam String name,
                                                   @RequestParam String description){
        // Создание экземпляра новой категории и установка ее свойств
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        // Сохранение категории и получение экземпляра с идентификатором
        Category newCategory = this.serviceCategory.saveCategory(category);

        // Проверка, была ли категория успешно создана и сохранена
        if (newCategory != null && newCategory.getId() != null) {

            // Возврат успешного ответа с созданной категорией
            return ResponseEntity.ok(newCategory);
        } else {

            // Возврат статуса ошибки, если создание категории не удалось
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Создает новый тэг с указанным именем и описанием. Возвращает созданный тэг с его идентификатором,
     * если операция прошла успешно. В случае неудачи возвращает статус ошибки 'Bad Request'.
     *
     * @param name Имя нового тэга.
     * @param description Описание нового тэга.
     * @return ResponseEntity с созданным тэгом и статусом ответа.
     */
    @PostMapping("/tag")
    public ResponseEntity<Tag> createTag(@RequestParam String name,
                                              @RequestParam String description){
        // Инициализация нового объекта тэга
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);

        // Сохранение тэга и получение экземпляра с идентификатором
        Tag newTag = this.serviceTag.saveTag(tag);

        // Проверка, был ли тэг успешно создан и сохранен
        if (newTag != null && newTag.getId() != null) {
            // Возврат успешного ответа с созданным тэгом
            return ResponseEntity.ok(newTag);
        } else {

            // Возврат статуса ошибки, если создание тэга не удалось
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Удаляет категорию по заданному идентификатору. Если категория с таким идентификатором найдена и успешно удалена,
     * возвращает статус OK. Если категория не найдена, возвращает статус 'Not Found'.
     *
     * @param id Идентификатор категории, которую нужно удалить.
     * @return ResponseEntity без содержимого, но с соответствующим статусом ответа.
     */
    @DeleteMapping("/deleteCategory/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        // Поиск категории по идентификатору
        Category category = serviceCategory.findTaskById(id);
        if (category != null) {
            // Удаление категории, если она найдена
            serviceCategory.deleteTask(id);

            // Возврат успешного статуса
            return ResponseEntity.ok().build();
        } else {

            // Возврат статуса 'Не найдено', если категория не существует
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Удаляет тэг по заданному идентификатору. Если тэг с таким идентификатором найден и успешно удален,
     * возвращает статус OK. Если тэг не найден, возвращает статус 'Not Found'.
     *
     * @param id Идентификатор тэга, который нужно удалить.
     * @return ResponseEntity без содержимого, но с соответствующим статусом ответа.
     */
    @DeleteMapping("/deleteTask/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        // Поиск тэга по идентификатору
        Tag tag = serviceTag.findTaskById(id);
        if (tag != null) {
            // Удаление тэга, если он найден
            serviceTag.deleteTask(id);

            // Возврат успешного статуса
            return ResponseEntity.ok().build();
        } else {
            // Возврат статуса 'Не найдено', если тэг не существует
            return ResponseEntity.notFound().build();
        }
    }
}
