import java.util.*;

public class MetaGraph extends Graph{

    public MetaGraph(List<List<Integer>> sccs) {
        super(sccs.size());
        mvToSCCsSet = new HashMap<>();
        for(int i = 0; i < sccs.size(); ++i){
            mvToSCCsSet.put(i, new HashSet<>(sccs.get(i)));
        }
    }

    public Map<Integer, Set<Integer>> mvToSCCsSet;

    public int getEdgesCount(){
        int count = 0;
        for (Set<Integer> adj : adjList) {
            for (Integer ignored : adj) {
                ++count;
            }
        }
        return count;
    }
}
