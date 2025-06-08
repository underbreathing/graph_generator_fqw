import uimodels.DetailedExplanationInfo;
import uimodels.TaskType;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class LatexGenerator {

    private static String filePath;

    public static void init(String stringPath) {
        LatexGenerator.filePath = stringPath;
    }


    public static void generateStart() {
        generateInfo("""
                \\documentclass{article}
                \\usepackage[T2A]{fontenc} % Поддержка Кириллицы
                \\usepackage[utf8]{inputenc} % Поддержка utf8
                \\usepackage{tikz}
                \\usetikzlibrary{snakes,arrows,shapes}
                \\usepackage{amsmath}
                \\usepackage[russian]{babel} % Поддержка русского языка
                \\usepackage{paralist}
                \\usetikzlibrary{fit}
                \\usepackage[export]{adjustbox}

                \\begin{document}
                \\pagestyle{empty}
                                
                """, false);
        // можно добавить \enlargethispage{100cm} чтобы попытаться уместить граф на той же странице.
    }

    public static void generateEnd() {
        generateInfo("""
                                    
                \\end{document}
                """, true);
    }

    public static void generateTask(TaskType taskType, List<Integer>[] adjList, int answer, int n) {
        String question;
        String sAnswer = "";

        int nCopy = n;
        StringBuilder answerVariants = new StringBuilder();
        answerVariants.append("\\noindent\\begin{inparaenum}[(1)]\n");
        answerVariants.append("\\item ").append(--nCopy).append(" или более;\n");
        while (nCopy > 0) {
            answerVariants.append(" \\item ").append(--nCopy).append(";");
        }
        answerVariants.append("\\end{inparaenum}\\\\\n");

        if (answer >= 0) {
            int answerOption;
            if (answer >= n - 1) {
                answerOption = 1;
            } else {
                answerOption = n - answer;
            }
            sAnswer = String.format("\n\n\\noindent\\textbf{Ответ:} (%d).\\\\\n", answerOption);
        }

        switch (taskType) {
            case SCC -> question = "Сколько всего сильно связных компонент в орграфе $G$?";
            case METAGRAPH -> question = "Сколько всего ребер в метаграфе орграфа $G$?";
            default -> throw new IllegalArgumentException("Неизвестный тип задания: " + taskType);
        }

        StringBuilder sVertices = new StringBuilder();
        StringBuilder edges = new StringBuilder();


        for (int i = 0; i < adjList.length; i++) {
            sVertices.append(i + 1).append(", ");
            for (int j = 0; j < adjList[i].size(); j++) {
                edges.append("(").append(i + 1).append(", ").append(adjList[i].get(j) + 1).append(")").append(", ");
            }
        }
        Utils.cleanTheTip(sVertices);
        Utils.cleanTheTip(edges);

        String taskText = String.format("""
                %%Начало задания
                \\noindent\\textbf{Задание.} Дан ориентированный граф $G = (V, E)$, где
                $V$ = \\{%s\\} и $E$ = \\{%s\\}.\\\\
                %s
                \\vspace{2mm}
                                
                %s
                %s
                %%Конец задания
                """, sVertices, edges, question, answerVariants, sAnswer);

        generateInfo(taskText);
    }

    public static void generateInfo(String info) {
        generateInfo(info, true);
    }

    private static void generateInfo(String info, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(filePath, append), StandardCharsets.UTF_8))) {
            writer.write(info);
            writer.newLine(); // добавим перевод строки после текста
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    public static void generateShortExplanation(TaskType taskType, Graph myGraph, List<List<Integer>> sccs, MetaGraph metaGraph) {
        StringBuilder explanation = new StringBuilder();

        explanation.append("""
                \\noindent\\textbf{Краткое обоснование.} Исходный орграф $G$:                
                \\vspace{8mm}
                                
                """);
        explanation.append(LatexGraphDrawer.generateGraph(myGraph, sccs));

        switch (taskType) {

            case SCC -> {
                explanation.append(String.format("""
                                                
                        \\vspace{8mm}
                                                
                        В орграфе $G$ ровно %d ССК:
                        \\begin{enumerate}
                        """, sccs.size()));
                sccs.forEach(scc -> {
                    explanation.append("\\item $\\langle ");
                    scc.forEach(vertex -> {
                        explanation.append(vertex).append(", ");
                    });
                    Utils.cleanTheTip(explanation);
                    explanation.append("\\rangle$");
                });
                explanation.append("\\end{enumerate}\n");
            }
            case METAGRAPH -> {
                explanation.append("""
                                                
                        \\\\\\\\Метаграф $G_r$ орграфа $G$:
                        \\vspace{8mm}
                                                
                        """);
                explanation.append(LatexGraphDrawer.generateGraph(metaGraph));
                explanation.append("\n\\\\\\\\\\\\В метаграфе $G_r$ орграфа $G$ ровно ")
                        .append("\\textbf{").append(getRibsPhrase(metaGraph.getEdgesCount())).append("}.");
            }
        }

        generateInfo(explanation.toString());
    }

    public static void generateLongExplanation(TaskType taskType, Graph myGraph, List<List<Integer>> sccs, MetaGraph metaGraph) {
        StringBuilder explanation = new StringBuilder();

        explanation.append("""
                                
                \\noindent\\textbf{Подробное обоснование.}
                Исходный орграф $G$:
                \\vspace{8mm}
                                
                """);
        explanation.append(LatexGraphDrawer.generateGraph(myGraph, sccs));
        explanation.append("""
                                
                \\\\\\\\Обращение $G_r$ орграфа $G$:
                \\vspace{2mm}
                                
                """);
        explanation.append(LatexGraphDrawer.generateGraph(myGraph.getReversed(), sccs));

        DetailedExplanationInfo detailedInfo = Algorithms.explanatoryKosaraju(myGraph);

        explanation.append("\n \\vspace{4mm} \n\n Обойдем $G_r$ в глубину и получим список его вершин в порядке убывания их post-значений: \\{");
        detailedInfo.getPostTimeDecreased().forEach(vertex -> {
            explanation.append(vertex + 1).append(", ");
        });
        Utils.cleanTheTip(explanation);
        explanation.append("\\}.\\\\");
        explanation.append("""
                Идя по списку, из каждой (ранее не посещенной) вершины обойдем $исходный$ орграф в глубину. 
                Вершины, посещаемые при каждом новом обходе, будут давать отдельную ССК.
                \\\\(Здесь и далее ССК – сильно связная компонента).\\\\""");
        explanation.append("\n\\begin{itemize}\n");
        Map<Integer, List<Integer>> visitInfo = detailedInfo.getVisitInvocations();
        for (Integer visit : visitInfo.keySet()) {
            explanation.append("\\item $visit($").append(visit + 1).append("$)$ дает ССК:");
            explanation.append(" $\\langle ");
            visitInfo.get(visit).forEach(visited -> explanation.append(visited + 1).append(", "));
            Utils.cleanTheTip(explanation);
            explanation.append("\\rangle$");
        }
        explanation.append("\n\\end{itemize}\n");


        if (taskType.equals(TaskType.SCC)) {
            explanation.append(String.format("""
                                        
                    \\vspace{0.5em}
                    \\textbf{Итого}: в орграфе $G$ ровно \\textbf{%d ССК}:
                    \\begin{enumerate}
                    """, sccs.size()));

            for (Integer visit : visitInfo.keySet()) {
                explanation.append("\\item $\\langle ");
                visitInfo.get(visit).forEach(visited -> explanation.append(visited + 1).append(", "));
                Utils.cleanTheTip(explanation);
                explanation.append("\\rangle$");
            }
            explanation.append("\\end{enumerate}\n");
        }

        if (taskType.equals(TaskType.METAGRAPH)) {
            explanation.append("""
                                       
                    Сформируем из каждой ССК метавершину и будем соединять направленным ребром пару метавершин $X$ и $Y$,
                    если в метавершине $X$ есть вершина, из которой идет ребро в вершину, лежащую в метавершине $Y$.
                    Таким образом получим метаграф $G_r$ орграфа $G$:\\\\\\\\
                                        
                    """);
            explanation.append(LatexGraphDrawer.generateGraph(metaGraph));
            explanation.append("\n\\vspace{1em}\n\nВ этом метаграфе ровно ").append("\\textbf{")
                    .append(getRibsPhrase(metaGraph.getEdgesCount())).append("}.");
        }

        generateInfo(explanation.toString());
    }

    private static String getRibsPhrase(int count) {
        String wordForm;

        // Последние две цифры числа
        int lastTwoDigits = count % 100;
        int lastDigit = count % 10;

        // Правила склонения
        if (lastTwoDigits >= 11 && lastTwoDigits <= 14) {
            wordForm = "рёбер";
        } else if (lastDigit == 1) {
            wordForm = "ребро";
        } else if (lastDigit >= 2 && lastDigit <= 4) {
            wordForm = "ребра";
        } else {
            wordForm = "рёбер";
        }

        return count + " " + wordForm;
    }
}
