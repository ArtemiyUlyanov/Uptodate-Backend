package me.artemiyulyanov.uptodate.minio;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class MinioService {
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

    public void uploadFile(String objectKey, MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        long contentLength = file.getSize();

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucket,
                objectKey,
                inputStream,
                new ObjectMetadata()
        );
        putObjectRequest.getMetadata().setContentLength(contentLength);

        amazonS3.putObject(putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public MinioMediaFile getFile(String objectKey) {
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucket, objectKey));

        return MinioMediaFile
                .builder()
                .inputStream(s3Object.getObjectContent())
                .objectKey(objectKey)
                .build();
    }
}