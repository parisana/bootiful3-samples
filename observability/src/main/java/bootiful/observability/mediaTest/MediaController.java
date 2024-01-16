package bootiful.observability.mediaTest;

import bootiful.observability.mediaTest.service.StorageService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * @author pari on 15/01/24
 */

@RestController
@RequestMapping("media")
@Slf4j
@RequiredArgsConstructor
public class MediaController {

    private final StorageService storageService;
    @PostMapping("normal-upload")
    @Observed(name="#media#uploadFile")
    // apache's multipart upload switches to using disk if file upload size>1kb
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file) {
        log.info("received file upload request!");
        this.storageService.store(file);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("download")
    @Observed
    public ResponseEntity<Resource> serveAFileAsResource() {
        log.info("received file download request!");
        return this.storageService.loadAsResource("largeFile.mp4");
    }

    @GetMapping("download-2")
    @Observed
    public ResponseEntity<ByteArrayResource> serveAnyFileAsByteArrayResource() {
        log.info("2 received file download request!");
        return this.storageService.downloadAsByteArrayResource("largeFile.mp4");
    }

    @GetMapping("download-3")
    @Observed
    public ResponseEntity<StreamingResponseBody> serveAnyFileAsStream() {
        log.info("3 received file download request!");
        return this.storageService.downloadAsStreamingResBody("largeFile.mp4");
    }

}
