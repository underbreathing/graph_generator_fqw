import java.util.HashSet;
import java.util.Set;

public class Graph {
    public Set<Integer>[] adjList;

    @SuppressWarnings("unchecked")
    public Graph(int vertices) {
        adjList = new Set[vertices];

        for (int i = 0; i < vertices; i++) {
            adjList[i] = new HashSet<>();
        }
    }

    public boolean containsEdge(int first, int second) {
        return adjList[first].contains(second);
    }

    public void printAdjacencyList() {
        System.out.println("Список смежности:");
        for (int i = 0; i < adjList.length; ++i) {
            System.out.print(i + ": ");
            for (int j : adjList[i]) {
                System.out.print(j + " ");
            }
            System.out.println();
        }
    }

    public record Edge(int from, int to) {
    }

    void addNewEdge(Edge ed) {
        addNewEdge(ed.from, ed.to);
    }

    void addNewEdge(int a, int b) {
        if (a < 0 || a > adjList.length - 1 || b < 0 || b > adjList.length - 1)
            throw new IllegalStateException("Не верно заданные параметры: a = " + a + " b = " + b);
        adjList[a].add(b);
    }

    public Graph getReversed() {
        Graph reversed = new Graph(adjList.length);

        for (int i = 0; i < reversed.adjList.length; ++i) {
            for (int neighbor : adjList[i]) {
                reversed.addNewEdge(neighbor, i);
            }
        }
        return reversed;
    }
}
