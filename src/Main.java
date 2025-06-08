import uimodels.Explanation;
import uimodels.GenerateParameters;
import uimodels.TaskType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class Main {

    static JSlider vminSlider;
    static JSlider vmaxSlider;
    static JTextField vminValueField;
    static JTextField vmaxValueField;
    static JCheckBox answerCheck;
    static JPanel explanationPanel;
    static JTextField latexPathField;
    static JComboBox<TaskType> taskToggle;
    static JFrame frame;
    static JTextField optionsField;
    static JComboBox<Explanation> explanationCombo;

    private static boolean isGuiMode;

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("-gui")) {
            isGuiMode = true;
            runGui();
            return;
        }

        if (args.length == 4 && args[0].equals("-p") && args[2].equals("-o")) {
            isGuiMode = false;
            String propertiesPath = args[1];
            String outputPath = args[3];
            runCliMode(propertiesPath, outputPath);
            return;
        }

        printUsage();

    }

    private static void printUsage() {
        System.out.println("Использование генератора:");
        System.out.println("  java -cp \"myapp.jar;libs/*\" Main  -gui");
        System.out.println("  (запуск графического интерфейса)");
        System.out.println("   java -cp \"myapp.jar;libs/*\" Main -p <путь к properties-файлу> -o <путь к выходному файлу>");
        System.out.println("  (режим командной строки)");
    }

    private static void runCliMode(String propertiesPath, String outputPath) {

        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(propertiesPath)) {
            props.load(fis);

            int n = Integer.parseInt(props.getProperty("n"));
            int vMin = Integer.parseInt(props.getProperty("v_min"));
            int vMax = Integer.parseInt(props.getProperty("v_max"));
            boolean generateAnswer = Boolean.parseBoolean(props.getProperty("generate_answer"));
            String pathToCompiler = props.getProperty("path_to_compiler");
            Explanation explanation = Explanation.valueOf(props.getProperty("explanation").toUpperCase());
            TaskType taskType = TaskType.valueOf(props.getProperty("task_type").toUpperCase());

            generate(
                    new GenerateParameters(
                            n,
                            vMin,
                            vMax,
                            generateAnswer,
                            explanation,
                            pathToCompiler,
                            taskType
                    ),
                    outputPath
            );
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Ошибка преобразования числа: " + e.getMessage());
        }
    }

    private static void runGui() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("GraphGenerator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(700, 600);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            // Панель Vmin и Vmax с текстовыми полями для отображения значения
            JPanel vPanel = new JPanel(new GridBagLayout());
            vPanel.setBorder(BorderFactory.createTitledBorder("Vertex Count"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Vmin
            gbc.gridx = 0;
            gbc.gridy = 0;
            vminValueField = new JTextField(String.valueOf(SettingsCache.getVmin()));
            vminValueField.setEditable(false);
            vminValueField.setPreferredSize(new Dimension(30, 25));
            vPanel.add(vminValueField, gbc);

            gbc.gridx = 1;
            vPanel.add(new JLabel("Vmin (number of vertices):"), gbc);

            gbc.gridx = 2;
            vminSlider = new JSlider(2, 20, SettingsCache.getVmin());
            vminSlider.setMajorTickSpacing(2);
            vminSlider.setPaintTicks(true);
            vminSlider.setPaintLabels(true);
            vPanel.add(vminSlider, gbc);

            // Vmax
            gbc.gridx = 0;
            gbc.gridy = 1;
            vmaxValueField = new JTextField(String.valueOf(SettingsCache.getVmax()));
            vmaxValueField.setEditable(false);
            vminValueField.setPreferredSize(new Dimension(30, 25));
            vPanel.add(vmaxValueField, gbc);

            gbc.gridx = 1;
            vPanel.add(new JLabel("Vmax (number of vertices):"), gbc);

            gbc.gridx = 2;
            vmaxSlider = new JSlider(2, 25, SettingsCache.getVmax());
            vmaxSlider.setMajorTickSpacing(5);
            vmaxSlider.setPaintTicks(true);
            vmaxSlider.setPaintLabels(true);
            vPanel.add(vmaxSlider, gbc);

            // Логика слайдеров
            vminSlider.addChangeListener(e -> {
                int vmin = vminSlider.getValue();
                vminValueField.setText(String.valueOf(vmin));
                SettingsCache.saveVmin(vmin);
                if (vmaxSlider.getValue() < vmin) {
                    vmaxSlider.setValue(vmin);
                }
            });

            vmaxSlider.addChangeListener(e -> {
                int vmax = vmaxSlider.getValue();
                vmaxValueField.setText(String.valueOf(vmax));
                SettingsCache.saveVmax(vmax);
                if (vmax < vminSlider.getValue()) {
                    vminSlider.setValue(vmax);
                }
            });

            // Чекбоксы
            answerCheck = new JCheckBox("Generate Answer");
            answerCheck.setSelected(SettingsCache.getGenAnswer());
            answerCheck.addActionListener(e -> {
                SettingsCache.setGenAnswer(answerCheck.isSelected());
            });

            explanationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            explanationPanel.setBorder(BorderFactory.createTitledBorder("uimodels.Explanation Detail"));

            explanationCombo = new JComboBox<>(Explanation.values());
            explanationCombo.setSelectedItem(SettingsCache.getGenExplanation());
            explanationCombo.addActionListener(e -> {
                Explanation selected = (Explanation) explanationCombo.getSelectedItem();
                if (selected != null) {
                    SettingsCache.setGenExplanation(selected);
                }
            });

            explanationPanel.add(new JLabel("uimodels.Explanation level:"));
            explanationPanel.add(explanationCombo);
            mainPanel.add(explanationPanel);

            // Панель выбора LaTeX компилятора (кнопка + некликабельное поле)
            JPanel latexPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            latexPanel.setBorder(BorderFactory.createTitledBorder("LaTeX Compiler Path"));

            JButton browseButton = new JButton("Browse");
            browseButton.setPreferredSize(new Dimension(100, 25)); // фиксированный размер кнопки

            latexPathField = new JTextField();
            latexPathField.setEditable(false);
            latexPathField.setPreferredSize(new Dimension(400, 25)); // фиксированная ширина текстового поля
            latexPathField.setText(SettingsCache.getLatexPath());

            browseButton.addActionListener(e -> {
                openFileManager();
            });

            latexPanel.add(browseButton, BorderLayout.WEST);
            latexPanel.add(latexPathField, BorderLayout.CENTER);

            // SCC / Metagraph toggle
            JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            togglePanel.setBorder(BorderFactory.createTitledBorder("Task Type"));
            togglePanel.add(new JLabel("Select:"));
            taskToggle = new JComboBox<>(TaskType.values());
            taskToggle.setSelectedItem(SettingsCache.getTaskType());
            togglePanel.add(taskToggle);
            taskToggle.addActionListener(e -> {
                SettingsCache.setTaskType((TaskType) taskToggle.getSelectedItem());
            });

            // Кнопка Generate
            JButton generateButton = new JButton("Generate");
            generateButton.addActionListener(e -> onGenerate());

            // Панель для числового ввода количества вариантов ответа
            JPanel optionsCountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            optionsCountPanel.setBorder(BorderFactory.createTitledBorder("Answer Options Count"));

            JLabel optionsLabel = new JLabel("Number of answer options (integer from 2 to 2^31 - 1):");
            optionsField = new JTextField(10); // ширина поля
            optionsField.setText(String.valueOf(SettingsCache.getN()));

            optionsCountPanel.add(optionsLabel);
            optionsCountPanel.add(optionsField);

            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            mainPanel.add(optionsCountPanel);

            // Собираем всё в mainPanel
            mainPanel.add(vPanel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            mainPanel.add(answerCheck);
            mainPanel.add(explanationPanel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            mainPanel.add(latexPanel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            mainPanel.add(togglePanel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            mainPanel.add(generateButton);

            frame.setContentPane(mainPanel);
            frame.setVisible(true);
        });
    }

    private static void openFileManager() {
        String currentPath = SettingsCache.getLatexPath();
        JFileChooser fileChooser;

        if (currentPath != null && !currentPath.isEmpty()) {
            File currentDir = new File(currentPath);
            if (currentDir.exists()) {
                if (currentDir.isFile()) {
                    // Если путь указывает на файл, используем его родительскую папку
                    currentDir = currentDir.getParentFile();
                }
                fileChooser = new JFileChooser(currentDir);
            } else {
                fileChooser = new JFileChooser();
            }
        } else {
            fileChooser = new JFileChooser();
        }

        fileChooser.setDialogTitle("Select pdfLaTeX Compiler (.exe)");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".exe");
            }

            @Override
            public String getDescription() {
                return "Executable Files (*.exe)";
            }
        });

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            updateLatexPathField(selectedFile);
        }
    }

    private static void updateLatexPathField(File selectedFile) {
        latexPathField.setText(selectedFile.getAbsolutePath());
        SettingsCache.saveLatexPath(selectedFile.getAbsolutePath());
    }

    private static void onGenerate() {
        // ✅ Получаем значения
        int vmin = vminSlider.getValue();
        int vmax = vmaxSlider.getValue();
        boolean generateAnswer = answerCheck.isSelected();
        Explanation explanation = (Explanation) explanationCombo.getSelectedItem();
        String latexPath = latexPathField.getText();
        TaskType taskType = (TaskType) taskToggle.getSelectedItem();

        logParameters(vmin, vmax, generateAnswer, explanation, latexPath, taskType);

        int n = validateNwithGui();
        if (n == -1) return;

        generate(
                new GenerateParameters(
                        n,
                        vmin,
                        vmax,
                        generateAnswer,
                        explanation,
                        latexPath,
                        taskType
                ),
                Globals.outputFilePath
        );
    }

    private static void generate(GenerateParameters parameters, String outputFilePath) {
        GenerateManager.generate(parameters, outputFilePath);

        //Компилируем и открываем latex файл

        LatexCompiler.compileLatex(parameters.latexPath(), outputFilePath);

        if (isGuiMode) {
            openGeneratedPdf(outputFilePath);
        } else {
            System.out.println("PDF сгенерирован: " + outputFilePath.replaceAll("\\.[^.]+$", ".pdf"));
        }
    }

    private static void logParameters(int vmin, int vmax, boolean generateAnswer, Explanation explanation, String latexPath, TaskType taskType) {
        System.out.println("Vmin: " + vmin);
        System.out.println("Vmax: " + vmax);
        System.out.println("Generate Answer: " + generateAnswer);
        System.out.println("Generate uimodels.Explanation: " + explanation);
        System.out.println("LaTeX Path: " + latexPath);
        System.out.println("Task Type: " + taskType);
    }

    private static void openGeneratedPdf(String outputFilePath) {
        File pdf = new File(outputFilePath.replaceAll("\\.[^.]+$", ".pdf"));
        if (pdf.exists()) {
            try {
                Desktop.getDesktop().open(pdf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int validateNwithGui() {
        String text = optionsField.getText().trim();
        int value;
        try {
            value = Integer.parseUnsignedInt(text);
            if (value < 2) {
                throw new NumberFormatException("Value must be ≥ 2");
            }
            SettingsCache.setN(value);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame,
                    "Please enter a valid integer from 2 to " + Integer.MAX_VALUE,
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        return value;
    }
}
