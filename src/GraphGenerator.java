import jdk.jshell.execution.Util;

import java.util.*;
import java.util.logging.Logger;

public class GraphGenerator {

    static final boolean isEnabledCycleInCycle = true;

    private static final Logger logger = Logger.getLogger(GraphGenerator.class.getName());
    private static final List<List<Integer>> sccs = new ArrayList<>();

    public static List<List<Integer>> getSCCs() {
        if (sccs.isEmpty()) {
            throw new IllegalStateException("Сначала нужно сгенерировать граф. Нужно вызвать функцию generateGraph, а затем эту.");
        }
        return sccs;
    }

    public static Graph generateGraph(int countOfNodes) {
        Graph graph = new Graph(countOfNodes);
        sccs.clear();
        int remainingNodes = countOfNodes;//оставшиеся узлы
        List<Integer> nodeList = new ArrayList<>();
        for (int i = 0; i < countOfNodes; i++) {
            nodeList.add(i);
        }
        Collections.shuffle(nodeList);

        while (remainingNodes > 0) {
            List<Integer> newSCC = new ArrayList<>();
            int newSSKSize = calculateNewSCCsize(remainingNodes);
            for (int i = 0; i < newSSKSize; i++) {
                newSCC.add(nodeList.remove(0));
            }
            linkSCCWithACycle(graph, newSCC);
            linkSCCToPreviousOnes(graph, newSCC);//линкуем к предыдущим до того как добавили в граф, чтобы ssk графа на момент линковки не включало текущую сск
            sccs.add(newSCC);
            remainingNodes -= newSSKSize;
        }

        return graph;
    }

    private static void linkSCCToPreviousOnes(Graph graph, List<Integer> newSCC) {
        if (sccs.isEmpty()) return;

        List<List<Integer>> selectedSCCs = getRandomSubset(sccs);

        int linkCount = generateCountOfLinks(calculateCountOfNodes(selectedSCCs));

        // Собираем все возможные рёбра: newSCC -> уже созданные ССК
        List<Graph.Edge> candidateEdges = new ArrayList<>();
        for (List<Integer> previousSCC : selectedSCCs) {
            for (int prev : previousSCC) {
                for (int curr : newSCC) {
                    candidateEdges.add(new Graph.Edge(curr, prev));
                }
            }
        }

        // Случайно выбираем нужное количество рёбер
        Collections.shuffle(candidateEdges);

        for (int i = 0; i < linkCount; i++) {
            Graph.Edge e = candidateEdges.get(i);
            graph.addNewEdge(e);
        }
    }


    private static int calculateCountOfNodes(List<List<Integer>> selectedSCCs) {
        return selectedSCCs.stream()
                .mapToInt(List::size)
                .sum();
    }

    private static int generateCountOfLinks(int size) { // определяем количество линковачных ребер в зависимости от количества выбранных старых ССК
        int rndInt = Utils.rnd.nextInt(0, 101);
        if (rndInt < 50) {
            logger.info("кол-во связующих с предыдущими ребер =  " + (int) (size * 0.3) + " size = " + size);
            return (int) (size * 0.3);
        }
        if (rndInt < 80) {
            logger.info("кол-во связующих с предыдущими ребер =  " + (int) (size * 0.5) + " size = " + size);
            return (int) (size * 0.5);
        }
        logger.info("кол-во связующих с предыдущими ребер =  " + (int) (size * 0.7) + " size = " + size);
        return (int) (size * 0.7);
    }

    public static List<List<Integer>> getRandomSubset(List<List<Integer>> lists) {
        if (lists == null || lists.isEmpty()) {
            return Collections.emptyList();
        }

        // Перемешиваем список случайным образом
        List<List<Integer>> shuffledList = new ArrayList<>(lists);
        Collections.shuffle(shuffledList, Utils.rnd);

        // Генерируем случайное количество элементов в подмножестве (от 0 до N)
        int subsetSize = Utils.rnd.nextInt(shuffledList.size() + 1);

        // Берем первые subsetSize элементов
        return new ArrayList<>(shuffledList.subList(0, subsetSize));
    }

    //здесь можно генерировать ССК с циклом в цикле. Как? Добавить еще одно ребро из любой взятой вершины в другую вершину.
    private static void linkSCCWithACycle(Graph graph, List<Integer> nextSCC) {
        int nextSCCsize = nextSCC.size();
        if (nextSCCsize >= 2) {
            for (int i = 0; i < nextSCCsize - 1; i++) {
                int nodeA = nextSCC.get(i);
                int nodeB = nextSCC.get(i + 1);
                graph.addNewEdge(nodeA, nodeB);
            }
            int last = nextSCC.get(nextSCCsize - 1);
            int first = nextSCC.get(0);
            graph.addNewEdge(last, first);
            if (isEnabledCycleInCycle) { //слева в условии фича тоггл
                if (Utils.rnd.nextInt(1, 101) <= 75) {
                    addInnerCycle(graph, nextSCC);
                }
            }
        }

    }

    private static void addInnerCycle(Graph graph, List<Integer> nextSSC) {
        int size = nextSSC.size();

        logger.info("size = " + size);

        if (size == 1) {
            if (Utils.rnd.nextBoolean()) {
                graph.addNewEdge(nextSSC.get(0), nextSSC.get(0));
            }
            return;
        }

        int countOfInnerEdges = Utils.rnd.nextInt(1, size);

        logger.info("count of inner edges = " + countOfInnerEdges);

        for (int i = 0; i < countOfInnerEdges; ++i) {
            int a = nextSSC.get(Utils.rnd.nextInt(size));
            int b = nextSSC.get(Utils.rnd.nextInt(size));
            if (Utils.rnd.nextBoolean()) {
                graph.addNewEdge(a, b);
            } else {
                graph.addNewEdge(b, a);
            }
        }
    }

    private static int calculateNewSCCsize(int remainingNodes) {
        if (remainingNodes > 5) {//из-за того, что dot2tex не поддерживает количество вершин в ССК более 5
            return Utils.rnd.nextInt(1, 6);
        }
        return Utils.rnd.nextInt(1, remainingNodes + 1);
    }
}
