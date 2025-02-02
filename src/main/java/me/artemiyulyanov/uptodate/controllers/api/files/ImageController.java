package me.artemiyulyanov.uptodate.controllers.api.files;

import me.artemiyulyanov.uptodate.controllers.AuthenticatedController;
import me.artemiyulyanov.uptodate.controllers.api.files.responses.FileUploadResponse;
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

    @GetMapping("/get")
    public ResponseEntity<?> getImage(@RequestParam String path, Model model) {
        MinioMediaFile mediaFile = minioService.getMediaFile(path);

        try (InputStream inputStream = mediaFile.getInputStream()) {
            return requestService.executeMediaResponse(HttpStatus.OK, mediaFile.getMediaType(), inputStream.readAllBytes());
        } catch (Exception e) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Unable to return image!");
        }
    }

    @Deprecated
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam MultipartFile file) {
        Optional<User> wrappedUser = getAuthorizedUser();

//        if (!isUserAuthorized()) {
//            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
//        }

        try {
            String objectKey = wrappedUser.get().getUsername() + File.separator + file.getOriginalFilename();

            if (!MinioMediaFile.isAvailable(objectKey)) {
                return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "This file format is unavailable!");
            }

            minioService.uploadFile(objectKey, file);
            return requestService.executeCustomResponse(
                    FileUploadResponse.builder()
                            .status(HttpStatus.OK.value())
                            .message("The file has been uploaded successfully!")
                            .path(objectKey)
                            .build()
            );
        } catch (NullPointerException e) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Unable to upload image!");
        }
    }
}