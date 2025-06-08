import jdk.jshell.execution.Util;
import jnr.ffi.annotations.Meta;

import java.util.*;
import java.util.logging.Logger;

public class GraphGenerator {

    static final boolean isEnabledCycleInCycle = true;

    static MetaGraph metaGraph = null;

    private static final Logger logger = Logger.getLogger(GraphGenerator.class.getName());
    private static final List<List<Integer>> sccs = new ArrayList<>();

    public static List<List<Integer>> getSCCs() {
        if (sccs.isEmpty()) {
            throw new IllegalStateException("Сначала нужно сгенерировать граф. Нужно вызвать функцию generateGraph, а затем эту.");
        }
        return sccs;
    }

    public static Graph generateGraph(int countOfNodes, boolean isMetagraphRequired) {
        Graph graph = new Graph(countOfNodes);
        MetaGraph metaGraph = null;
        if (isMetagraphRequired) {
            metaGraph = new MetaGraph(countOfNodes);
        }
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
            linkSCCToPreviousOnes(graph, newSCC, metaGraph);
            sccs.add(newSCC);
            remainingNodes -= newSSKSize;
        }

        if (isMetagraphRequired) {
            metaGraph.setMetaVertices(sccs);
            metaGraph.trimAdjList();
            GraphGenerator.metaGraph = metaGraph;
        }

        return graph;
    }

    private static void linkSCCToPreviousOnes(Graph graph, List<Integer> newSCC, MetaGraph metagraph) {
        if (sccs.isEmpty()) return;

        int newSCCindex = sccs.size();

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

        Collections.shuffle(candidateEdges);

        // Случайно выбираем нужное количество рёбер
        for (int i = 0; i < linkCount; i++) {
            Graph.Edge e = candidateEdges.get(i);
            graph.addNewEdge(e);
            if (metagraph != null) {
                int prevSCCindex = findSubsetIndexContainingElement(selectedSCCs, e.to());
                metagraph.addNewEdge(newSCCindex, prevSCCindex);
            }
        }
    }

    public static int findSubsetIndexContainingElement(List<List<Integer>> listOfLists, int k) {
        for (int i = 0; i < listOfLists.size(); i++) {
            if (listOfLists.get(i).contains(k)) {
                return i;
            }
        }
        throw new IllegalStateException("Ошибка в алгоритме linkSCCToPreviousOnes");
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

    private static int calculateCountOfNodes(List<List<Integer>> selectedSCCs) {
        return selectedSCCs.stream()
                .mapToInt(List::size)
                .sum();
    }

//    private static void linkSCCToPreviousOnes(Graph graph, List<Integer> newSCC, MetaGraph metaGraph) {
//        if (sccs.isEmpty()) return;
//
//        int sccsSize = sccs.size();
//
//        List<Integer> selectedSCCindices = getRandomSubsetIndices(sccs);
//
//        for (int i = 0; i < selectedSCCindices.size(); ++i) {
//            int currentIndex = selectedSCCindices.get(i);
//            List<Integer> selectedPrevious = getRandomSubset(sccs.get(currentIndex));
//            List<Integer> selectedCurrent = getRandomSubset(newSCC);
//            for (int prev : selectedPrevious) {
//                for (int current : selectedCurrent) {
//                    graph.addNewEdge(current, prev);
//                    if (metaGraph != null) {
//                        metaGraph.addNewEdge(sccsSize, currentIndex);
//                    }
//                }
//            }
//        }
//    }

    public static <T> List<Integer> getRandomSubsetIndices(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            indices.add(i);
        }

        Collections.shuffle(indices, Utils.rnd);

        int subsetSize = Utils.rnd.nextInt(indices.size() + 1);
        return new ArrayList<>(indices.subList(0, subsetSize));
    }

    public static <T> List<T> getRandomSubset(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> shuffledList = new ArrayList<>(list);
        Collections.shuffle(shuffledList, Utils.rnd);

        int subsetSize = Utils.rnd.nextInt(shuffledList.size() + 1);
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
