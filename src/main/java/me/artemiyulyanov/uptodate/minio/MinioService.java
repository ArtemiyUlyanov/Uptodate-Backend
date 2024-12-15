package me.artemiyulyanov.uptodate.minio;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import jakarta.annotation.PostConstruct;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.ArticleComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class MinioService {
    public static final String ARTICLE_RESOURCES_FOLDER = "/articles/%d";
    public static final String ARTICLE_COMMENT_RESOURCES_FOLDER = "/articles/%d/comments/%d";

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private String bucket;

    @PostConstruct
    public void init() {
        if (!amazonS3.doesBucketExistV2(bucket)) {
            amazonS3.createBucket(bucket);
        }
    }

    public boolean uploadFile(String objectKey, MultipartFile file) {
        if(amazonS3.doesObjectExist(bucket, objectKey)) return false;

        try (InputStream inputStream = file.getInputStream()) {
            long contentLength = file.getSize();

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucket,
                    objectKey,
                    inputStream,
                    new ObjectMetadata()
            );
            putObjectRequest.getMetadata().setContentLength(contentLength);

            amazonS3.putObject(putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void removeFile(String objectKey) {
        if(amazonS3.doesObjectExist(bucket, objectKey)) amazonS3.deleteObject(new DeleteObjectRequest(bucket, objectKey));
    }

    public MinioMediaFile getFile(String objectKey) {
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucket, objectKey));

        return MinioMediaFile
                .builder()
                .inputStream(s3Object.getObjectContent())
                .objectKey(objectKey)
                .build();
    }

    public void saveArticleResources(Article article, List<MultipartFile> resources) {
        resources.forEach(file -> uploadFile(String.format(ARTICLE_RESOURCES_FOLDER, article.getId()) + File.separator + file.getOriginalFilename(), file));
    }

    public void saveArticleCommentResources(ArticleComment comment, List<MultipartFile> resources) {
        resources.forEach(file -> uploadFile(String.format(ARTICLE_COMMENT_RESOURCES_FOLDER, comment.getArticle().getId(), comment.getId()) + File.separator + file.getOriginalFilename(), file));
    }

    public void deleteArticleResources(Article article) {
        String resourcesFolder = String.format(ARTICLE_RESOURCES_FOLDER, article.getId());
        if(amazonS3.doesObjectExist(bucket, resourcesFolder)) removeFile(resourcesFolder);
    }

    public void deleteArticleCommentResources(ArticleComment comment) {
        String resourcesFolder = String.format(ARTICLE_RESOURCES_FOLDER, comment.getAuthor().getId(), comment.getId());
        if(amazonS3.doesObjectExist(bucket, resourcesFolder)) removeFile(resourcesFolder);
    }
}