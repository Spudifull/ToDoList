package com.example.todolist.servicetask;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import javax.annotation.PostConstruct;

import com.example.todolist.config.StorageProperties;
import com.example.todolist.model.File;
import com.example.todolist.model.Task;
import com.example.todolist.repository.FileRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.example.todolist.exception.FileNotFoundException;
import com.example.todolist.exception.FileStorageException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class FileSystemStorageService implements FileSystemStorage {
    private final Path dirlocation;
    private final FileRepository fileRepository;

    private final PDFService pdfService;

    public FileSystemStorageService(StorageProperties storageProperties,
                                    FileRepository fileRepository, PDFService pdfService) {
        this.dirlocation = Paths.get(storageProperties.getLocation())
                .toAbsolutePath()
                .normalize();
        this.fileRepository = fileRepository;
        this.pdfService = pdfService;
    }

    @Override
    @PostConstruct
    public void init(){
        try{
            Files.createDirectories(this.dirlocation);
        }
        catch(Exception exception){
            throw new FileStorageException("Could not create upload dir!");
        }
    }

    /**
     * Сохраняет переданный файл и связывает его с заданной задачей. Если файл является PDF, конвертирует его страницы
     * в изображения и сохраняет их как отдельные файлы. Все файлы регистрируются в репозитории файлов с ссылкой на их задачу.
     * Возвращает имя сохраненного файла.
     *
     * @param file Мультипарт-файл, полученный от клиента.
     * @param task Задача, с которой будет связан файл.
     * @return Имя сохраненного файла.
     * @throws FileStorageException Если файл не может быть сохранен.
     */
    @Override
    public String saveFile(MultipartFile file, Task task){
        try{
            // Очистка и получение имени файла, создание пути к файлу
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            Path dirfile = this.dirlocation.resolve(Objects.requireNonNull(fileName));

            // Создание сущности файла и сохранение его свойств
            File fileEntity = new File();
            fileEntity.setFilePath(dirfile.toString());
            fileEntity.setFileType(file.getContentType());
            fileEntity.setTask(task);


            fileRepository.save(fileEntity);

            // Получение входного потока файла
            InputStream inputStream = file.getInputStream();
            if(Objects.equals(fileEntity.getFileType(), "application/pdf")){
                String baseName = dirfile.toFile().getName().replaceFirst("[.][^.]+$", "");

                // Конвертация PDF в изображения, если файл является PDF
                List<Path> imagePaths = pdfService.convertPDFToImages(inputStream, this.dirlocation,baseName);

                // Сохранение каждого изображения как нового файла
                for (Path imagePath : imagePaths){
                    File imageEntity = new File();
                    imageEntity.setFilePath(imagePath.toString());
                    imageEntity.setFileType("image/png");
                    imageEntity.setTask(task);
                    imageEntity.setOriginalFieldID(fileEntity.getId());

                    fileRepository.save(imageEntity);
                }
            }
            else {

                // Прямое копирование файла, если он не является PDF
                Files.copy(file.getInputStream(), dirfile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Возврат имени сохраненного файл
            return fileName;
        } catch(Exception exception) {

            // Выброс исключения при возникновении ошибки сохранения файла
            throw new FileStorageException("Could not upload file");
        }
    }

    /**
     * Загружает файл с заданным именем из директории хранения и возвращает его как ресурс.
     * Проверяет, существует ли файл и доступен ли он для чтения. В случае успеха возвращает ресурс файла.
     * Если файл не найден или не может быть прочитан, выбрасывает исключение FileNotFoundException.
     *
     * @param fileName Имя файла для загрузки.
     * @return Ресурс файла, готовый для чтения или скачивания.
     * @throws FileNotFoundException Если файл не найден или не может быть прочитан.
     */
    @Override
    public Resource loadFile(String fileName){
        try{
            // Создание пути к файлу и нормализация этого пути
            Path file = this.dirlocation.resolve(fileName).normalize();

            // Создание ресурса URL из пути файла
            Resource resource = new UrlResource(file.toUri());

            // Проверка, существует ли файл и доступен ли он для чтения
            if(resource.exists() || resource.isReadable()){

                // Возврат ресурса файла, если он доступен
                return resource;
            }

            else{
                // Выброс исключения, если файл не найден или не доступен для чтения
                throw new FileNotFoundException("Could not find file");
            }
        }
        catch(MalformedURLException exception){

            // Выброс исключения, если путь не может быть преобразован в URL
            throw new FileNotFoundException("Could not download file");
        }
    }

    public List<File> getFilesByTaskId(Long taskId){
        return fileRepository.findByTaskId(taskId);
    }

    public File findFileById(Long id)
    {
        return fileRepository.findById(id).orElse(null);
    }

    /**
     * Создает и возвращает ResponseEntity с ресурсом файла для загрузки. Устанавливает соответствующие заголовки
     * для обеспечения того, чтобы клиент смог корректно скачать и сохранить файл. Подходит для загрузки
     * обычных файлов, таких как текстовые файлы, изображения и другие, не требующие специальной обработки, в отличие от PDF.
     *
     * @param fileEntity Сущность файла, содержащая информацию о пути и типе файла.
     * @return ResponseEntity, содержащий ресурс файла и заголовки для его загрузки.
     * @throws MalformedURLException Если путь файла не может быть преобразован в URL.
     */
    public ResponseEntity<Resource> downloadRegularFile(File fileEntity) throws MalformedURLException {
        // Преобразование пути файла в Path и создание ресурса URL из него
        Path path = Paths.get(fileEntity.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        // Создание и возврат ResponseEntity с ресурсом файла, типом контента и заголовками для загрузки
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Извлекает все файлы, связанные с заданным идентификатором PDF-файла, и фильтрует их,
     * чтобы вернуть только изображения. Используется для получения изображений, которые были
     * конвертированы из страниц данного PDF-файла.
     *
     * @param pdfId Идентификатор PDF-файла, для которого нужно найти связанные изображения.
     * @return Список файлов изображений, связанных с указанным PDF-файлом.
     */
    public List<File> getImagesForPdf(Long pdfId) {
        // Получение всех файлов, связанных с указанным PDF-файлом
        List<File> relatedFiles = fileRepository.findByOriginalFieldID(pdfId);

        // Фильтрация и возврат только изображений, исключая сам PDF-файл
        return relatedFiles.stream()
                .filter(file -> !file.getFileType().equals("application/pdf"))
                .collect(Collectors.toList());
    }

    /**
     * Создает и отправляет клиенту ZIP-архив, содержащий изображения, связанные с указанным PDF файлом.
     * Устанавливает соответствующие заголовки ответа для загрузки файла и обрабатывает поток данных в ZIP-формат.
     *
     * @param pdfFile Объект файла PDF, для которого нужно получить связанные изображения.
     * @param response HttpServletResponse, используемый для установки статуса и заголовков ответа.
     * @return ResponseEntity, представляющий результат операции: успешное создание или внутреннюю ошибку сервера.
     */
    public ResponseEntity<Resource> downloadPdfAsZip(File pdfFile, HttpServletResponse response) {
        // Получение изображений, связанных с PDF файлом
        List<File> imageFiles = this.getImagesForPdf(pdfFile.getId());
        Path pdfPath = Paths.get(pdfFile.getFilePath());

        // Определение имени ZIP-файла на основе имени PDF файла
        String zipFilename = "converted-" +  pdfPath.getFileName().toString() + ".zip";

        // Установка HTTP ответа для загрузки файла
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", "attachment; filename=\"" + zipFilename + "\"");

        try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {

            // Добавление каждого изображения в ZIP-архив
            for (File imageFile : imageFiles) {
                Path imagePath = Paths.get(imageFile.getFilePath());
                ZipEntry imageEntry = new ZipEntry(imagePath.getFileName().toString());
                zippedOut.putNextEntry(imageEntry);
                Files.copy(imagePath, zippedOut);
                zippedOut.closeEntry();
            }
        } catch (IOException e) {
            // Логирование и возврат внутренней ошибки сервера в случае исключения
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }

        // Возврат успешного статуса после создания и отправки ZIP-файла
        return ResponseEntity.ok().build();
    }

    /**
     * Извлекает файл из потока ZipInputStream и сохраняет его во временный файл на диске.
     * Создает директорию для хранения, если она не существует, и записывает данные файла из ZIP-архива.
     *
     * @param zipIn Поток ZipInputStream, из которого извлекается файл.
     * @param fileName Имя файла для сохранения из ZIP-архива.
     * @return Временный файл, содержащий данные из ZIP-архива.
     * @throws IOException Если возникнут ошибки при чтении из потока или записи в файл.
     */
    public java.io.File saveZipEntryToFile(ZipInputStream zipIn, String fileName) throws IOException {
        // Создание директории для временных файлов, если она не существует
        java.io.File tempDir = new java.io.File(String.valueOf(this.dirlocation));
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // Создание временного файла для сохранения данных из ZIP-архива
        java.io.File tempFile = new java.io.File(tempDir, fileName);

        // Чтение данных из ZipInputStream и их запись во временный файл
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = zipIn.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }

        // Возврат ссылки на созданный временный файл
        return tempFile;
    }

    /**
     * Сохраняет файл из ZIP-архива и связывает его с задачей. Если файл является PDF, конвертирует его страницы
     * в изображения и также сохраняет их как связанные файлы. Устанавливает тип файла и связывает его с задачей
     * в базе данных через репозиторий.
     *
     * @param file Файл из ZIP-архива для сохранения.
     * @param task Задача, с которой будет связан файл.
     * @throws FileStorageException Если произойдет ошибка при сохранении файла или его конвертации.
     */
    public void saveFromZipFile(java.io.File file, Task task) {
        try {
            // Определение имени и пути для сохраняемого файла
            String fileName = file.getName();
            Path dirfile = this.dirlocation.resolve(fileName);

            // Создание и сохранение основной сущности файла
            File fileEntity = new File();
            fileEntity.setFilePath(dirfile.toString());
            fileEntity.setFileType(Files.probeContentType(dirfile));
            fileEntity.setTask(task);

            fileRepository.save(fileEntity);

            try (InputStream inputStream = new FileInputStream(file)) {
                if (Objects.equals(fileEntity.getFileType(), "application/pdf")) {
                    // Конвертация PDF в изображения и сохранение каждого из них
                    List<Path> imagePaths = pdfService.convertPDFToImages(inputStream, this.dirlocation,fileName);

                    for (Path imagePath : imagePaths){
                        File imageEntity = new File();
                        imageEntity.setFilePath(imagePath.toString());
                        imageEntity.setFileType("image/png");
                        imageEntity.setTask(task);
                        imageEntity.setOriginalFieldID(fileEntity.getId());

                        fileRepository.save(imageEntity);
                    }
                } else {
                    // Копирование файла в целевую директорию, если это не PDF
                    Files.copy(inputStream, dirfile, StandardCopyOption.REPLACE_EXISTING);
                }
            }

        } catch (Exception exception) {
            // Логирование и выброс исключения в случае ошибок
            throw new FileStorageException("Could not upload file: " + exception.getMessage());
        }
    }
}
