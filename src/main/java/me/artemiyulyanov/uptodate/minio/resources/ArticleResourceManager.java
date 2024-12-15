package me.artemiyulyanov.uptodate.minio.resources;

import lombok.*;
import me.artemiyulyanov.uptodate.minio.MinioService;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.text.ArticleTextFragment;
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
public class ArticleResourceManager implements ResourceManager<Article> {
    public static final String RESOURCES_FOLDER = "articles/%d";

    @Autowired
    private MinioService minioService;

    @Override
    public void uploadResources(Article article, List<MultipartFile> files) {
        if (files != null) {
            files.forEach(file -> minioService.uploadFile(getResourceFolder(article) + File.separator + file.getOriginalFilename(), file));
        }
    }

    @Override
    public void updateResources(Article article, List<MultipartFile> files) {
        List<String> images = article.getContent()
                .stream()
                .filter(fragment -> fragment.getType() == ArticleTextFragment.ArticleTextFragmentType.IMAGE)
                .map(ArticleTextFragment::getText)
                .toList();

        List<String> resources = getResources(article);
        resources.stream()
                .filter(resource -> !images.contains(resource))
                .forEach(resource -> minioService.deleteFile(resource));
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
}