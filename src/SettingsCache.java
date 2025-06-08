import uimodels.Explanation;
import uimodels.TaskType;

import java.util.prefs.Preferences;

public class SettingsCache {

    private static final String ANSWER = "answer";
    private static final String EXPLAIN = "explain";
    private static final String PREFS_KEY = "graphGeneratorPrefs";
    private static final String LATEX_PATH = "latex path";
    private static final String VMIN = "vmin";
    private static final String VMAX = "vmax";
    private static final String N = "n";
    private static final String TASK_TYPE = "task type";

    private static final Preferences prefs = Preferences.userRoot().node(PREFS_KEY);

    public static void saveLatexPath(String path) {
        prefs.put(LATEX_PATH, path);
    }

    public static String getLatexPath() {
        return prefs.get(LATEX_PATH, "");
    }

    public static void saveVmin(int value) {
        prefs.putInt(VMIN, value);
    }

    public static int getVmin() {
        return prefs.getInt(VMIN, 6); // значение по умолчанию
    }

    public static void saveVmax(int value) {
        prefs.putInt(VMAX, value);
    }

    public static int getVmax() {
        return prefs.getInt(VMAX, 8); // значение по умолчанию
    }

    public static boolean getGenAnswer() {
        return prefs.getBoolean(ANSWER, true);
    }

    public static void setGenAnswer(boolean value) {
        prefs.putBoolean(ANSWER, value);
    }

    public static Explanation getGenExplanation() {
        String value = prefs.get(EXPLAIN, Explanation.NO.toString()); // по умолчанию NO
        try {
            return Explanation.valueOf(value);
        } catch (IllegalArgumentException e) {
            return Explanation.NO; // на случай повреждённых данных
        }
    }

    public static void setGenExplanation(Explanation value) {
        prefs.put(EXPLAIN, value.name());
    }

    public static int getN() {
        return prefs.getInt(N, 7);
    }

    public static void setN(int value) {
        prefs.putInt(N, value);
    }

    public static TaskType getTaskType() {
        String value = prefs.get(TASK_TYPE, TaskType.SCC.name()); // по умолчанию NO
        try {
            return TaskType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return TaskType.SCC; // на случай повреждённых данных
        }
    }

    public static void setTaskType(TaskType value) {
        if(value == null) return;
        prefs.put(TASK_TYPE, value.name());
    }
}
