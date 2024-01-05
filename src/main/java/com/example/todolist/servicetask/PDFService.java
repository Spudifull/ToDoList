package com.example.todolist.servicetask;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.graphics.PdfImageType;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


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
    public List<Path> convertPDFToImages(InputStream pdfInputStream, Path outputDir, String baseName) throws IOException {
        // Список для хранения путей созданных изображений
        List<Path> imagePaths = new ArrayList<>();

        // Создание временного файла из InputStream
        java.io.File tempPdfFile = java.io.File.createTempFile("pdf_to_convert", ".pdf");
        tempPdfFile.deleteOnExit();

        try (FileOutputStream out = new FileOutputStream(tempPdfFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = pdfInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Загрузка PDF документа из временного файла
        PdfDocument pdf = new PdfDocument();
        pdf.loadFromFile(tempPdfFile.getAbsolutePath());

        // Конвертация каждой страницы в изображение
        for (int i = 0; i < pdf.getPages().getCount(); i++) {
            BufferedImage image = pdf.saveAsImage(i, PdfImageType.Bitmap, 500, 500);

            // Определение пути и имени файла
            Path imagePath = outputDir.resolve(String.format(baseName + "-page-%d.png", i));

            // Сохранение изображения
            ImageIO.write(image, "PNG", imagePath.toFile());
            imagePaths.add(imagePath);
        }

        // Освобождение ресурсов PDF документа
        pdf.close();

        return imagePaths;
    }

}
