package bootiful.observability.mediaTest.properties;

import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author pari on 15/01/24
 */
@Configuration
public class StorageProperties {
    private final Path mediaParentPath = Paths.get(System.getProperty("user.dir")).resolve("media");

    public Path getLocation() {
        return mediaParentPath;
    }
}
