import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class LatexCompiler {
    public static void compileLatex(String pdflatexPath, String texFilePath) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    pdflatexPath, // путь к pdflatex.exe
                    "-interaction=nonstopmode", // чтобы не было зависаний
                    texFilePath   // путь к .tex файлу
            );

            // Установим рабочую директорию (где лежит .tex)
            File texFile = new File(texFilePath);
            builder.directory(texFile.getParentFile());

            builder.redirectErrorStream(true); // объединить stdout и stderr
            Process process = builder.start();

            // Читаем вывод процесса
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line); // выводим, можно логировать
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("✅ Compilation successful!");
            } else {
                System.out.println("❌ Compilation failed with exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}