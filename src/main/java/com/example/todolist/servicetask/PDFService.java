package com.example.todolist.servicetask;

import com.example.todolist.repository.FileRepository;
import com.example.todolist.repository.Repository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import com.example.todolist.model.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PDFService {


    /**
     * Конвертирует каждую страницу PDF-документа, полученного из входного потока, в изображение формата PNG.
     * Сохраняет изображения в заданной директории с базовым именем файла и маркировкой страницы. Возвращает
     * список путей к созданным изображениям.
     *
     * @param pdfInputStream Входной поток PDF-документа для конвертации.
     * @param outputDir Директория, в которую будут сохранены изображения.
     * @param baseName Базовое имя для сохраняемых изображений (обычно имя исходного PDF-файла).
     * @return Список путей к созданным изображениям.
     */
    public List<Path> convertPDFToImages(InputStream pdfInputStream, Path outputDir, String baseName){
        // Список для хранения путей созданных изображений
        List<Path> imagePaths = new ArrayList<>();


        try(PDDocument document = PDDocument.load(pdfInputStream)){
            // Инициализация рендерера для PDF
            PDFRenderer renderer = new PDFRenderer(document);

            // Перебор всех страниц PDF-документа
            for(int page = 0; page < document.getNumberOfPages(); ++page){
                BufferedImage image = renderer.renderImageWithDPI(page, 300);

                // Определение пути сохранения изображения
                Path imagePath = outputDir.resolve(baseName + "-page-" + page + ".png");

                // Сохранение изображения на диск
                ImageIO.write(image, "PNG", imagePath.toFile());

                // Добавление пути в список
                imagePaths.add(imagePath);
                }
        }catch(Exception exception){
            // Логирование в случае возникновения исключений при обработке PDF
                exception.printStackTrace();
        }
            return imagePaths;
    }

}
