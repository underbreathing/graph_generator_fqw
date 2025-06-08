import uimodels.DetailedExplanationInfo;

import java.util.*;
import java.util.function.Consumer;

public class Algorithms {
    static boolean[] visited;
    static List<Integer> postTimeDecreased;
    static int curSCC; //номер текущей ССК
    static int[] numSCC;
    static List<Integer> visitedVertices;

    public static DetailedExplanationInfo explanatoryKosaraju(Graph g) {
        DetailedExplanationInfo detailedInfo = new DetailedExplanationInfo();
        Map<Integer, List<Integer>> visitInvocations = new LinkedHashMap<>();
        curSCC = 0;
        postTimeDecreased = new ArrayList<>();
        Graph reversedG = g.getReversed();
        dfs(reversedG, null, v -> postTimeDecreased.add(0, v));
        detailedInfo.setPostTimeDecreased(postTimeDecreased);

        numSCC = new int[reversedG.adjList.length];
        visited = new boolean[reversedG.adjList.length];
        for (int node : postTimeDecreased) {
            if (!visited[node]) {
                visitedVertices = new ArrayList<>();
                curSCC++;
                visit(node, g.adjList, v -> numSCC[v] = curSCC, null);
                visitInvocations.put(node, visitedVertices);
            }
        }
        detailedInfo.setVisitInvocations(visitInvocations);
        return detailedInfo;
    }

    //будем представлять не списком смежных вершин, а множеством смежных вершин. Set. дубликаты ребер будут автоматически всюду устраняться
    // и метаграф будем строить по ходу генерации исходного орграфа
    // правда маленькое затруднение, мы не знаем сколько будет вершин в метаграфе
    //сделать так - завести вектор с размером равным количеству вершин в орграфе а в конце сделать resizetoContent
    public static MetaGraph generateMetaGraph(Graph myGraph, List<List<Integer>> sccs) {
        MetaGraph metaGraph = new MetaGraph(sccs);

        Map<Integer, Integer> sccIndexes = getSCCindexes(sccs);

        Set<Graph.Edge> metaEdges = new HashSet<>();

        List<Integer>[] adj = myGraph.adjList;

        for (int i = 0; i < adj.length; ++i) {
            int thisSCC = sccIndexes.get(i); // ССК в которой лежит эта вершина
            for (Integer vertex : adj[i]) {
                int otherSCC = sccIndexes.get(vertex);
                if (thisSCC != otherSCC) {
                    metaEdges.add(new Graph.Edge(thisSCC, otherSCC));
                }
            }
        }
        for (Graph.Edge edge : metaEdges) {
            metaGraph.addNewEdge(edge);
        }
        return metaGraph;
    }

    private static Map<Integer, Integer> getSCCindexes(List<List<Integer>> sccs) {
        Map<Integer, Integer> sccIndexes = new HashMap<>();
        for (int sccNumber = 0; sccNumber < sccs.size(); sccNumber++) {
            List<Integer> scc = sccs.get(sccNumber);
            for (int vertex : scc) {
                sccIndexes.put(vertex, sccNumber);
            }
        }
        return sccIndexes;
    }

    private static void dfs(Graph graph, Consumer<Integer> previsit, Consumer<Integer> postvisit) {
        int size = graph.adjList.length;

        visited = new boolean[size];

        for (int i = 0; i < size; ++i) {
            if (!visited[i]) {
                visit(i, graph.adjList, previsit, postvisit);
            }
        }

        System.out.println("Вершины в порядке убывания post-time:" + postTimeDecreased);
    }

    private static void visit(int v, List<Integer>[] adj, Consumer<Integer> previsit, Consumer<Integer> postvisit) {
        visited[v] = true;
        if (visitedVertices != null) {
            visitedVertices.add(v);
        }
        if (previsit != null) {
            previsit.accept(v);
        }

        adj[v].forEach(vNext -> {
            if (!visited[vNext]) {
                visit(vNext, adj, previsit, postvisit);
            }
        });

        if (postvisit != null) {
            postvisit.accept(v);
        }
    }
}
