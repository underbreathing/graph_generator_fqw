import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import guru.nidi.graphviz.engine.*;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class LatexGraphDrawer {

    private static final String dotFilePath = "graph.dot";
    private static final String xDotLogPath = "xdot_log.xdot";

    static class Node {
        String id;
        double x, y;
        boolean meta;

        Node(String id, double x, double y, boolean meta) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.meta = meta;
        }

        public String toString() {
            if (!meta) {
                return String.format(Locale.US, "\n\\node[ellipse, draw, minimum width=54bp, minimum height=36bp] (v%s) at (%.2f bp,%.2f bp) {%s};", id, x, y, id) +
                        String.format(Locale.US, "\\node (v%s) at (%.2f bp,%.2f bp) [ellipse] {};", id, x, y);
            } else {
                return String.format(Locale.US, "\n\\node[ellipse, draw, minimum width=54bp, minimum height=36bp] (v) at (%.2f bp,%.2f bp) {$\\langle%s\\rangle$};", x, y, id);
            }
        }
    }

    static class Edge {
        String from;
        String to;
        List<double[]> points;

        Edge(String from, String to, List<double[]> points) {
            this.from = from;
            this.to = to;
            this.points = points;
        }

        /*
        точка, контроллы
         */
        public String toString() {
            StringBuilder sb = new StringBuilder("% Edge: " + from + " -> " + to + "\n\\draw [->] ");

            // Далее добавляем кривые с контролями, начиная с первой точки
            for (int i = 1; i + 2 < points.size(); i += 3) {
                double[] p1 = points.get(i);       // Первая контрольная точка
                double[] p2 = points.get(i + 1);   // Вторая контрольная точка
                double[] p3 = points.get(i + 2);   // Конечная точка

                sb.append(String.format(Locale.US, "(%1$.3fbp,%2$.3fbp) ", p1[0], p1[1]));
                sb.append(String.format(Locale.US, ".. controls (%1$.3fbp,%2$.3fbp) and (%3$.3fbp,%4$.3fbp) .. ",
                        p2[0], p2[1], p3[0], p3[1]));
            }
            double[] lastPoint = points.get(0);
            sb.append(String.format(Locale.US, "(%1$.3fbp,%2$.3fbp);", lastPoint[0], lastPoint[1]));
            return sb.toString();
        }
    }

    public static StringBuilder generateGraph(MetaGraph g) {

        String dotContent = DotConvertor.convertToDot(g);

        FileManager.writeToDotFile(dotFilePath, dotContent);

        String xdot = dotToXDOTString(dotFilePath);

        List<Node> nodes = parseMetaNodes(xdot);

        // Рёбра
        List<Edge> edges = parseEdgesMeta(xdot);

        return metaGraphToTikz(nodes, edges);
    }


    //Будет возвращать путь к .tex файлу
    public static StringBuilder generateGraph(Graph g, List<List<Integer>> sccs) {

        //Теперь надо преобразовать в dot файл с подкластерами
        String dotContent = DotConvertor.convertToDot(g, sccs);

        FileManager.writeToDotFile(dotFilePath, dotContent);

        String xdot = dotToXDOTString(dotFilePath);

        List<Node> nodes = parseNodes(xdot);

        // Рёбра
        List<Edge> edges = parseEdges(xdot);

        return graphToTikz(nodes, edges, sccs);
    }

    private static List<Edge> parseEdgesMeta(String xdot) {
        Pattern edgePattern = Pattern.compile(
                "(?m)^\\s*(?:\"([^\"]+)\"|(\\d+))\\s*->\\s*(?:\"([^\"]+)\"|(\\d+)).*?pos=\"e,?([^\"]+)\"",
                Pattern.DOTALL
        );

        Matcher edgeMatcher = edgePattern.matcher(xdot);
        List<Edge> edges = new ArrayList<>();
        while (edgeMatcher.find()) {
            String from;
            String to;
            String[] posData;

            from = edgeMatcher.group(1) != null ? edgeMatcher.group(1) : edgeMatcher.group(2);
            to = edgeMatcher.group(3) != null ? edgeMatcher.group(3) : edgeMatcher.group(4);

            posData = edgeMatcher.group(5)
                    .replaceAll("[^0-9.,\\s]", "")
                    .trim().split("\\s+");

            List<double[]> points = parseEdgePoints(posData);
            edges.add(new Edge(from, to, points));
        }
        return edges;
    }

    private static List<Edge> parseEdges(String xdot) {
        Pattern edgePattern = Pattern.compile("(\\d+) -> (\\d+).*?pos=\"e,?([^\"]+)\"", Pattern.DOTALL);

        Matcher edgeMatcher = edgePattern.matcher(xdot);
        List<Edge> edges = new ArrayList<>();
        while (edgeMatcher.find()) {
            String from;
            String to;
            String[] posData;

            from = edgeMatcher.group(1);
            to = edgeMatcher.group(2);
            posData = edgeMatcher.group(3).replaceAll("[^0-9.,\\s]", "")  // заменяем переносы на пробел
                    .trim().split(" ");

            List<double[]> points = parseEdgePoints(posData);
            edges.add(new Edge(from, to, points));
        }
        return edges;
    }

    private static List<double[]> parseEdgePoints(String[] posData) {
        List<double[]> points = new ArrayList<>();
        for (int i = 0; i < posData.length - 1; i++) {  // <= пропускаем последний
            String coord = posData[i];
            if (coord.contains(",")) {
                String[] xy = coord.split(",");
                double x = Double.parseDouble(xy[0]);
                double y = 0;
                if (xy.length > 1) {
                    y = Double.parseDouble(xy[1]); /// Тут падаем
                } else {
                    System.out.println("ERROR - странное ребро: " + coord + "\n postData = ");
                    for (String pos : posData) {
                        System.out.println(pos);
                    }
                }
                points.add(new double[]{x, y});
            }
        }
        return points;
    }

    private static List<Node> parseMetaNodes(String xdot) {
        Pattern nodePattern = Pattern.compile(
                "(?m)^\\s*(?:\"([^\"]+)\"|(\\d+))\\s*\\[.*?pos=\"(\\d+(?:\\.\\d+)?),(\\d+(?:\\.\\d+)?)\"",
                Pattern.DOTALL
        );
        Matcher nodeMatcher = nodePattern.matcher(xdot);
        List<Node> nodes = new ArrayList<>();

        while (nodeMatcher.find()) {
            String id = nodeMatcher.group(1) != null ? nodeMatcher.group(1) : nodeMatcher.group(2);
            double x = Double.parseDouble(nodeMatcher.group(3));
            double y = Double.parseDouble(nodeMatcher.group(4));
            nodes.add(new Node(id, x, y, true));
        }
        return nodes;
    }

    private static List<Node> parseNodes(String xdot) {
        Pattern nodePattern;
        nodePattern = Pattern.compile(
                "(?m)^\\s*(\\d+)\\s+\\[.*?pos=\"(\\d+(?:\\.\\d+)?),(\\d+(?:\\.\\d+)?)\"",
                Pattern.DOTALL
        );

        Matcher nodeMatcher = nodePattern.matcher(xdot);
        List<Node> nodes = new ArrayList<>();
        while (nodeMatcher.find()) {
            String id = nodeMatcher.group(1);
            double x = Double.parseDouble(nodeMatcher.group(2));
            double y = Double.parseDouble(nodeMatcher.group(3));
            nodes.add(new Node(id, x, y, false));
        }
        return nodes;
    }

    private static String dotToXDOTString(String pathToDotFile) {
        String xdotOutput = "";

        try {
            Graphviz.useEngine(new GraphvizCmdLineEngine());
            // Путь к DOT-файлу
            File dotFile = new File(pathToDotFile);

            // Парсинг в MutableGraph
            MutableGraph g = new Parser().read(dotFile);

            // Генерация XDOT-строки
            xdotOutput = Graphviz.fromGraph(g).render(Format.XDOT).toString();

            // Записываем лог в файл
            File outputFile = new File(xDotLogPath);
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(xdotOutput);
            }
            System.out.println("Лог XDOT записан. Смотри в файле : " + xDotLogPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return xdotOutput;
    }


    public static StringBuilder metaGraphToTikz(List<Node> nodes, List<Edge> edges) {
        StringBuilder graphCode = new StringBuilder();

        appendHeader(graphCode, 0.4f);

        appendContent(edges, graphCode, nodes);

        appendBottom(graphCode);
        return graphCode;
    }

    private static void appendContent(List<Edge> edges, StringBuilder graphCode, List<Node> nodes) {
        for (Edge edge : edges) {
            graphCode.append(edge.toString());
        }
        for (Node node : nodes) {
            graphCode.append(node.toString());
        }
    }


    public static StringBuilder graphToTikz(List<Node> nodes, List<Edge> edges, List<List<Integer>> sccs) {
        StringBuilder graphCode = new StringBuilder();

        appendHeader(graphCode, 0.7f);

        appendContent(edges, graphCode, nodes);


        sccs.forEach(scc -> {
            graphCode.append("\n \\node[draw, thick, blue, dashed, rounded corners=25pt, fit=");
            scc.forEach(vertex -> {
                graphCode.append("(v").append(vertex + 1).append(") ");
            });
            graphCode.append(", inner sep=25pt] {};");
        });


        appendBottom(graphCode);
        return graphCode;
    }

    private static void appendBottom(StringBuilder graphCode) {
        graphCode.append("""
                                    
                \\end{tikzpicture}
                \\end{adjustbox}
                % End of code""");
    }

    private static void appendHeader(StringBuilder graphCode, float maxHeight) {
        graphCode.append(String.format("""
                %% Start of code
                \\begin{adjustbox}{max size={1.4\\textwidth}{%.1f\\textheight}, center} %% ограничиваем ширину и высоту
                \\begin{tikzpicture}[>=latex',line join=bevel,]
                  \\pgfsetlinewidth{1bp}
                %%
                \\pgfsetcolor{black}
                """, maxHeight));
    }

}
