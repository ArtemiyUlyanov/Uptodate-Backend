package me.artemiyulyanov.uptodate.minio.resources;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceManager<T> {
    void uploadResources(T entity, List<MultipartFile> files);
    void updateResources(T entity, List<MultipartFile> files);
    void deleteResources(T entity);

    String getResourceFolder(T entity);
    List<String> getResources(T entity);
}