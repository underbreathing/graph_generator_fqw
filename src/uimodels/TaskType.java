package uimodels;

public enum TaskType {
    SCC,
    METAGRAPH;

    @Override
    public String toString() {
        return switch (this) {
            case SCC -> "ССК";
            case METAGRAPH -> "Метаграф";
        };
    }
}
