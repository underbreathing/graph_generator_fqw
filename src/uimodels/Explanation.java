package uimodels;

public enum Explanation {
    DETAILED,
    SHORT,
    NO;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase(); // Для красивого отображения
    }
}
