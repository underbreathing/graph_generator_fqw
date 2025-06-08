import uimodels.Explanation;
import uimodels.GenerateParameters;
import uimodels.TaskType;

import java.util.List;
import java.util.Objects;

public class GenerateManager {

    public static void generate(GenerateParameters parameters, String outputFilePath) {

        int countOfNodes = Utils.rnd.nextInt(parameters.vmin(), parameters.vmax() + 1);
        TaskType taskType = parameters.taskType();
        Graph myGraph = GraphGenerator.generateGraph(countOfNodes, taskType.equals(TaskType.METAGRAPH));
        List<List<Integer>> sccs = GraphGenerator.getSCCs();
        MetaGraph metaGraph = GraphGenerator.metaGraph;

        myGraph.printAdjacencyList();
        System.out.println("ССК орграфа: " + sccs);


        int answer = parameters.generateAnswer() ? taskType.equals(TaskType.SCC) ? sccs.size() : metaGraph.getEdgesCount() : -1;

        LatexGenerator.init(outputFilePath);
        LatexGenerator.generateStart();

        LatexGenerator.generateTask(taskType, myGraph.adjList, answer, parameters.n());


        switch (Objects.requireNonNull(parameters.explanation())) {

            case DETAILED -> LatexGenerator.generateLongExplanation(taskType, myGraph, sccs, metaGraph);
            case SHORT -> LatexGenerator.generateShortExplanation(taskType, myGraph, sccs, metaGraph);
            case NO -> {
            }
        }

        LatexGenerator.generateEnd();
    }

}
