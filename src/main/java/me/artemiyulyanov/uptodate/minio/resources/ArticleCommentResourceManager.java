package me.artemiyulyanov.uptodate.minio.resources;

import lombok.*;
import me.artemiyulyanov.uptodate.minio.MinioService;
import me.artemiyulyanov.uptodate.models.ArticleComment;
import me.artemiyulyanov.uptodate.repositories.ArticleCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
@Builder
public class ArticleCommentResourceManager implements ResourceManager<ArticleComment> {
    public static final String RESOURCES_FOLDER = "articles/%d/comments/%d";

    @Autowired
    private MinioService minioService;

    @Override
    public List<String> uploadResources(ArticleComment comment, List<MultipartFile> files) {
        if (files != null) {
            return files.stream()
                    .map(file -> minioService.uploadFile(getResourceFolder(comment) + File.separator + file.getOriginalFilename(), file))
                    .toList();
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> updateResources(ArticleComment comment, List<MultipartFile> files) {
        deleteResources(comment);

        if (files != null) {
            return files.stream()
                    .map(file -> minioService.uploadFile(getResourceFolder(comment) + File.separator + file.getOriginalFilename(), file))
                    .toList();
        }

        return Collections.emptyList();
    }

    @Override
    public void deleteResources(ArticleComment comment, List<String> filesNames) {
        filesNames.forEach(fileName -> minioService.deleteFile(getResourceFolder(comment) + File.separator + fileName));
    }

    @Override
    public void deleteResources(ArticleComment comment) {
        if (minioService.folderExists(getResourceFolder(comment))) minioService.deleteFolder(getResourceFolder(comment));
    }

    @Override
    public String getResourceFolder(ArticleComment comment) {
        return String.format(RESOURCES_FOLDER, comment.getArticle().getId(), comment.getId());
    }

    @Override
    public List<String> getResources(ArticleComment comment) {
        return minioService.getFolder(getResourceFolder(comment));
    }
}
