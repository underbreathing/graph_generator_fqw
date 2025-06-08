import java.util.List;
import java.util.Map;
import java.util.Set;

public class DotConvertor {

    static String convertToDot(MetaGraph metaGraph) {
        StringBuilder dotContent = new StringBuilder();
        dotContent.append("digraph G {\n");

        for (int i = 0; i < metaGraph.adjList.length; ++i) {
            appendMetaVertex(dotContent, metaGraph.mvToSCCsSet, i);
            dotContent.append(";\n");
        }

        for (int i = 0; i < metaGraph.adjList.length; i++) {
            for (int j : metaGraph.adjList[i]) {
                appendMetaVertex(dotContent, metaGraph.mvToSCCsSet, i);
                dotContent.append(" -> ");
                appendMetaVertex(dotContent, metaGraph.mvToSCCsSet, j);
                dotContent.append(";\n");
            }
        }

        dotContent.append("}");
        return dotContent.toString();

    }

    static String convertToDot(Graph myGraph, List<List<Integer>> sccs) {
        StringBuilder dotContent = new StringBuilder();
        dotContent.append("digraph G {\n");

        sccs.forEach((scc) -> {
            dotContent.append("subgraph cluster_").append(scc.get(0)).append(" {\nstyle=invis;\n");
            scc.forEach((vertex) -> dotContent.append(vertex + 1).append(";\n"));
            dotContent.append("}\n");
        });

        for (int i = 0; i < myGraph.adjList.length; i++) {
            for (int j : myGraph.adjList[i]) {
                dotContent.append(i + 1).append(" -> ").append(j + 1).append(";\n");
            }
        }


        dotContent.append("}");
        return dotContent.toString();
    }

    private static void appendMetaVertex(StringBuilder dotContent, Map<Integer, Set<Integer>> sccs, int i) {
        dotContent.append("\"");
        for (int vertex : sccs.get(i)) {
            dotContent.append(vertex + 1).append(",");
        }
        dotContent.deleteCharAt(dotContent.length() - 1);
        dotContent.append("\"");
    }
}
