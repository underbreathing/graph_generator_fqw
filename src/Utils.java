import java.util.Random;

public class Utils {

    static Random rnd = new Random();

    public static void cleanTheTip(StringBuilder sVertices) {
        if (sVertices.charAt(sVertices.length() - 1) == 'k') {
            deleteLast(sVertices, 12);
        }
        deleteLast(sVertices, 2);
    }

    private static void deleteLast(StringBuilder sb, int count) {
        if (sb.length() >= count) {
            sb.delete(sb.length() - count, sb.length() - 1);
        }
    }


}
