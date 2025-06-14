package uimodels;

public enum Explanation {
    DETAILED,
    SHORT,
    NO;

    @Override
    public String toString() {
        return switch (this) {
            case DETAILED -> "Подробное";
            case SHORT -> "Короткое";
            case NO -> "Без обоснования";
        };
    }
}
