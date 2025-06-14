import java.util.*;

public class MetaGraph extends Graph {

    public MetaGraph(int size) {
        super(size);
    }

    public void setMetaVertices(List<List<Integer>> sccs) {
        mvToSCCsSet = new HashMap<>();
        for (int i = 0; i < sccs.size(); ++i) {
            mvToSCCsSet.put(i, new HashSet<>(sccs.get(i)));
        }
    }

    public Map<Integer, Set<Integer>> mvToSCCsSet;

    public int getEdgesCount() {
        int count = 0;
        for (Set<Integer> adj : adjList) {
            for (Integer ignored : adj) {
                ++count;
            }
        }
        return count;
    }

    public void trimAdjList() {
        adjList = Arrays.copyOf(adjList, mvToSCCsSet.size());
    }
}
