import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {
    static void writeToDotFile(String filePath, String content) {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            // Создаем объект файла для записи
            fileWriter = new FileWriter(filePath);
            bufferedWriter = new BufferedWriter(fileWriter);

            // Записываем содержимое в файл
            bufferedWriter.write(content);
            System.out.println("Граф успешно записан в dot файл. Путь до него: " + filePath);
        } catch (IOException e) {
            // Обработка ошибок ввода/вывода
            e.printStackTrace();
        } finally {
            try {
                // Закрытие BufferedWriter и FileWriter
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
