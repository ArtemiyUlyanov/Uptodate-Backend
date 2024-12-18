package me.artemiyulyanov.uptodate.minio.resources;

import lombok.*;
import me.artemiyulyanov.uptodate.minio.MinioService;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
@Builder
public class UserResourceManager implements ResourceManager<User> {
    public static final String RESOURCES_FOLDER = "users/%d";

    @Autowired
    private MinioService minioService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void uploadResources(User user, List<MultipartFile> files) {
        MultipartFile icon = files.get(0);

        if (icon != null) {
            String iconObjectKey = getResourceFolder(user) + File.separator + icon.getOriginalFilename();
            minioService.uploadFile(iconObjectKey, icon);

            user.setIcon(iconObjectKey);
            userRepository.save(user);
        }
    }

    @Override
    public void updateResources(User user, List<MultipartFile> files) {
        MultipartFile icon = files.get(0);

        if (icon != null) {
            String iconObjectKey = getResourceFolder(user) + File.separator + icon.getOriginalFilename();

            minioService.deleteFolder(getResourceFolder(user));
            minioService.uploadFile(iconObjectKey, icon);

            user.setIcon(iconObjectKey);
            userRepository.save(user);
        }
    }

    @Override
    public void deleteResources(User user) {
        if (minioService.folderExists(getResourceFolder(user))) minioService.deleteFolder(getResourceFolder(user));
    }

    @Override
    public String getResourceFolder(User user) {
        return String.format(RESOURCES_FOLDER, user.getId());
    }

    @Override
    public List<String> getResources(User user) {
        return minioService.getFolder(getResourceFolder(user));
    }
}
