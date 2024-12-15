package me.artemiyulyanov.uptodate.controllers.api.files;

import me.artemiyulyanov.uptodate.controllers.AuthenticatedController;
import me.artemiyulyanov.uptodate.minio.MinioMediaFile;
import me.artemiyulyanov.uptodate.minio.MinioService;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.web.RequestService;
import me.artemiyulyanov.uptodate.web.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class ImageController extends AuthenticatedController {
    @Autowired
    private MinioService minioService;

    @Autowired
    private RequestService requestService;

    @GetMapping("/get/{path}")
    public ResponseEntity getImage(@PathVariable String path, Model model) {
        if (!isUserAuthorized()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
        }

        MinioMediaFile mediaFile = minioService.getMediaFile(path);

        try (InputStream inputStream = mediaFile.getInputStream()) {
            return requestService.executeImage(HttpStatus.OK, mediaFile.getMediaType(), inputStream.readAllBytes());
        } catch (Exception e) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 20, "Unable to return image!");
        }
    }

    @Deprecated
    @PostMapping("/upload")
    public ResponseEntity<ServerResponse> uploadImage(@RequestParam MultipartFile file) {
        Optional<User> wrappedUser = getAuthorizedUser();

        if (!isUserAuthorized()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
        }

        try {
            String objectKey = wrappedUser.get().getUsername() + File.separator + file.getOriginalFilename();

            if (!MinioMediaFile.isAvailable(objectKey)) {
                return requestService.executeError(HttpStatus.BAD_REQUEST, 21, "This file format is unavailable!");
            }

            minioService.uploadFile(objectKey, file);
            return requestService.executeTemplate(HttpStatus.OK, 200, "The file has been uploaded successfully!", Map.of("path", objectKey));
        } catch (NullPointerException e) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 20, "Unable to upload image!");
        }
    }
}