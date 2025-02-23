package me.artemiyulyanov.uptodate.minio.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import me.artemiyulyanov.uptodate.minio.MinioService;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.ContentBlock;
import me.artemiyulyanov.uptodate.repositories.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
@Builder
public class ArticleResourceManager implements ResourceManager<Article> {
    public static final String RESOURCES_FOLDER = "articles/%d";

    @Autowired
    private MinioService minioService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<String> uploadResources(Article article, List<MultipartFile> files) {
        if (files != null) {
            return files.stream()
                    .map(file -> minioService.uploadFile(getResourceFolder(article) + File.separator + file.getOriginalFilename(), file))
                    .toList();
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> updateResources(Article article, List<MultipartFile> files) {
        deleteResources(article);

        if (files != null) {
            return files.stream()
                    .map(file -> minioService.uploadFile(getResourceFolder(article) + File.separator + file.getOriginalFilename(), file))
                    .toList();
        }

        return Collections.emptyList();
    }

    @Override
    public void deleteResources(Article article, List<String> filesNames) {
        filesNames.forEach(fileName -> minioService.deleteFile(getResourceFolder(article) + File.separator + fileName));
    }

    @Override
    public void deleteResources(Article article) {
        if (minioService.folderExists(getResourceFolder(article))) minioService.deleteFolder(getResourceFolder(article));
    }

    @Override
    public String getResourceFolder(Article article) {
        return String.format(RESOURCES_FOLDER, article.getId());
    }

    @Override
    public List<String> getResources(Article article) {
        return minioService.getFolder(getResourceFolder(article));
    }

    public List<ContentBlock> uploadContent(Article article, List<MultipartFile> resources) {
        AtomicInteger index = new AtomicInteger(0);

        List<String> resourcesUrls = uploadResources(article, resources);
        List<ContentBlock> updatedContentBlocks = article.getContent().stream()
                .filter(contentBlock -> contentBlock.getType().equals("image") && contentBlock.getText().startsWith("file-"))
                .peek(contentBlock -> contentBlock.setText(resourcesUrls.get(index.getAndIncrement())))
                .toList();

        return updatedContentBlocks;
    }

    public String uploadCover(Article article, MultipartFile cover) {
        return uploadResources(article, List.of(cover)).stream().findFirst().orElse(null);
    }
}