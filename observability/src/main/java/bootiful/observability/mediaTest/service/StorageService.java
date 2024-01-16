package bootiful.observability.mediaTest.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author pari on 15/01/24
 */
public interface StorageService {
    void init();

    void store(MultipartFile file);

    Stream<Path> loadAll();

    Path load(String filename);

    ResponseEntity<Resource> loadAsResource(String filename);

    // loads the file to memory
    @Deprecated(since = "it loads the entire file to memory. use loadAsResource() instead")
    ResponseEntity<ByteArrayResource> downloadAsByteArrayResource(String filename);

    ResponseEntity<StreamingResponseBody> downloadAsStreamingResBody(String filename);

    void deleteAll();
}
