package off.szymon.vMessage;

import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class Config {

    private final static Logger log = VMessagePlugin.getInstance().getLogger();
    private static File file;
    private static YamlFile yaml;

    public static void setup() {
        file = copyResource();
        try {
            yaml = new YamlFile(file);
            yaml.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File copyResource() {
        File outputFile = new File(VMessagePlugin.getInstance().getDataFolder(), "config.yml");
        try {
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Failed to create directories for: " + outputFile.getAbsolutePath());
            }
            if (!outputFile.exists()) {
                try (InputStream in = Objects.requireNonNull(Config.class.getClassLoader().getResourceAsStream("config.yml"))) {
                    Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    log.debug("Copied resource to {}",outputFile.getAbsolutePath());
                }
            } else {
                log.debug("File already exists: {}", outputFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    public static String getString(String key) {
        return (String) yaml.get(key);
    }

    public static YamlFile getYaml() {
        return yaml;
    }

    public static boolean reload() {
        try {
            yaml.load(file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
