import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    public static void main(String[] args) {
        String configPath = System.getProperty("config.file");
        System.out.println("Config path: " + configPath);

        if (configPath == null) {
            System.err.println("Please provide -Dconfig.file=path/to/config.properties");
            System.exit(1);
        }

        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream(configPath)) {
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("App Name: " + props.getProperty("app.name"));
        System.out.println("Version: " + props.getProperty("app.version"));
        System.out.println("Environment: " + props.getProperty("app.env"));
    }
}