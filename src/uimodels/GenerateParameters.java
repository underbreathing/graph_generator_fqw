package uimodels;

public record GenerateParameters(
        int n,
        int vmin,
        int vmax,
        boolean generateAnswer,
        Explanation explanation,
        String latexPath,
        TaskType taskType
) {}
