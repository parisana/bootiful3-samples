package bootiful.observability.mediaTest.service;

import bootiful.observability.mediaTest.exceptions.StorageException;
import bootiful.observability.mediaTest.exceptions.StorageFileNotFoundException;
import bootiful.observability.mediaTest.properties.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author pari on 15/01/24
 */
@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {

        if(properties.getLocation() == null || properties.getLocation().toString().isEmpty()){
            throw new StorageException("File upload location can not be Empty.");
        }
        this.rootLocation = properties.getLocation();
    }

    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(Objects.requireNonNull(file.getOriginalFilename())))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try (final Stream<Path> pathStream = Files.walk(this.rootLocation, 1)) {
            return pathStream
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public ResponseEntity<Resource> loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentLength(resource.contentLength())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        } catch (IOException e) {
            throw new StorageException("Error loading file", e);
        }
    }

    @Deprecated
    @Override
    public ResponseEntity<ByteArrayResource> downloadAsByteArrayResource(final String filename) {
        try {
            Path localInputFPath = load(filename);
            final byte[] fileBytes = Files.readAllBytes(localInputFPath);
            return ResponseEntity.ok()
                    .contentLength(fileBytes.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" +
                            filename +
                            "\"")
                    .body(new ByteArrayResource(fileBytes));
        } catch (IOException e) {
            throw new StorageException("Error loading file", e);
        }
    }

    @Override
    public ResponseEntity<StreamingResponseBody> downloadAsStreamingResBody(final String filename) {
        final Path filePath = load(filename);
        final StreamingResponseBody streamingResponseBody = outputStream -> {
            try(final FileInputStream fileInputStream = new FileInputStream(filePath.toFile())) {
                final byte[] buffer = new byte[16*1024];
                int numberOfBytesToWrite;
                while ((numberOfBytesToWrite = fileInputStream.read(buffer, 0, buffer.length)) != -1) {
                    outputStream.write(buffer, 0, numberOfBytesToWrite);
                }
            } catch (IOException e) {
                throw new StorageFileNotFoundException("Error loading file!", e);
            }

        };
        return ResponseEntity.ok()
                .contentLength(filePath.toFile().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" +
                        filename +
                        "\"")
                .body(streamingResponseBody);
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
